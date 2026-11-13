package com.ruoyi.integration.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrAttendanceImportDTO;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.service.IHrAttendanceRecordService;
import com.ruoyi.integration.domain.IntegrationSyncLog;
import com.ruoyi.integration.domain.IntegrationUserBinding;
import com.ruoyi.integration.mapper.IntegrationSyncLogMapper;
import com.ruoyi.integration.mapper.IntegrationUserBindingMapper;
import com.ruoyi.integration.service.IWecomSyncService;
import com.ruoyi.integration.wecom.WecomApiClient;
import com.ruoyi.integration.wecom.WecomMockDataProvider;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 企业微信集成同步服务实现
 *
 * 数据链路：企业微信通讯录(userid/alias/mobile) → integration_user_binding
 *          → 匹配 biz_hr_employee → checkin/getcheckindata 打卡记录
 *          → HrAttendanceImportDTO（带 punchType 精确落位）→ 复用 importAttendance 幂等导入
 *
 * 与钉钉实现的差异：
 * 1. 匹配策略为「手机号优先、工号兜底」：企业微信 2022-06-20 起新建自建应用不再返回 mobile，
 *    因此手机号未命中时自动用工号兜底，工号来源支持 alias（别名）与 userid 两种；
 * 2. 打卡数据原生带 checkin_type（上班打卡/下班打卡/外出打卡），据此精确落位上下班，
 *    外出打卡不参与落位，避免被当成正常上下班卡；
 * 3. 打卡接口限制不同：企业微信单次 30 天 / 100 人（钉钉为 7 天 / 50 人）。
 *
 * @author ruoyi
 */
@Service
public class WecomSyncServiceImpl implements IWecomSyncService
{
    private static final Logger log = LoggerFactory.getLogger(WecomSyncServiceImpl.class);

    private static final String CONNECTOR_WECOM = "wecom";

    /** 打卡来源字典值（hr_clock_source: 6=企业微信同步） */
    private static final String CLOCK_SOURCE_WECOM = "6";

    /** 打卡记录单批用户数（企业微信限制 100） */
    private static final int USER_BATCH_SIZE = WecomApiClient.ATTENDANCE_USER_LIMIT;

    /** 打卡记录单次时间跨度上限（天，企业微信限制 30） */
    private static final int MAX_SPAN_DAYS = WecomApiClient.ATTENDANCE_MAX_SPAN_DAYS;

    /** 单次同步最大天数（防误操作拉取过大范围） */
    private static final int MAX_SYNC_DAYS = 365;

    /** 匹配策略参数键：mobile=手机号优先工号兜底 / jobNumber=工号优先 / auto=同 mobile */
    private static final String MATCH_TYPE_KEY = "integration.wecom.matchType";

    /** 企业微信打卡类型：上班打卡 */
    private static final String TYPE_ON_DUTY = "上班打卡";

    /** 企业微信打卡类型：下班打卡 */
    private static final String TYPE_OFF_DUTY = "下班打卡";

    @Autowired
    private WecomApiClient wecomApiClient;

    @Autowired
    private IntegrationUserBindingMapper bindingMapper;

    @Autowired
    private IntegrationSyncLogMapper syncLogMapper;

    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;

    @Autowired
    private IHrAttendanceRecordService hrAttendanceRecordService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 同步企业微信通讯录并自动匹配员工档案
     */
    @Override
    public AjaxResult syncContacts()
    {
        long start = System.currentTimeMillis();
        if (!wecomApiClient.isEnabled())
        {
            return AjaxResult.error("企业微信集成未启用或凭据未配置（参数键：integration.wecom.enabled/corpId/secret）");
        }
        try
        {
            List<JSONObject> users = wecomApiClient.listAllUsers();
            int inserted = 0;
            int updated = 0;
            for (JSONObject user : users)
            {
                String userid = user.getString("userid");
                if (StringUtils.isEmpty(userid))
                {
                    continue;
                }
                // 企业微信无独立工号字段，alias（别名）常被企业用于填写工号，作为工号匹配来源
                String alias = user.getString("alias");
                IntegrationUserBinding existing = bindingMapper.selectByConnectorAndExternalUserid(CONNECTOR_WECOM, userid);
                if (existing != null)
                {
                    IntegrationUserBinding update = new IntegrationUserBinding();
                    update.setBindingId(existing.getBindingId());
                    update.setExternalName(user.getString("name"));
                    update.setMobile(user.getString("mobile"));
                    update.setExternalJobNo(alias);
                    update.setSyncTime(new Date());
                    bindingMapper.updateIntegrationUserBinding(update);
                    updated++;
                }
                else
                {
                    IntegrationUserBinding binding = new IntegrationUserBinding();
                    binding.setConnectorType(CONNECTOR_WECOM);
                    binding.setExternalUserid(userid);
                    binding.setExternalName(user.getString("name"));
                    binding.setMobile(user.getString("mobile"));
                    binding.setExternalJobNo(alias);
                    binding.setSyncTime(new Date());
                    binding.setCreateBy("wecom");
                    bindingMapper.insertIntegrationUserBinding(binding);
                    inserted++;
                }
            }

            AutoMatchStat matchStat = autoMatchBindings();

            String msg = mockPrefix() + "通讯录同步完成：新增 " + inserted + "，更新 " + updated + "，自动匹配员工 " + matchStat.matched
                    + "，未匹配 " + countUnmatched()
                    + (matchStat.skipped > 0 ? "（" + matchStat.skipped + " 个员工已被其他企业微信用户绑定，已跳过）" : "");
            recordLog("contact_sync", "-", "0", msg, start);
            return AjaxResult.success(msg);
        }
        catch (Exception e)
        {
            log.error("企业微信通讯录同步失败", e);
            recordLog("contact_sync", "-", "1", e.getMessage(), start);
            return AjaxResult.error("企业微信通讯录同步失败：" + e.getMessage());
        }
    }

    /**
     * 同步指定日期范围的打卡数据
     */
    @Override
    public AjaxResult syncAttendance(Date beginDate, Date endDate)
    {
        long start = System.currentTimeMillis();
        if (!wecomApiClient.isEnabled())
        {
            return AjaxResult.error("企业微信集成未启用或凭据未配置（参数键：integration.wecom.enabled/corpId/secret）");
        }
        if (beginDate == null || endDate == null)
        {
            return AjaxResult.error("同步日期范围不能为空");
        }
        if (beginDate.after(endDate))
        {
            return AjaxResult.error("开始日期不能晚于结束日期");
        }
        long spanDays = (endDate.getTime() - beginDate.getTime()) / (24L * 3600 * 1000) + 1;
        if (spanDays > MAX_SYNC_DAYS)
        {
            return AjaxResult.error("单次同步范围不能超过 " + MAX_SYNC_DAYS + " 天，请分段同步");
        }

        List<IntegrationUserBinding> bindings = bindingMapper.selectMatchedBindings(CONNECTOR_WECOM);
        if (bindings.isEmpty())
        {
            return AjaxResult.error("暂无已匹配的企业微信用户绑定，请先执行通讯录同步并完成员工匹配");
        }
        Map<String, IntegrationUserBinding> bindingMap = new HashMap<>();
        List<String> useridList = new ArrayList<>();
        for (IntegrationUserBinding binding : bindings)
        {
            bindingMap.put(binding.getExternalUserid(), binding);
            useridList.add(binding.getExternalUserid());
        }

        int totalImported = 0;
        int totalPunches = 0;
        int totalUnmapped = 0;
        String lastMsg = "";
        try
        {
            // 按 30 天窗口切段（企业微信限制）
            Date windowBegin = beginDate;
            while (!windowBegin.after(endDate))
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(windowBegin);
                cal.add(Calendar.DAY_OF_MONTH, MAX_SPAN_DAYS - 1);
                Date windowEnd = cal.getTime();
                if (windowEnd.after(endDate))
                {
                    windowEnd = endDate;
                }
                // 打卡接口用秒级时间戳，结束时间需覆盖当日 23:59:59
                Date queryEnd = endOfDay(windowEnd);

                // 按 100 人一批分批拉取
                for (int from = 0; from < useridList.size(); from += USER_BATCH_SIZE)
                {
                    List<String> batch = useridList.subList(from, Math.min(from + USER_BATCH_SIZE, useridList.size()));
                    List<JSONObject> records = wecomApiClient.listAttendance(windowBegin, queryEnd, batch);

                    List<HrAttendanceImportDTO> dtoList = new ArrayList<>();
                    for (JSONObject record : records)
                    {
                        Long checkinTime = record.getLong("checkin_time");
                        if (checkinTime == null)
                        {
                            continue;
                        }
                        String punchUserid = record.getString("userid");
                        if (StringUtils.isEmpty(punchUserid))
                        {
                            punchUserid = record.getString("userId");
                        }
                        IntegrationUserBinding binding = bindingMap.get(punchUserid);
                        if (binding == null || StringUtils.isEmpty(binding.getEmployeeNo()))
                        {
                            totalUnmapped++;
                            continue;
                        }
                        HrAttendanceImportDTO dto = new HrAttendanceImportDTO();
                        dto.setEmployeeNo(binding.getEmployeeNo());
                        dto.setEmployeeName(binding.getExternalName());
                        // checkin_time 为秒级时间戳
                        dto.setClockTime(new Date(checkinTime * 1000L));
                        dto.setMachineNo("wecom");
                        // 精确落位：上班打卡/下班打卡映射为打卡类型；外出打卡等不参与上下班归位
                        dto.setPunchType(mapPunchType(record.getString("checkin_type")));
                        dtoList.add(dto);
                    }
                    totalPunches += dtoList.size();
                    if (!dtoList.isEmpty())
                    {
                        String sourceRemark = wecomApiClient.isMockEnabled() ? "【模拟】企业微信同步" : "企业微信同步";
                        AjaxResult importResult = hrAttendanceRecordService.importAttendance(dtoList, true, "wecom",
                                CLOCK_SOURCE_WECOM, sourceRemark);
                        totalImported += dtoList.size();
                        lastMsg = String.valueOf(importResult.get("msg"));
                    }
                }
                windowBegin = addDays(endOfDay(windowEnd), 1);
            }
            String msg = mockPrefix() + "考勤同步完成：拉取有效打卡 " + totalPunches + " 条，处理员工/日 " + totalImported + " 条，未匹配打卡 " + totalUnmapped + " 条"
                    + (StringUtils.isNotEmpty(lastMsg) ? "；导入明细：" + lastMsg : "");
            recordLog("attendance_sync", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, beginDate) + "~" + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, endDate), "0", msg, start);
            return AjaxResult.success(msg);
        }
        catch (Exception e)
        {
            log.error("企业微信考勤同步失败", e);
            recordLog("attendance_sync", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, beginDate) + "~" + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, endDate), "1", e.getMessage(), start);
            return AjaxResult.error("企业微信考勤同步失败：" + e.getMessage());
        }
    }

    /**
     * 企业微信 checkin_type 映射为考勤导入的打卡类型
     *
     * 仅「上班打卡 / 下班打卡」参与上下班精确落位；
     * 「外出打卡 / 仅记录打卡时间和位置」返回 null，交由导入链路按时间推断，避免外出卡被当成上下班卡。
     */
    private String mapPunchType(String checkinType)
    {
        if (TYPE_ON_DUTY.equals(checkinType))
        {
            return HrAttendanceImportDTO.PUNCH_ON_DUTY;
        }
        if (TYPE_OFF_DUTY.equals(checkinType))
        {
            return HrAttendanceImportDTO.PUNCH_OFF_DUTY;
        }
        return null;
    }

    /**
     * 按配置策略自动匹配员工档案
     *
     * 策略参数 integration.wecom.matchType：
     *   mobile（默认）= 手机号优先，未命中时用工号兜底
     *   jobNumber     = 工号优先，未命中时用手机号兜底
     *   auto          = 同 mobile
     *
     * 工号来源支持 alias（别名）与 userid 两种：企业微信无独立工号字段，
     * 部分企业把工号填在成员别名中，部分企业直接用工号作为 userid。
     *
     * 仅处理未匹配绑定，已匹配（含人工绑定）不重跑，防止策略切换冲掉既有绑定。
     * 一一绑定约束：目标员工已被其他外部用户占用时跳过。
     */
    private AutoMatchStat autoMatchBindings()
    {
        AutoMatchStat stat = new AutoMatchStat();
        String strategy = resolveMatchStrategy();
        IntegrationUserBinding query = new IntegrationUserBinding();
        query.setConnectorType(CONNECTOR_WECOM);
        query.getParams().put("matched", "no");
        List<IntegrationUserBinding> unmatched = bindingMapper.selectIntegrationUserBindingList(query);
        for (IntegrationUserBinding binding : unmatched)
        {
            HrEmployee employee = null;
            String usedStrategy = null;

            if ("jobNumber".equals(strategy))
            {
                // 工号优先
                employee = matchByJobNo(binding);
                usedStrategy = "jobNumber";
                if (employee == null)
                {
                    employee = matchByMobile(binding);
                    usedStrategy = "mobile";
                }
            }
            else
            {
                // 默认：手机号优先，工号兜底
                employee = matchByMobile(binding);
                usedStrategy = "mobile";
                if (employee == null)
                {
                    employee = matchByJobNo(binding);
                    usedStrategy = "jobNumber";
                }
            }

            if (employee != null)
            {
                IntegrationUserBinding occupied = bindingMapper.selectOccupiedBinding(CONNECTOR_WECOM,
                        employee.getEmployeeId(), binding.getBindingId());
                if (occupied != null)
                {
                    log.info("员工 {} 已被企业微信用户 {} 绑定，跳过自动匹配（外部用户：{}）", employee.getEmployeeNo(),
                            occupied.getExternalName(), binding.getExternalName());
                    stat.skipped++;
                    continue;
                }
                IntegrationUserBinding update = new IntegrationUserBinding();
                update.setBindingId(binding.getBindingId());
                update.setEmployeeNo(employee.getEmployeeNo());
                update.setEmployeeId(employee.getEmployeeId());
                update.setMatchType("auto_" + usedStrategy);
                update.setSyncTime(new Date());
                bindingMapper.updateIntegrationUserBinding(update);
                stat.matched++;
            }
        }
        return stat;
    }

    /**
     * 按手机号精确匹配员工档案
     */
    private HrEmployee matchByMobile(IntegrationUserBinding binding)
    {
        if (StringUtils.isEmpty(binding.getMobile()))
        {
            return null;
        }
        return hrEmployeeMapper.selectHrEmployeeByPhone(binding.getMobile());
    }

    /**
     * 按工号匹配员工档案：先取 alias（别名），再取 userid
     */
    private HrEmployee matchByJobNo(IntegrationUserBinding binding)
    {
        HrEmployee employee = null;
        String alias = binding.getExternalJobNo();
        if (StringUtils.isNotEmpty(alias))
        {
            employee = hrEmployeeMapper.selectHrEmployeeByEmployeeNo(alias);
        }
        if (employee == null && StringUtils.isNotEmpty(binding.getExternalUserid()))
        {
            employee = hrEmployeeMapper.selectHrEmployeeByEmployeeNo(binding.getExternalUserid());
        }
        return employee;
    }

    /**
     * 读取匹配策略配置（默认 mobile，即手机号优先、工号兜底）
     */
    private String resolveMatchStrategy()
    {
        try
        {
            String strategy = configService.selectConfigByKey(MATCH_TYPE_KEY);
            if ("jobNumber".equalsIgnoreCase(strategy))
            {
                return "jobNumber";
            }
        }
        catch (Exception e)
        {
            log.warn("读取企业微信匹配策略配置失败，使用默认手机号优先策略: {}", e.getMessage());
        }
        return "mobile";
    }

    /**
     * 统计未匹配绑定数量
     */
    private int countUnmatched()
    {
        IntegrationUserBinding query = new IntegrationUserBinding();
        query.setConnectorType(CONNECTOR_WECOM);
        query.getParams().put("matched", "no");
        return bindingMapper.selectIntegrationUserBindingList(query).size();
    }

    /**
     * 记录同步日志
     */
    private void recordLog(String bizType, String bizKey, String status, String resultMsg, long startMillis)
    {
        try
        {
            IntegrationSyncLog syncLog = new IntegrationSyncLog();
            syncLog.setConnectorType(CONNECTOR_WECOM);
            syncLog.setBizType(bizType);
            syncLog.setBizKey(bizKey);
            syncLog.setStatus(status);
            syncLog.setResultMsg(StringUtils.substring(resultMsg, 0, 990));
            syncLog.setCostMs(System.currentTimeMillis() - startMillis);
            syncLogMapper.insertIntegrationSyncLog(syncLog);
        }
        catch (Exception e)
        {
            log.error("集成同步日志写入失败: {}", e.getMessage());
        }
    }

    /**
     * 模拟数据模式下的结果前缀，避免与真实企业微信同步结果混淆
     */
    private String mockPrefix()
    {
        return wecomApiClient.isMockEnabled() ? "【模拟数据】" : "";
    }

    /** 取当日 23:59:59，保证打卡接口的时间窗口覆盖整天 */
    private Date endOfDay(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    /**
     * 自动匹配统计结果
     */
    private static class AutoMatchStat
    {
        /** 本次匹配成功数量 */
        private int matched;

        /** 因目标员工已被其他外部用户绑定而跳过的数量 */
        private int skipped;
    }
}
