package com.ruoyi.integration.wecom;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 企业微信 API 客户端（qyapi）
 *
 * 依赖参数配置（系统管理-参数设置）：
 *   integration.wecom.enabled  = true/false
 *   integration.wecom.corpId   = 企业ID（企业管理后台 → 我的企业 → 企业信息）
 *   integration.wecom.secret   = 自建应用 Secret
 *
 * 使用前提（企业微信后台配置，缺一不可）：
 *   1. 自建应用的可见范围需覆盖需要同步的成员；
 *   2. 如需同步打卡数据，必须在「应用管理 → 打卡 → 可调用接口的应用」中加入该自建应用；
 *   3. 自建应用的服务器出口 IP 需加入「企业可信IP」。
 *
 * 与企业微信安全机制相关的两个已知限制：
 *   - 2022-06-20 起新建自建应用调用 user/list 不再返回 mobile（手机号），
 *     需成员 OAuth2 手工授权才可获取，因此手机号匹配可能拿不到值，须依赖 userid/alias 对工号兜底；
 *   - 2023-12-01 起不再支持用系统应用 secret 调用接口，打卡接口必须用自建应用 secret。
 *
 * @author ruoyi
 */
@Component
public class WecomApiClient
{
    private static final Logger log = LoggerFactory.getLogger(WecomApiClient.class);

    private static final String API_BASE = "https://qyapi.weixin.qq.com";

    private static final String TOKEN_CACHE_KEY = "integration:wecom:access_token";

    /** 通讯录根部门ID（企业微信固定为 1） */
    private static final long ROOT_DEPT_ID = 1L;

    /** 打卡记录单次查询用户数上限（企业微信限制 100） */
    public static final int ATTENDANCE_USER_LIMIT = 100;

    /** 打卡记录单次时间跨度上限（天，企业微信限制 30） */
    public static final int ATTENDANCE_MAX_SPAN_DAYS = 30;

    /** 参数键 */
    private static final String KEY_ENABLED = "integration.wecom.enabled";

    private static final String KEY_CORP_ID = "integration.wecom.corpId";

    private static final String KEY_SECRET = "integration.wecom.secret";

    private static final String KEY_MOCK_ENABLED = "integration.wecom.mockEnabled";

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private WecomMockDataProvider mockDataProvider;

    /**
     * 是否开启模拟数据模式（无真实凭据时用于端到端联调）
     */
    public boolean isMockEnabled()
    {
        try
        {
            return "true".equalsIgnoreCase(configService.selectConfigByKey(KEY_MOCK_ENABLED));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * 集成是否已启用且凭据齐全
     *
     * 模拟数据模式下无需凭据，直接视为可用。
     */
    public boolean isEnabled()
    {
        try
        {
            if (isMockEnabled())
            {
                return true;
            }
            String enabled = configService.selectConfigByKey(KEY_ENABLED);
            if (!"true".equalsIgnoreCase(enabled))
            {
                return false;
            }
            String corpId = configService.selectConfigByKey(KEY_CORP_ID);
            String secret = configService.selectConfigByKey(KEY_SECRET);
            return StringUtils.isNotEmpty(corpId) && StringUtils.isNotEmpty(secret);
        }
        catch (Exception e)
        {
            log.error("读取企业微信集成配置失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 access_token（Redis 缓存，提前 10 分钟失效）
     */
    public String getAccessToken()
    {
        String cached = redisCache.getCacheObject(TOKEN_CACHE_KEY);
        if (StringUtils.isNotEmpty(cached))
        {
            return cached;
        }
        String corpId = configService.selectConfigByKey(KEY_CORP_ID);
        String secret = configService.selectConfigByKey(KEY_SECRET);
        String url = API_BASE + "/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + secret;
        JSONObject resp = JSON.parseObject(HttpUtils.sendGet(url));
        checkError(resp);
        String token = resp.getString("access_token");
        Long expiresIn = resp.getLong("expires_in");
        if (StringUtils.isEmpty(token))
        {
            throw new RuntimeException("企业微信获取 access_token 失败: " + resp.toJSONString());
        }
        long ttl = (expiresIn != null ? expiresIn : 7200) - 600;
        redisCache.setCacheObject(TOKEN_CACHE_KEY, token, (int) Math.max(ttl, 60), TimeUnit.SECONDS);
        return token;
    }

    /**
     * 清除 token 缓存（配置变更后调用）
     */
    public void clearTokenCache()
    {
        redisCache.deleteObject(TOKEN_CACHE_KEY);
    }

    /**
     * 全量拉取企业通讯录用户
     *
     * 企业微信 user/list 无分页，但只能按部门查询，因此需先取全部部门再逐部门拉取；
     * 使用 LinkedHashMap 按 userid 去重（同一成员可能属于多个部门）。
     *
     * @return 用户列表，含 userid / name / mobile / alias / position / department
     */
    public List<JSONObject> listAllUsers()
    {
        if (isMockEnabled())
        {
            return mockDataProvider.listUsers();
        }
        String token = getAccessToken();
        List<Long> deptIds = listAllDepartmentIds(token);

        Map<String, JSONObject> userMap = new LinkedHashMap<>();
        for (Long deptId : deptIds)
        {
            String url = API_BASE + "/cgi-bin/user/list?access_token=" + token
                    + "&department_id=" + deptId + "&fetch_child=0";
            JSONObject resp = JSON.parseObject(HttpUtils.sendGet(url));
            // 部门无权限或部门已删除时跳过，不中断整体同步
            if (resp.getIntValue("errcode") != 0)
            {
                log.warn("企业微信拉取部门 {} 成员失败: errcode={}, errmsg={}", deptId,
                        resp.getIntValue("errcode"), resp.getString("errmsg"));
                continue;
            }
            JSONArray userlist = resp.getJSONArray("userlist");
            if (userlist == null || userlist.isEmpty())
            {
                continue;
            }
            for (int i = 0; i < userlist.size(); i++)
            {
                JSONObject user = userlist.getJSONObject(i);
                String userid = user.getString("userid");
                if (StringUtils.isNotEmpty(userid))
                {
                    userMap.put(userid, user);
                }
            }
        }
        List<JSONObject> users = new ArrayList<>(userMap.values());
        log.info("企业微信通讯录拉取完成: 共 {} 个部门、{} 个用户", deptIds.size(), users.size());
        return users;
    }

    /**
     * 拉取全部部门ID（根部门 + 所有子部门）
     */
    private List<Long> listAllDepartmentIds(String token)
    {
        List<Long> deptIds = new ArrayList<>();
        deptIds.add(ROOT_DEPT_ID);
        String url = API_BASE + "/cgi-bin/department/simplelist?access_token=" + token;
        JSONObject resp = JSON.parseObject(HttpUtils.sendGet(url));
        checkError(resp);
        JSONArray departments = resp.getJSONArray("department_id");
        if (departments == null)
        {
            // 兜底兼容 department/list 的返回结构
            departments = resp.getJSONArray("department");
        }
        if (departments != null)
        {
            for (int i = 0; i < departments.size(); i++)
            {
                JSONObject dept = departments.getJSONObject(i);
                Long id = dept.getLong("id");
                if (id != null && !deptIds.contains(id))
                {
                    deptIds.add(id);
                }
            }
        }
        return deptIds;
    }

    /**
     * 拉取指定日期范围的打卡记录
     *
     * 企业微信该接口是"获取打卡记录数据"(checkin/getcheckindata)，一次最多 30 天、100 人；
     * 返回的 checkin_type 已明确是「上班打卡 / 下班打卡 / 外出打卡 / 仅记录打卡时间和位置」，
     * 调用方据此精确落位，无需按班次推断上下班。
     *
     * @param beginDate 开始时间
     * @param endDate   结束时间
     * @param useridList 企业微信 userid 列表（≤100）
     * @return 打卡记录列表（原始 JSONObject）
     */
    public List<JSONObject> listAttendance(Date beginDate, Date endDate, List<String> useridList)
    {
        List<JSONObject> records = new ArrayList<>();
        if (useridList == null || useridList.isEmpty())
        {
            return records;
        }
        // 企业微信限制：单次用户数不超过 100，超出则分批
        for (int from = 0; from < useridList.size(); from += ATTENDANCE_USER_LIMIT)
        {
            List<String> batch = useridList.subList(from, Math.min(from + ATTENDANCE_USER_LIMIT, useridList.size()));
            records.addAll(listAttendanceBatch(beginDate, endDate, batch));
        }
        return records;
    }

    /**
     * 单批（≤100人）打卡记录拉取
     */
    private List<JSONObject> listAttendanceBatch(Date beginDate, Date endDate, List<String> useridList)
    {
        if (isMockEnabled())
        {
            return mockDataProvider.listAttendance(beginDate, endDate, useridList);
        }
        String token = getAccessToken();
        Map<String, Object> body = new HashMap<>();
        // 3 = 全部打卡（含上下班与外出）
        body.put("opencheckindatatype", 3);
        body.put("starttime", beginDate.getTime() / 1000);
        body.put("endtime", endDate.getTime() / 1000);
        body.put("useridlist", useridList);
        String url = API_BASE + "/cgi-bin/checkin/getcheckindata?access_token=" + token;
        JSONObject resp = JSON.parseObject(HttpUtils.sendPost(url, JSON.toJSONString(body), "application/json;charset=utf-8"));
        // 该接口无数据时不返回 checkindata，errcode 为 0，正常容错
        if (resp.getIntValue("errcode") != 0)
        {
            log.warn("企业微信打卡记录查询返回异常: {}", resp.toJSONString());
            return new ArrayList<>();
        }
        JSONArray checkindata = resp.getJSONArray("checkindata");
        List<JSONObject> records = new ArrayList<>();
        if (checkindata != null)
        {
            for (int i = 0; i < checkindata.size(); i++)
            {
                records.add(checkindata.getJSONObject(i));
            }
        }
        return records;
    }

    /**
     * 统一错误检查
     */
    private void checkError(JSONObject resp)
    {
        if (resp == null)
        {
            throw new RuntimeException("企业微信接口返回为空");
        }
        Integer errcode = resp.getInteger("errcode");
        if (errcode != null && errcode != 0)
        {
            throw new RuntimeException("企业微信接口调用失败 errcode=" + errcode + ", errmsg=" + resp.getString("errmsg"));
        }
    }
}
