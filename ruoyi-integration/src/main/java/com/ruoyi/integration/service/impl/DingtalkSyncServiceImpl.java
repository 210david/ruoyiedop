package com.ruoyi.integration.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.service.IHrAttendanceRecordService;
import com.ruoyi.integration.dingtalk.DingTalkApiClient;
import com.ruoyi.integration.domain.IntegrationSyncLog;
import com.ruoyi.integration.domain.IntegrationUserBinding;
import com.ruoyi.integration.mapper.IntegrationSyncLogMapper;
import com.ruoyi.integration.mapper.IntegrationUserBindingMapper;
import com.ruoyi.integration.service.IDingtalkSyncService;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 钉钉集成同步服务实现
 *
 * 数据链路：钉钉通讯录(userid/mobile) → integration_user_binding → 按手机号匹配 biz_hr_employee
 *          → attendance/list 打卡结果 → HrAttendanceImportDTO → 复用 importAttendance 幂等导入
 *
 * @author ruoyi
 */
@Service
public class DingtalkSyncServiceImpl implements IDingtalkSyncService
{
    private static final Logger log = LoggerFactory.getLogger(DingtalkSyncServiceImpl.class);

    private static final String CONNECTOR_DINGTALK = "dingtalk";

    /** 打卡来源字典值（hr_clock_source: 5=钉钉同步；0考勤机/1补卡/2手动/3外出/4出差已被占用） */
    private static final String CLOCK_SOURCE_DINGTALK = "5";

    /** 打卡结果单批用户数（钉钉限制 50） */
    private static final int USER_BATCH_SIZE = 50;

    /** 打卡结果单次时间跨度上限（天，钉钉限制 7） */
    private static final int MAX_SPAN_DAYS = 7;

    /** 单次同步最大天数（防误操作拉取过大范围） */
    private static final int MAX_SYNC_DAYS = 92;

    @Autowired
    private DingTalkApiClient dingTalkApiClient;

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

    /** 匹配策略参数键（mobile=手机号 jobNumber=工号 auto=工号优先手机号兜底） */
    private static final String MATCH_TYPE_KEY = "integration.dingtalk.matchType";

    /**
     * 同步钉钉通讯录并自动匹配员工档案
     */
    @Override
    public AjaxResult syncContacts()
    {
        long start = System.currentTimeMillis();
        if (!dingTalkApiClient.isEnabled())
        {
            return AjaxResult.error("钉钉集成未启用或凭据未配置（参数键：integration.dingtalk.enabled/appKey/appSecret）");
        }
        try
        {
            List<JSONObject> users = dingTalkApiClient.listAllUsers();
            int inserted = 0;
            int updated = 0;
            for (JSONObject user : users)
            {
                String userid = user.getString("userid");
                if (StringUtils.isEmpty(userid))
                {
                    continue;
                }
                IntegrationUserBinding existing = bindingMapper.selectByConnectorAndExternalUserid(CONNECTOR_DINGTALK, userid);
                if (existing != null)
                {
                    IntegrationUserBinding update = new IntegrationUserBinding();
                    update.setBindingId(existing.getBindingId());
                    update.setExternalName(user.getString("name"));
                    update.setMobile(user.getString("mobile"));
                    update.setExternalJobNo(user.getString("job_number"));
                    update.setSyncTime(new Date());
                    bindingMapper.updateIntegrationUserBinding(update);
                    updated++;
                }
                else
                {
                    IntegrationUserBinding binding = new IntegrationUserBinding();
                    binding.setConnectorType(CONNECTOR_DINGTALK);
                    binding.setExternalUserid(userid);
                    binding.setExternalName(user.getString("name"));
                    binding.setMobile(user.getString("mobile"));
                    binding.setExternalJobNo(user.getString("job_number"));
                    binding.setSyncTime(new Date());
                    binding.setCreateBy("dingtalk");
                    bindingMapper.insertIntegrationUserBinding(binding);
                    inserted++;
                }
            }

            AutoMatchStat matchStat = autoMatchBindings();

            String msg = mockPrefix() + "通讯录同步完成：新增 " + inserted + "，更新 " + updated + "，自动匹配员工 " + matchStat.matched + "，未匹配 "
                    + countUnmatched() + (matchStat.skipped > 0 ? "（" + matchStat.skipped + " 个员工已被其他钉钉用户绑定，已跳过）" : "");
            recordLog("contact_sync", "-", "0", msg, start);
            return AjaxResult.success(msg);
        }
        catch (Exception e)
        {
            log.error("钉钉通讯录同步失败", e);
            recordLog("contact_sync", "-", "1", e.getMessage(), start);
            return AjaxResult.error("钉钉通讯录同步失败：" + e.getMessage());
        }
    }

    /**
     * 同步指定日期范围的打卡数据
     */
    @Override
    public AjaxResult syncAttendance(Date beginDate, Date endDate)
    {
        long start = System.currentTimeMillis();
        if (!dingTalkApiClient.isEnabled())
        {
            return AjaxResult.error("钉钉集成未启用或凭据未配置（参数键：integration.dingtalk.enabled/appKey/appSecret）");
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

        // 已匹配员工的绑定关系
        List<IntegrationUserBinding> bindings = bindingMapper.selectMatchedBindings(CONNECTOR_DINGTALK);
        if (bindings.isEmpty())
        {
            return AjaxResult.error("暂无已匹配的钉钉用户绑定，请先执行通讯录同步并完成员工匹配");
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
            // 按 7 天窗口切段（钉钉限制）
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
                String dateFrom = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, windowBegin) + " 00:00:00";
                String dateTo = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, windowEnd) + " 23:59:59";

                // 按 50 人一批分批拉取
                for (int from = 0; from < useridList.size(); from += USER_BATCH_SIZE)
                {
                    List<String> batch = useridList.subList(from, Math.min(from + USER_BATCH_SIZE, useridList.size()));
                    List<JSONObject> records = dingTalkApiClient.listAttendance(dateFrom, dateTo, batch);

                    // 转换为考勤导入 DTO（仅取实际打卡时间，迟到/早退由平台班次规则统一计算）
                    List<HrAttendanceImportDTO> dtoList = new ArrayList<>();
                    for (JSONObject record : records)
                    {
                        Long checkTime = record.getLong("userCheckTime");
                        if (checkTime == null)
                        {
                            continue;
                        }
                        // 钉钉打卡记录的用户字段为 userId（驼峰），通讯录为 userid（全小写），此处兼容两种写法
                        String punchUserid = record.getString("userId");
                        if (StringUtils.isEmpty(punchUserid))
                        {
                            punchUserid = record.getString("userid");
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
                        dto.setClockTime(new Date(checkTime));
                        dto.setMachineNo("dingtalk");
                        dtoList.add(dto);
                    }
                    totalPunches += dtoList.size();
                    if (!dtoList.isEmpty())
                    {
                        // 模拟模式下在备注中标记来源，便于验证后精确清理，不影响正式数据
                        String sourceRemark = dingTalkApiClient.isMockEnabled() ? "【模拟】钉钉同步" : "钉钉同步";
                        // 复用平台现有导入链路：员工+日期分组、最早上班/最晚下班、幂等更新
                        AjaxResult importResult = hrAttendanceRecordService.importAttendance(dtoList, true, "dingtalk", CLOCK_SOURCE_DINGTALK, sourceRemark);
                        totalImported += dtoList.size();
                        lastMsg = String.valueOf(importResult.get("msg"));
                    }
                }
                windowBegin = addDays(windowEnd, 1);
            }
            String msg = mockPrefix() + "考勤同步完成：拉取有效打卡 " + totalPunches + " 条，处理员工/日 " + totalImported + " 条，未匹配打卡 " + totalUnmapped + " 条"
                    + (StringUtils.isNotEmpty(lastMsg) ? "；导入明细：" + lastMsg : "");
            recordLog("attendance_sync", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, beginDate) + "~" + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, endDate), "0", msg, start);
            return AjaxResult.success(msg);
        }
        catch (Exception e)
        {
            log.error("钉钉考勤同步失败", e);
            recordLog("attendance_sync", DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, beginDate) + "~" + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, endDate), "1", e.getMessage(), start);
            return AjaxResult.error("钉钉考勤同步失败：" + e.getMessage());
        }
    }

    /**
     * 按配置策略自动匹配员工档案
     * 策略参数 integration.dingtalk.matchType：mobile=手机号 / jobNumber=工号 / auto=工号优先、手机号兜底
     * 仅处理未匹配绑定，已匹配（含人工绑定）不重跑，防止策略切换冲掉既有绑定
     * 一一绑定约束：目标员工已被其他外部用户占用时跳过，避免同一员工打卡数据被两个账号覆盖
     *
     * @return 匹配统计（命中数 / 因员工被占用跳过数）
     */
    private AutoMatchStat autoMatchBindings()
    {
        AutoMatchStat stat = new AutoMatchStat();
        String strategy = resolveMatchStrategy();
        IntegrationUserBinding query = new IntegrationUserBinding();
        query.setConnectorType(CONNECTOR_DINGTALK);
        query.getParams().put("matched", "no");
        List<IntegrationUserBinding> unmatched = bindingMapper.selectIntegrationUserBindingList(query);
        for (IntegrationUserBinding binding : unmatched)
        {
            com.ruoyi.hr.domain.HrEmployee employee = null;
            String usedStrategy = null;

            // 工号优先（jobNumber / auto）
            if ("jobNumber".equals(strategy) || "auto".equals(strategy))
            {
                if (StringUtils.isNotEmpty(binding.getExternalJobNo()))
                {
                    employee = hrEmployeeMapper.selectHrEmployeeByEmployeeNo(binding.getExternalJobNo());
                    usedStrategy = "jobNumber";
                }
                // auto 策略下工号未命中时兜底手机号
                if (employee == null && "auto".equals(strategy))
                {
                    employee = matchByMobile(binding);
                    usedStrategy = "mobile";
                }
            }
            else
            {
                // 默认手机号策略
                employee = matchByMobile(binding);
                usedStrategy = "mobile";
            }

            if (employee != null)
            {
                // 一一绑定约束：员工已被其他钉钉用户绑定则跳过，保持未匹配等待人工处理
                IntegrationUserBinding occupied = bindingMapper.selectOccupiedBinding(CONNECTOR_DINGTALK,
                        employee.getEmployeeId(), binding.getBindingId());
                if (occupied != null)
                {
                    log.info("员工 {} 已被钉钉用户 {} 绑定，跳过自动匹配（外部用户：{}）", employee.getEmployeeNo(),
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
     * 自动匹配统计结果
     */
    private static class AutoMatchStat
    {
        /** 本次匹配成功数量 */
        private int matched;

        /** 因目标员工已被其他外部用户绑定而跳过的数量 */
        private int skipped;
    }

    /**
     * 按手机号精确匹配员工档案
     */
    private com.ruoyi.hr.domain.HrEmployee matchByMobile(IntegrationUserBinding binding)
    {
        if (StringUtils.isEmpty(binding.getMobile()))
        {
            return null;
        }
        return hrEmployeeMapper.selectHrEmployeeByPhone(binding.getMobile());
    }

    /**
     * 读取匹配策略配置（默认 mobile）
     */
    private String resolveMatchStrategy()
    {
        try
        {
            String strategy = configService.selectConfigByKey(MATCH_TYPE_KEY);
            if ("jobNumber".equalsIgnoreCase(strategy) || "auto".equalsIgnoreCase(strategy))
            {
                return strategy.toLowerCase();
            }
        }
        catch (Exception e)
        {
            log.warn("读取匹配策略配置失败，使用默认手机号匹配: {}", e.getMessage());
        }
        return "mobile";
    }

    /**
     * 统计未匹配绑定数量
     */
    private int countUnmatched()
    {
        IntegrationUserBinding query = new IntegrationUserBinding();
        query.setConnectorType(CONNECTOR_DINGTALK);
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
            syncLog.setConnectorType(CONNECTOR_DINGTALK);
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
     * 模拟数据模式下的结果前缀，避免与真实钉钉同步结果混淆
     */
    private String mockPrefix()
    {
        return dingTalkApiClient.isMockEnabled() ? "【模拟数据】" : "";
    }

    private Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
}
