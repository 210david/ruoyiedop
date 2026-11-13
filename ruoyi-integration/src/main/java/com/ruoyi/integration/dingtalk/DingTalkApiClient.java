package com.ruoyi.integration.dingtalk;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 钉钉开放平台 API 客户端（oapi）
 *
 * 依赖参数配置（系统管理-参数设置）：
 *   integration.dingtalk.enabled  = true/false
 *   integration.dingtalk.appKey   = 企业内部应用 AppKey
 *   integration.dingtalk.appSecret= 企业内部应用 AppSecret
 *
 * @author ruoyi
 */
@Component
public class DingTalkApiClient
{
    private static final Logger log = LoggerFactory.getLogger(DingTalkApiClient.class);

    private static final String OAPI_BASE = "https://oapi.dingtalk.com";

    private static final String TOKEN_CACHE_KEY = "integration:dingtalk:access_token";

    /** 通讯录同步的部门根节点 */
    private static final long ROOT_DEPT_ID = 1L;

    /** 打卡结果单次拉取上限（钉钉限制） */
    private static final int ATTENDANCE_LIMIT = 50;

    /** 打卡结果单次时间跨度上限（天，钉钉限制 7 天） */
    private static final int MAX_SPAN_DAYS = 7;

    /** 模拟数据模式参数键：为 true 时不发起任何 HTTP 请求，返回内置模拟数据 */
    private static final String KEY_MOCK_ENABLED = "integration.dingtalk.mockEnabled";

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private DingtalkMockDataProvider mockDataProvider;

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
     * 模拟数据模式下无需凭据，直接视为可用（真实数据仍受 enabled + 凭据约束）。
     */
    public boolean isEnabled()
    {
        try
        {
            if (isMockEnabled())
            {
                return true;
            }
            String enabled = configService.selectConfigByKey("integration.dingtalk.enabled");
            if (!"true".equalsIgnoreCase(enabled))
            {
                return false;
            }
            String appKey = configService.selectConfigByKey("integration.dingtalk.appKey");
            String appSecret = configService.selectConfigByKey("integration.dingtalk.appSecret");
            return StringUtils.isNotEmpty(appKey) && StringUtils.isNotEmpty(appSecret);
        }
        catch (Exception e)
        {
            log.error("读取钉钉集成配置失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 access_token（Redis 缓存，提前失效）
     */
    public String getAccessToken()
    {
        String cached = redisCache.getCacheObject(TOKEN_CACHE_KEY);
        if (StringUtils.isNotEmpty(cached))
        {
            return cached;
        }
        String appKey = configService.selectConfigByKey("integration.dingtalk.appKey");
        String appSecret = configService.selectConfigByKey("integration.dingtalk.appSecret");
        String url = OAPI_BASE + "/gettoken?appkey=" + appKey + "&appsecret=" + appSecret;
        JSONObject resp = JSON.parseObject(HttpUtils.sendGet(url));
        checkError(resp);
        String token = resp.getString("access_token");
        Long expiresIn = resp.getLong("expires_in");
        if (StringUtils.isEmpty(token))
        {
            throw new RuntimeException("钉钉获取 access_token 失败: " + resp.toJSONString());
        }
        // 提前 10 分钟失效
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
     * 全量拉取企业通讯录用户（递归遍历部门）
     *
     * @return 用户列表，含 userid / name / mobile
     */
    public List<JSONObject> listAllUsers()
    {
        // 模拟模式：返回内部构造的通讯录数据，不发起 HTTP 请求
        if (isMockEnabled())
        {
            return mockDataProvider.listUsers();
        }
        String token = getAccessToken();
        List<Long> deptIds = new ArrayList<>();
        collectDeptIds(token, ROOT_DEPT_ID, deptIds, 0);

        List<JSONObject> users = new ArrayList<>();
        for (Long deptId : deptIds)
        {
            long cursor = 0L;
            int guard = 0;
            while (guard++ < 50)
            {
                JSONObject body = new JSONObject();
                body.put("dept_id", deptId);
                body.put("cursor", cursor);
                body.put("size", 100);
                JSONObject resp = JSON.parseObject(HttpUtils.sendPost(OAPI_BASE + "/topapi/v2/user/list?access_token=" + token, body.toJSONString(), "application/json;charset=utf-8"));
                checkError(resp);
                JSONObject result = resp.getJSONObject("result");
                if (result == null)
                {
                    break;
                }
                JSONArray list = result.getJSONArray("list");
                if (list != null)
                {
                    for (int i = 0; i < list.size(); i++)
                    {
                        users.add(list.getJSONObject(i));
                    }
                }
                if (!result.getBooleanValue("has_more"))
                {
                    break;
                }
                cursor = result.getLongValue("next_cursor");
            }
        }
        log.info("钉钉通讯录拉取完成: 共 {} 个用户", users.size());
        return users;
    }

    /**
     * 递归收集部门ID
     */
    private void collectDeptIds(String token, long parentDeptId, List<Long> deptIds, int depth)
    {
        if (depth > 20)
        {
            return;
        }
        JSONObject body = new JSONObject();
        body.put("dept_id", parentDeptId);
        JSONObject resp = JSON.parseObject(HttpUtils.sendPost(OAPI_BASE + "/topapi/v2/department/listsub?access_token=" + token, body.toJSONString(), "application/json;charset=utf-8"));
        checkError(resp);
        deptIds.add(parentDeptId);
        JSONArray result = resp.getJSONArray("result");
        if (result != null)
        {
            for (int i = 0; i < result.size(); i++)
            {
                collectDeptIds(token, result.getJSONObject(i).getLongValue("dept_id"), deptIds, depth + 1);
            }
        }
    }

    /**
     * 拉取指定日期范围（≤7天）、指定用户的打卡结果
     *
     * @param workDateFrom yyyy-MM-dd HH:mm:ss
     * @param workDateTo   yyyy-MM-dd HH:mm:ss
     * @param useridList   钉钉 userid 列表（≤50）
     * @return 打卡记录列表（原始 JSONObject：userid / userCheckTime / checkType / timeResult 等）
     */
    public List<JSONObject> listAttendance(String workDateFrom, String workDateTo, List<String> useridList)
    {
        if (useridList == null || useridList.isEmpty())
        {
            return new ArrayList<>();
        }
        if (useridList.size() > ATTENDANCE_LIMIT)
        {
            throw new RuntimeException("钉钉打卡结果单次查询用户数不得超过 " + ATTENDANCE_LIMIT);
        }
        // 模拟模式：返回内部构造的打卡数据，不发起 HTTP 请求
        if (isMockEnabled())
        {
            return mockDataProvider.listAttendance(workDateFrom, workDateTo, useridList);
        }
        String token = getAccessToken();
        List<JSONObject> records = new ArrayList<>();
        int offset = 0;
        int guard = 0;
        while (guard++ < 200)
        {
            Map<String, Object> body = new HashMap<>();
            body.put("workDateFrom", workDateFrom);
            body.put("workDateTo", workDateTo);
            body.put("userIdList", useridList);
            body.put("offset", offset);
            body.put("limit", ATTENDANCE_LIMIT);
            JSONObject resp = JSON.parseObject(HttpUtils.sendPost(OAPI_BASE + "/attendance/list?access_token=" + token, JSON.toJSONString(body), "application/json;charset=utf-8"));
            // 打卡结果接口在无数据时 errcode 可能为 403/空记录，统一容错
            if (resp.getIntValue("errcode") != 0)
            {
                log.warn("钉钉打卡结果查询返回异常: {}", resp.toJSONString());
                break;
            }
            JSONArray recordresult = resp.getJSONArray("recordresult");
            if (recordresult == null || recordresult.isEmpty())
            {
                break;
            }
            for (int i = 0; i < recordresult.size(); i++)
            {
                records.add(recordresult.getJSONObject(i));
            }
            if (recordresult.size() < ATTENDANCE_LIMIT)
            {
                break;
            }
            offset += ATTENDANCE_LIMIT;
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
            throw new RuntimeException("钉钉接口返回为空");
        }
        Integer errcode = resp.getInteger("errcode");
        if (errcode != null && errcode != 0)
        {
            throw new RuntimeException("钉钉接口调用失败 errcode=" + errcode + ", errmsg=" + resp.getString("errmsg"));
        }
    }
}
