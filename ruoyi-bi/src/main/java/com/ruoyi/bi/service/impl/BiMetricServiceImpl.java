package com.ruoyi.bi.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.bi.domain.BiMetricAlert;
import com.ruoyi.bi.domain.BiMetricCheckLog;
import com.ruoyi.bi.domain.BiMetricDict;
import com.ruoyi.bi.domain.BiMetricDictLog;
import com.ruoyi.bi.domain.BiMetricSnapshot;
import com.ruoyi.bi.mapper.BiMetricAlertMapper;
import com.ruoyi.bi.mapper.BiMetricCheckLogMapper;
import com.ruoyi.bi.mapper.BiMetricDictLogMapper;
import com.ruoyi.bi.mapper.BiMetricDictMapper;
import com.ruoyi.bi.mapper.BiMetricSnapshotMapper;
import com.ruoyi.bi.service.IBiMetricService;
import com.ruoyi.common.stats.IModuleStatsProvider;
import com.ruoyi.common.stats.MetricValue;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.utils.MessageHelper;

/**
 * BI指标服务实现（统一指标层核心）
 *
 * 依赖说明：通过 Spring 注入全部 IModuleStatsProvider 实现，
 * 本模块 Maven 层不依赖任何业务模块（PRD v1.2 F1.2）。
 *
 * @author ruoyi
 */
@Service
public class BiMetricServiceImpl implements IBiMetricService
{
    private static final Logger log = LoggerFactory.getLogger(BiMetricServiceImpl.class);

    @Autowired
    private BiMetricDictMapper dictMapper;

    @Autowired
    private BiMetricSnapshotMapper snapshotMapper;

    @Autowired
    private BiMetricCheckLogMapper checkLogMapper;

    @Autowired
    private BiMetricDictLogMapper dictLogMapper;

    @Autowired
    private BiMetricAlertMapper alertMapper;

    /** 部门查询（ruoyi-system 平台基础模块；PRD F5 部门维度快照与可见范围计算） */
    @Autowired
    private SysDeptMapper deptMapper;

    /** 站内消息发送（ruoyi-system 平台基础模块，规范 5.14 消息通知机制） */
    @Autowired
    private MessageHelper messageHelper;

    /** F7 预警推送默认接收角色（字典 notify_roles 未配置时兜底，避免全员轰炸） */
    private static final String ALERT_RECIPIENT_ROLE = "admin";

    /** 抑制规则常量：同级别 24h 内不重复推送 / 未恢复每 3 天汇总提醒（PRD v1.1 F7） */
    private static final long SUPPRESS_WINDOW_MS = 24L * 3600 * 1000;
    private static final long SUMMARY_REMIND_MS = 3L * 24 * 3600 * 1000;

    /** 指标提供者集合（各业务模块实现，可能为空——支持 Provider 分批接入） */
    private final List<IModuleStatsProvider> providers;

    public BiMetricServiceImpl(@Autowired(required = false) List<IModuleStatsProvider> providers)
    {
        this.providers = providers == null ? Collections.<IModuleStatsProvider> emptyList() : providers;
    }

    /**
     * 指标编码 → 提供者 映射（每次调用重建，保证热部署后一致）
     */
    private Map<String, IModuleStatsProvider> buildProviderMap()
    {
        Map<String, IModuleStatsProvider> map = new HashMap<>();
        for (IModuleStatsProvider provider : providers)
        {
            for (String code : provider.supportedMetricCodes())
            {
                map.putIfAbsent(code, provider);
            }
        }
        return map;
    }

    @Override
    public int snapshotMetrics(Date metricDate)
    {
        Date day = dayStart(metricDate);
        List<BiMetricDict> dictList = dictMapper.selectEnabledList();
        Map<String, IModuleStatsProvider> providerMap = buildProviderMap();

        List<BiMetricSnapshot> rows = new ArrayList<>();
        int skipped = 0;
        for (BiMetricDict dict : dictList)
        {
            IModuleStatsProvider provider = providerMap.get(dict.getMetricCode());
            if (provider == null)
            {
                skipped++;
                continue;
            }
            try
            {
                MetricValue mv = provider.getMetric(dict.getMetricCode(), day, null);
                BiMetricSnapshot row = new BiMetricSnapshot();
                row.setMetricCode(dict.getMetricCode());
                row.setMetricDate(day);
                // 0=全部门汇总（dept_id NOT NULL，避免 NULL 使唯一键失效）
                row.setDeptId(mv.getDeptId() == null ? Long.valueOf(0L) : mv.getDeptId());
                row.setMetricValue(mv.getValue());
                row.setUnit(mv.getUnit());
                row.setDataFlag(mv.getDataFlag() == null ? "real" : mv.getDataFlag());
                row.setSnapshotTime(new Date());
                rows.add(row);
            }
            catch (Exception e)
            {
                // 单指标失败不中断整批快照（接口契约第 4 条的兜底防御）
                log.error("[BI快照] 指标{}计算异常，本次跳过", dict.getMetricCode(), e);
            }
        }

        // ④ 部门维度快照（PRD F5）：逐部门调用 Provider，仅落有值的部门粒度行；
        // Provider 未实现部门维度时 getMetric 返回 value=null，自然不落库（支持分批接入）
        List<SysDept> deptList = deptMapper.selectDeptList(new SysDept());
        for (SysDept dept : deptList)
        {
            if (dept.getDeptId() == null || !"0".equals(dept.getStatus()))
            {
                continue;
            }
            for (BiMetricDict dict : dictList)
            {
                IModuleStatsProvider provider = providerMap.get(dict.getMetricCode());
                if (provider == null)
                {
                    continue;
                }
                try
                {
                    MetricValue mv = provider.getMetric(dict.getMetricCode(), day, dept.getDeptId());
                    if (mv == null || mv.getValue() == null)
                    {
                        continue;
                    }
                    BiMetricSnapshot row = new BiMetricSnapshot();
                    row.setMetricCode(dict.getMetricCode());
                    row.setMetricDate(day);
                    row.setDeptId(mv.getDeptId() == null ? dept.getDeptId() : mv.getDeptId());
                    row.setMetricValue(mv.getValue());
                    row.setUnit(mv.getUnit());
                    row.setDataFlag(mv.getDataFlag() == null ? "real" : mv.getDataFlag());
                    row.setSnapshotTime(new Date());
                    rows.add(row);
                }
                catch (Exception e)
                {
                    log.error("[BI快照] 指标{}部门{}计算异常，本次跳过", dict.getMetricCode(), dept.getDeptId(), e);
                }
            }
        }

        // 幂等：同日重跑先删后插（删除必须用截断后的 day，与 insert 键一致；
        // 若用原始 metricDate（带时分秒）则永远删不中，insert 撞唯一键 uk_metric）
        snapshotMapper.deleteByMetricDate(day);
        if (!rows.isEmpty())
        {
            snapshotMapper.insertBatch(rows);
        }
        log.info("[BI快照] 完成：日期={}，字典指标={}，写入={}，无Provider跳过={}",
                day, dictList.size(), rows.size(), skipped);
        return rows.size();
    }

    @Override
    public int checkMetrics(Date metricDate)
    {
        Date day = dayStart(metricDate);
        List<BiMetricDict> dictList = dictMapper.selectEnabledList();
        Map<String, IModuleStatsProvider> providerMap = buildProviderMap();

        List<BiMetricCheckLog> logs = new ArrayList<>();
        int inconsistent = 0;
        for (BiMetricDict dict : dictList)
        {
            IModuleStatsProvider provider = providerMap.get(dict.getMetricCode());
            if (provider == null)
            {
                continue;
            }
            BigDecimal realtime = null;
            try
            {
                realtime = provider.getMetric(dict.getMetricCode(), day, null).getValue();
            }
            catch (Exception e)
            {
                log.error("[BI对账] 指标{}实时计算异常", dict.getMetricCode(), e);
            }

            BiMetricSnapshot snap = snapshotMapper.selectByUnique(dict.getMetricCode(), day, null);
            BigDecimal snapVal = snap == null ? null : snap.getMetricValue();

            boolean match = realtime == null ? snapVal == null
                    : (snapVal != null && realtime.compareTo(snapVal) == 0);
            if (!match)
            {
                inconsistent++;
            }

            BiMetricCheckLog logRow = new BiMetricCheckLog();
            logRow.setMetricCode(dict.getMetricCode());
            logRow.setCheckDate(day);
            logRow.setDeptId(0L);
            logRow.setSnapshotValue(snapVal);
            logRow.setRealtimeValue(realtime);
            if (realtime != null && snapVal != null)
            {
                logRow.setDiffValue(realtime.subtract(snapVal));
            }
            logRow.setCheckStatus(match ? "0" : "1");
            logRow.setCreateTime(new Date());
            logs.add(logRow);
        }

        if (!logs.isEmpty())
        {
            checkLogMapper.insertBatch(logs);
        }
        if (inconsistent > 0)
        {
            log.warn("[BI对账] 完成：日期={}，比对={}，不一致={}（详见 bi_metric_check_log）",
                    day, logs.size(), inconsistent);
        }
        else
        {
            log.info("[BI对账] 完成：日期={}，比对={}，全部一致", day, logs.size());
        }
        return inconsistent;
    }

    // ==================== F7 阈值预警（PRD v1.2） ====================

    @Override
    public int alertMetrics(Date metricDate)
    {
        Date day = dayStart(metricDate);
        Date now = new Date();
        List<BiMetricDict> dictList = dictMapper.selectEnabledList();

        int triggered = 0;
        for (BiMetricDict dict : dictList)
        {
            // 阈值被清空视为恢复基线消失：不参与预警匹配，但必须先走恢复判定
            //（否则配置错误的阈值触发的预警在删除阈值后永远挂"未恢复"）
            boolean hasThreshold = dict.getThresholdRed() != null || dict.getThresholdYellow() != null;

            BiMetricSnapshot snap = snapshotMapper.selectByUnique(dict.getMetricCode(), day, null);
            BigDecimal value = snap == null ? null : snap.getMetricValue();
            String level = hasThreshold ? evaluateLevel(dict, value) : null;

            // 无预警：处理恢复（上次未恢复的全部预警——含黄/红多级别——置已恢复）
            if (level == null)
            {
                List<BiMetricAlert> opens = alertMapper.selectOpenAlerts(dict.getMetricCode());
                for (BiMetricAlert open : opens)
                {
                    open.setStatus("1");
                    open.setRecoverTime(now);
                    open.setUpdateBy("system");
                    alertMapper.updateAlert(open);
                    log.info("[BI预警] 指标{}已恢复（值={}），预警单{}关闭", dict.getMetricCode(), value, open.getId());
                }
                continue;
            }

            // 有预警：按 uk_alert upsert（重跑只刷新数值，不重复推送）
            BiMetricAlert exist = alertMapper.selectByUnique(dict.getMetricCode(), day, 0L, level);
            if (exist != null)
            {
                exist.setMetricValue(value);
                exist.setThresholdValue(thresholdOf(dict, level));
                exist.setAlertMessage(buildAlertMessage(dict, level, value, day));
                exist.setUpdateBy("system");
                alertMapper.updateAlert(exist);
                continue;
            }

            // 新触发的预警记录
            BiMetricAlert alert = new BiMetricAlert();
            alert.setMetricCode(dict.getMetricCode());
            alert.setMetricDate(day);
            alert.setDeptId(0L);
            alert.setAlertLevel(level);
            alert.setMetricValue(value);
            alert.setThresholdValue(thresholdOf(dict, level));
            alert.setAlertMessage(buildAlertMessage(dict, level, value, day));
            alert.setPushCount(0);
            alert.setStatus("0");
            alert.setCreateBy("system");
            alert.setCreateTime(now);
            alert.setPushStatus("0");

            // 抑制规则判定（升级立即推 > 24h 同级抑制 > 3 天汇总提醒）
            String suppressReason = decideSuppress(dict, level, now);
            if (suppressReason == null)
            {
                // 先落库生成 id（站内消息 biz_id 需回指预警记录），推送后回写推送状态
                alertMapper.insertAlert(alert);
                pushAlert(dict, alert, now);
                alertMapper.updateAlert(alert);
            }
            else
            {
                alert.setPushStatus("2");
                alert.setPushRemark(suppressReason);
                alertMapper.insertAlert(alert);
                log.info("[BI预警] 指标{}级别{}触发但被抑制：{}", dict.getMetricCode(), level, suppressReason);
            }
            triggered++;
        }

        log.info("[BI预警] 完成：日期={}，参与字典={}，新触发={}条", day, dictList.size(), triggered);
        return triggered;
    }

    @Override
    public List<BiMetricAlert> selectAlertList(BiMetricAlert query)
    {
        return alertMapper.selectAlertList(query);
    }

    /**
     * 阈值级别判定：2红线 > 1黄线（threshold_dir=lt 低于预警 / gt 超限预警）
     */
    private String evaluateLevel(BiMetricDict dict, BigDecimal value)
    {
        if (value == null)
        {
            return null;
        }
        boolean lt = "lt".equals(dict.getThresholdDir());
        if (dict.getThresholdRed() != null)
        {
            BigDecimal t = BigDecimal.valueOf(dict.getThresholdRed());
            if (lt ? value.compareTo(t) < 0 : value.compareTo(t) > 0)
            {
                return "2";
            }
        }
        if (dict.getThresholdYellow() != null)
        {
            BigDecimal t = BigDecimal.valueOf(dict.getThresholdYellow());
            if (lt ? value.compareTo(t) < 0 : value.compareTo(t) > 0)
            {
                return "1";
            }
        }
        return null;
    }

    /**
     * 级别对应阈值
     */
    private BigDecimal thresholdOf(BiMetricDict dict, String level)
    {
        return "2".equals(level)
                ? (dict.getThresholdRed() == null ? null : BigDecimal.valueOf(dict.getThresholdRed()))
                : (dict.getThresholdYellow() == null ? null : BigDecimal.valueOf(dict.getThresholdYellow()));
    }

    private String buildAlertMessage(BiMetricDict dict, String level, BigDecimal value, Date day)
    {
        String levelName = "2".equals(level) ? "红线" : "黄线";
        String dirText = "lt".equals(dict.getThresholdDir()) ? "已低于" : "已超过";
        return "指标【" + dict.getMetricName() + "】" + DateUtils.parseDateToStr("yyyy-MM-dd", day)
                + " 值 " + value + " " + dirText + levelName + "阈值 " + thresholdOf(dict, level);
    }

    /**
     * 抑制规则判定（PRD v1.1 F7）：
     * 1. 级别升级（黄→红）→ 立即推送（返回 null）；
     * 2. 同级别 24h 内已推送 → 抑制；
     * 3. 未恢复预警距上次推送 ≥3 天 → 汇总提醒（返回 null）；
     * 4. 其余未恢复场景 → 抑制。
     *
     * @return null=允许推送；非 null=抑制原因
     */
    private String decideSuppress(BiMetricDict dict, String level, Date now)
    {
        BiMetricAlert open = alertMapper.selectOpenAlert(dict.getMetricCode());

        // 1. 升级立即推送（黄→红）
        if (open != null && "1".equals(open.getAlertLevel()) && "2".equals(level))
        {
            return null;
        }

        // 2. 同级别 24h 内已推送 → 抑制
        BiMetricAlert lastPushed = alertMapper.selectLatestPushed(dict.getMetricCode(), level);
        if (lastPushed != null && lastPushed.getPushTime() != null
                && now.getTime() - lastPushed.getPushTime().getTime() < SUPPRESS_WINDOW_MS)
        {
            return "24小时内同级别已推送（" + DateUtils.parseDateToStr("MM-dd HH:mm", lastPushed.getPushTime()) + "）";
        }

        // 3. 未恢复预警每 3 天汇总提醒
        if (open != null && open.getPushTime() != null)
        {
            if (now.getTime() - open.getPushTime().getTime() >= SUMMARY_REMIND_MS)
            {
                return null;
            }
            return "预警未恢复，每3天汇总提醒一次（上次推送 "
                    + DateUtils.parseDateToStr("MM-dd HH:mm", open.getPushTime()) + "）";
        }

        // 新预警首推
        return null;
    }

    /**
     * 推送站内消息（sys_message type=2 业务预警，红线=紧急/黄线=重要，按角色分发）
     *
     * F7 通知对象（可配置）：字典 notify_roles 配置接收角色（逗号分隔 role_key），
     * 空则回退默认 admin；多角色逐角色发送，单角色失败不影响其他角色。
     */
    private void pushAlert(BiMetricDict dict, BiMetricAlert alert, Date now)
    {
        String levelName = "2".equals(alert.getAlertLevel()) ? "红线" : "黄线";
        String msgLevel = "2".equals(alert.getAlertLevel()) ? "3" : "2";
        String title = "【指标预警-" + levelName + "】" + dict.getMetricName();
        String[] roles = (dict.getNotifyRoles() == null || dict.getNotifyRoles().trim().isEmpty())
                ? new String[] { ALERT_RECIPIENT_ROLE } : dict.getNotifyRoles().trim().split(",");
        boolean pushed = false;
        String lastError = null;
        for (String roleKey : roles)
        {
            String role = roleKey.trim();
            if (role.isEmpty())
            {
                continue;
            }
            try
            {
                messageHelper.sendMessage(title, alert.getAlertMessage(), "2", msgLevel,
                        "bi", alert.getId(), "/bi/dashboard", role);
                pushed = true;
            }
            catch (Exception e)
            {
                // 单角色推送失败不阻断（记录最后异常，其余角色继续）
                lastError = e.getMessage();
                log.error("[BI预警] 指标{}推送角色{}失败", dict.getMetricCode(), role, e);
            }
        }
        if (pushed)
        {
            alert.setPushStatus("1");
            alert.setPushTime(now);
            alert.setPushCount(alert.getPushCount() == null ? 1 : alert.getPushCount() + 1);
            if (lastError != null)
            {
                alert.setPushRemark("部分角色推送异常：" + lastError);
            }
            log.info("[BI预警] 指标{}级别{}已推送站内消息（角色：{}）",
                    dict.getMetricCode(), alert.getAlertLevel(), dict.getNotifyRoles());
        }
        else
        {
            // 推送失败不阻断预警记录落库（记录保持未推送状态，可人工核查）
            alert.setPushStatus("0");
            alert.setPushRemark("推送异常：" + lastError);
            log.error("[BI预警] 指标{}推送失败", dict.getMetricCode());
        }
    }

    // ==================== F6 跨模块价值链视图（PRD v1.2） ====================

    /** 价值链静态定义（节点编码序列 + 环节说明；指标名称以字典为准） */
    private static final String[][] CHAIN_SALES_TO_CASH = {
            {"MK_LEAD_CVR", "线索 → 成交"},
            {"MK_WIN_RATE", "商机 → 赢单"},
            {"BID_WIN_RATE", "投标 → 中标"},
            {"MK_SIG_AMT", "签约金额"},
            {"MMS_WO_RATE", "生产完工"},
            {"MK_RCPT_AMT", "回款落袋"}};
    private static final String[][] CHAIN_SUPPLY = {
            {"PMS_ONTIME_RATE", "到货及时"},
            {"WMS_TURNOVER", "库存周转"},
            {"QMS_PASS_RATE", "检验合格"},
            {"MMS_WO_RATE", "生产完工"},
            {"DMS_OEE", "设备效率"}};

    @Override
    public Map<String, Object> getValueChain()
    {
        Map<String, Object> result = new HashMap<>();
        Date latestDate = snapshotMapper.selectLatestMetricDate();
        result.put("snapshotDate", latestDate);
        if (latestDate == null)
        {
            result.put("chains", new ArrayList<>());
            return result;
        }

        // 62 天窗口趋势数据 → 指标编码 → (yyyy-MM → 该月最后一个快照日的值)
        Calendar cal = Calendar.getInstance();
        cal.setTime(latestDate);
        cal.add(Calendar.DAY_OF_MONTH, -61);
        List<BiMetricSnapshot> trend = snapshotMapper.selectTrendList(dayStart(cal.getTime()), latestDate, null);

        String latestMonth = new java.text.SimpleDateFormat("yyyy-MM").format(latestDate);
        Map<String, Map<String, BiMetricSnapshot>> byMonth = new HashMap<>();
        for (BiMetricSnapshot s : trend)
        {
            if (s.getMetricValue() == null)
            {
                continue;
            }
            String month = new java.text.SimpleDateFormat("yyyy-MM").format(s.getMetricDate());
            Map<String, BiMetricSnapshot> monthMap = byMonth.computeIfAbsent(s.getMetricCode(), k -> new HashMap<>());
            BiMetricSnapshot exist = monthMap.get(month);
            // 趋势已按日期升序，后出现的即该月更晚快照日
            if (exist == null || !exist.getMetricDate().after(s.getMetricDate()))
            {
                monthMap.put(month, s);
            }
        }

        // 指标字典（编码 → 名称/单位）
        Map<String, BiMetricDict> dictMap = new HashMap<>();
        for (BiMetricDict dict : dictMapper.selectEnabledList())
        {
            dictMap.put(dict.getMetricCode(), dict);
        }

        List<Map<String, Object>> chains = new ArrayList<>();
        chains.add(buildChain("sales_to_cash", "销售到现金链",
                "线索转化 → 商机赢单 → 投标中标 → 月签约 → 工单完成 → 月回款", CHAIN_SALES_TO_CASH, dictMap, byMonth, latestMonth));
        chains.add(buildChain("supply_assurance", "供应保障链",
                "到货及时 → 库存周转 → 检验合格 → 生产完工 → 设备效率", CHAIN_SUPPLY, dictMap, byMonth, latestMonth));
        result.put("chains", chains);
        return result;
    }

    /**
     * 组装单条链路：节点取最新月值，环比取上月值（上月无快照则对比为空）
     */
    private Map<String, Object> buildChain(String code, String name, String desc, String[][] nodeDefs,
            Map<String, BiMetricDict> dictMap, Map<String, Map<String, BiMetricSnapshot>> byMonth, String latestMonth)
    {
        String prevMonth;
        {
            java.util.Calendar c = java.util.Calendar.getInstance();
            try
            {
                c.setTime(new java.text.SimpleDateFormat("yyyy-MM").parse(latestMonth));
            }
            catch (java.text.ParseException e)
            {
                throw new IllegalStateException("月份解析失败：" + latestMonth, e);
            }
            c.add(Calendar.MONTH, -1);
            prevMonth = new java.text.SimpleDateFormat("yyyy-MM").format(c.getTime());
        }

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (String[] def : nodeDefs)
        {
            String metricCode = def[0];
            String linkLabel = def[1];
            BiMetricDict dict = dictMap.get(metricCode);
            if (dict == null)
            {
                continue; // 字典已删除的节点不出链
            }
            Map<String, BiMetricSnapshot> monthMap = byMonth.get(metricCode);
            BiMetricSnapshot cur = monthMap == null ? null : monthMap.get(latestMonth);
            BiMetricSnapshot prev = monthMap == null ? null : monthMap.get(prevMonth);

            Map<String, Object> node = new HashMap<>();
            node.put("metricCode", metricCode);
            node.put("metricName", dict.getMetricName());
            node.put("linkLabel", linkLabel);
            node.put("value", cur == null ? null : cur.getMetricValue());
            node.put("prevValue", prev == null ? null : prev.getMetricValue());
            node.put("unit", cur == null ? null : cur.getUnit());
            node.put("dataFlag", cur == null ? null : cur.getDataFlag());
            node.put("month", cur == null ? null : new java.text.SimpleDateFormat("yyyy-MM-dd").format(cur.getMetricDate()));
            nodes.add(node);
        }

        Map<String, Object> chain = new HashMap<>();
        chain.put("code", code);
        chain.put("name", name);
        chain.put("desc", desc);
        chain.put("nodes", nodes);
        return chain;
    }

    // ==================== F8 报表导出（PRD v1.2，规范 4.3.11） ====================

    @Override
    public List<com.ruoyi.bi.domain.BiDashboardExport> buildExportRows()
    {
        List<com.ruoyi.bi.domain.BiDashboardExport> rows = new ArrayList<>();
        Date latestDate = snapshotMapper.selectLatestMetricDate();
        if (latestDate == null)
        {
            return rows;
        }
        Map<String, BiMetricDict> dictMap = new HashMap<>();
        for (BiMetricDict dict : dictMapper.selectEnabledList())
        {
            dictMap.put(dict.getMetricCode(), dict);
        }
        for (BiMetricSnapshot snap : snapshotMapper.selectListByDate(latestDate))
        {
            BiMetricDict dict = dictMap.get(snap.getMetricCode());
            if (dict == null)
            {
                continue;
            }
            com.ruoyi.bi.domain.BiDashboardExport row = new com.ruoyi.bi.domain.BiDashboardExport();
            row.setMetricCode(snap.getMetricCode());
            row.setMetricName(dict.getMetricName());
            row.setDomain(dict.getDomain());
            row.setValue(snap.getMetricValue() == null ? "暂无数据" : snap.getMetricValue().toPlainString());
            row.setUnit(snap.getUnit());
            row.setDataFlag(snap.getDataFlag());
            row.setSnapshotDate(snap.getMetricDate());
            rows.add(row);
        }
        return rows;
    }

    /**
     * 截断到当日零点（bi_metric_snapshot.metric_date 为 date 列，
     * 带时分秒的参数会导致等值比较失配与幂等删除失效）
     */
    private Date dayStart(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // ==================== 指标字典管理（留痕见 bi_metric_dict_log） ====================

    @Override
    public List<BiMetricDict> selectDictList(BiMetricDict biMetricDict)
    {
        return dictMapper.selectDictList(biMetricDict);
    }

    @Override
    public BiMetricDict selectDictById(Long id)
    {
        return dictMapper.selectDictById(id);
    }

    @Override
    public boolean checkMetricCodeUnique(BiMetricDict biMetricDict)
    {
        BiMetricDict exist = dictMapper.checkMetricCodeUnique(biMetricDict.getMetricCode(), biMetricDict.getId());
        Long existId = exist == null ? null : exist.getId();
        return existId == null || existId.equals(biMetricDict.getId());
    }

    @Override
    public int insertDict(BiMetricDict dict)
    {
        int rows = dictMapper.insertDict(dict);
        if (rows > 0)
        {
            List<BiMetricDictLog> logs = new ArrayList<>();
            logs.add(buildLog(dict.getMetricCode(), "INSERT", "metric_code", null, dict.getMetricCode(), dict.getCreateBy()));
            logs.add(buildLog(dict.getMetricCode(), "INSERT", "caliber_desc", null, dict.getCaliberDesc(), dict.getCreateBy()));
            dictLogMapper.insertBatch(logs);
        }
        return rows;
    }

    @Override
    public int updateDict(BiMetricDict dict)
    {
        BiMetricDict old = dictMapper.selectDictById(dict.getId());
        if (old == null)
        {
            return 0;
        }
        int rows = dictMapper.updateDict(dict);
        if (rows > 0)
        {
            // 逐字段 diff 留痕——口径变更审计的基础
            List<BiMetricDictLog> logs = new ArrayList<>();
            diffAndLog(logs, old, dict, "metricName", old.getMetricName(), dict.getMetricName(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "domain", old.getDomain(), dict.getDomain(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "caliberDesc", old.getCaliberDesc(), dict.getCaliberDesc(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "calcRef", old.getCalcRef(), dict.getCalcRef(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "refreshType", old.getRefreshType(), dict.getRefreshType(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "freq", old.getFreq(), dict.getFreq(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "thresholdRed",
                    old.getThresholdRed() == null ? null : String.valueOf(old.getThresholdRed()),
                    dict.getThresholdRed() == null ? null : String.valueOf(dict.getThresholdRed()), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "thresholdYellow",
                    old.getThresholdYellow() == null ? null : String.valueOf(old.getThresholdYellow()),
                    dict.getThresholdYellow() == null ? null : String.valueOf(dict.getThresholdYellow()), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "thresholdDir", old.getThresholdDir(), dict.getThresholdDir(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "owner", old.getOwner(), dict.getOwner(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "drillUrl", old.getDrillUrl(), dict.getDrillUrl(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "notifyRoles", old.getNotifyRoles(), dict.getNotifyRoles(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "status", old.getStatus(), dict.getStatus(), dict.getUpdateBy());
            diffAndLog(logs, old, dict, "remark", old.getRemark(), dict.getRemark(), dict.getUpdateBy());
            if (!logs.isEmpty())
            {
                dictLogMapper.insertBatch(logs);
                // 口径定义变更属高风险操作，同步告警日志
                log.info("[BI字典] 指标{}变更 {} 处字段，操作人{}", old.getMetricCode(), logs.size(), dict.getUpdateBy());
            }
        }
        return rows;
    }

    @Override
    public int deleteDictByIds(Long[] ids, String updateBy)
    {
        // 删除前取编码用于留痕
        List<BiMetricDictLog> logs = new ArrayList<>();
        for (Long id : ids)
        {
            BiMetricDict old = dictMapper.selectDictById(id);
            if (old != null)
            {
                logs.add(buildLog(old.getMetricCode(), "DELETE", "metric_code", old.getMetricCode(), null, updateBy));
            }
        }
        int rows = dictMapper.deleteDictByIds(ids, updateBy);
        if (rows > 0 && !logs.isEmpty())
        {
            dictLogMapper.insertBatch(logs);
        }
        return rows;
    }

    @Override
    public List<BiMetricDictLog> selectLogByMetricCode(String metricCode)
    {
        return dictLogMapper.selectLogByMetricCode(metricCode);
    }

    @Override
    public List<BiMetricCheckLog> selectCheckLogList(BiMetricCheckLog query)
    {
        return checkLogMapper.selectCheckLogList(query);
    }

    private BiMetricDictLog buildLog(String metricCode, String changeType, String fieldName, String oldValue, String newValue, String operator)
    {
        BiMetricDictLog logRow = new BiMetricDictLog();
        logRow.setMetricCode(metricCode);
        logRow.setChangeType(changeType);
        logRow.setFieldName(fieldName);
        logRow.setOldValue(oldValue);
        logRow.setNewValue(newValue);
        logRow.setChangeBy(operator);
        return logRow;
    }

    private void diffAndLog(List<BiMetricDictLog> logs, BiMetricDict old, BiMetricDict updated,
            String fieldName, String oldValue, String newValue, String operator)
    {
        // 新值为 null 视为"请求未提交该字段"（updateDict 的 XML 对普通字段做条件更新，
        // null 不会写库），因此不记留痕，避免产生"改为空"的误导记录；
        // threshold_red/yellow 两列为无条件更新（null 会清空），需如实留痕，不走本方法判断。
        if (newValue == null)
        {
            return;
        }
        if (oldValue != null && oldValue.equals(newValue))
        {
            return;
        }
        logs.add(buildLog(old.getMetricCode(), "UPDATE", fieldName, oldValue, newValue, operator));
    }

    @Override
    public Map<String, Object> getDashboardOverview(int trendDays)
    {
        return buildOverviewData(null, trendDays);
    }

    @Override
    public Map<String, Object> getBoardData(String domain, int trendDays)
    {
        return buildOverviewData(domain, trendDays);
    }

    /**
     * 驾驶舱/主题看板共用取数（F2/F3）。
     *
     * 数据权限（PRD F5，@DataScope 等效语义的快照维度过滤）：
     * - 全量权限（admin/全部数据角色）：可见全部指标行（含 dept_id=0 公司级汇总行）；
     * - 受限权限：仅可见其部门范围内的部门粒度行，公司级 dept_id=0 汇总行隐藏（脱敏）。
     *
     * @param domain 域过滤（null=全部域，驾驶舱；非 null=主题看板单域）
     */
    private Map<String, Object> buildOverviewData(String domain, int trendDays)
    {
        Map<String, Object> result = new HashMap<>();
        Collection<Long> visibleDepts = visibleDeptIds();
        result.put("deptScopeAll", visibleDepts == null);

        // 1. 最新快照日期（取快照表最大 metric_date；无快照时整体返回空结构）
        Date latestDate = snapshotMapper.selectLatestMetricDate();
        result.put("snapshotDate", latestDate);
        if (latestDate == null)
        {
            result.put("metrics", new ArrayList<>());
            result.put("alerts", new ArrayList<>());
            result.put("trend", new ArrayList<>());
            return result;
        }

        // 2. 指标字典（编码 → 名称/域/阈值）
        Map<String, BiMetricDict> dictMap = new HashMap<>();
        for (BiMetricDict dict : dictMapper.selectEnabledList())
        {
            dictMap.put(dict.getMetricCode(), dict);
        }

        // 3. 最新快照指标行（可见部门过滤 + 域过滤，合并字典信息）
        List<Map<String, Object>> metrics = new ArrayList<>();
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (BiMetricSnapshot snap : snapshotMapper.selectListByDate(latestDate))
        {
            BiMetricDict dict = dictMap.get(snap.getMetricCode());
            if (dict == null)
            {
                continue; // 字典已停用/删除的残留快照不入驾驶舱
            }
            if (domain != null && !domain.equals(dict.getDomain()))
            {
                continue; // F3 主题看板仅展示本域指标
            }
            if (visibleDepts != null
                    && (snap.getDeptId() == null || !visibleDepts.contains(snap.getDeptId())))
            {
                continue; // 受限权限：公司级 dept_id=0 行与其他部门行不可见（脱敏）
            }
            Map<String, Object> row = new HashMap<>();
            row.put("metricCode", snap.getMetricCode());
            row.put("metricName", dict.getMetricName());
            row.put("domain", dict.getDomain());
            row.put("deptId", snap.getDeptId());
            row.put("value", snap.getMetricValue());
            row.put("unit", snap.getUnit());
            row.put("dataFlag", snap.getDataFlag());
            row.put("snapshotDate", snap.getMetricDate());
            row.put("drillUrl", dict.getDrillUrl());
            metrics.add(row);

            // 预警横幅：红（越阈值）→ 橙（估算口径）→ 蓝（无数据），一指标至多一条
            Map<String, Object> alert = buildMetricAlert(dict, snap.getMetricValue(), snap.getUnit(), snap.getDataFlag());
            if (alert != null)
            {
                alerts.add(alert);
            }
        }
        result.put("metrics", metrics);
        result.put("alerts", alerts);

        // 4. 近 N 天趋势（按可见部门维度过滤）
        Calendar cal = Calendar.getInstance();
        cal.setTime(latestDate);
        cal.add(Calendar.DAY_OF_MONTH, -(Math.max(trendDays, 1) - 1));
        List<BiMetricSnapshot> trend = snapshotMapper.selectTrendList(dayStart(cal.getTime()), latestDate, visibleDepts);
        if (domain != null)
        {
            // 主题看板仅保留本域指标的趋势行
            List<BiMetricSnapshot> filtered = new ArrayList<>();
            for (BiMetricSnapshot t : trend)
            {
                BiMetricDict dict = dictMap.get(t.getMetricCode());
                if (dict != null && domain.equals(dict.getDomain()))
                {
                    filtered.add(t);
                }
            }
            trend = filtered;
        }
        result.put("trend", trend);
        return result;
    }

    /**
     * 当前登录人可见部门范围（PRD F5：@DataScope 等效语义的快照维度过滤）。
     *
     * @return null=全部可见（admin 或角色含"全部数据"权限）；非 null=可见 deptId 集合
     *         （空集合=受限且未配置部门，兜底本部门后仍空则不可见任何数据）
     */
    private Collection<Long> visibleDeptIds()
    {
        SysUser user;
        try
        {
            user = SecurityUtils.getLoginUser().getUser();
        }
        catch (Exception e)
        {
            // 系统内部调用（无登录上下文，如任务触发的查询）不做过滤
            return null;
        }
        if (user == null || user.isAdmin())
        {
            return null;
        }
        Set<Long> depts = new HashSet<>();
        boolean allScope = false;
        if (user.getRoles() != null)
        {
            for (SysRole role : user.getRoles())
            {
                if (role == null || role.getDataScope() == null)
                {
                    continue;
                }
                switch (role.getDataScope())
                {
                    case "1": // 全部数据
                        allScope = true;
                        break;
                    case "3": // 本部门
                    case "5": // 仅本人：指标无人员维度，按本部门处理
                        if (user.getDeptId() != null)
                        {
                            depts.add(user.getDeptId());
                        }
                        break;
                    case "4": // 本部门及以下
                        if (user.getDeptId() != null)
                        {
                            depts.add(user.getDeptId());
                            collectChildren(user.getDeptId(), depts);
                        }
                        break;
                    case "2": // 自定义：按角色已授权部门
                        List<Long> roleDepts = deptMapper.selectDeptListByRoleId(role.getRoleId(), true);
                        if (roleDepts != null)
                        {
                            depts.addAll(roleDepts);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        if (!allScope && depts.isEmpty() && user.getDeptId() != null)
        {
            // 角色未配置数据权限时兜底本部门，避免误放全量
            depts.add(user.getDeptId());
        }
        return allScope ? null : depts;
    }

    /** 递归收集子部门 id（PRD F5 dataScope=4 本部门及以下） */
    private void collectChildren(Long parentId, Set<Long> out)
    {
        List<SysDept> children = deptMapper.selectChildrenDeptById(parentId);
        if (children == null)
        {
            return;
        }
        for (SysDept child : children)
        {
            if (child.getDeptId() != null && out.add(child.getDeptId()))
            {
                collectChildren(child.getDeptId(), out);
            }
        }
    }

    @Override
    public int backfillMetrics(int days)
    {
        Date yesterday = dayStart(DateUtils.addDays(new Date(), -1));
        Calendar cal = Calendar.getInstance();
        cal.setTime(yesterday);
        cal.add(Calendar.DAY_OF_MONTH, -(Math.max(days, 1) - 1));
        int total = 0;
        Date cursor = dayStart(cal.getTime());
        while (!cursor.after(yesterday))
        {
            // 复用 snapshotMetrics 幂等设计（同日先删后插），已有数据的日期仅刷新数值
            total += snapshotMetrics(cursor);
            Calendar next = Calendar.getInstance();
            next.setTime(cursor);
            next.add(Calendar.DAY_OF_MONTH, 1);
            cursor = dayStart(next.getTime());
        }
        log.info("[BI回补] 完成：天数={}，累计写入快照行={}（逐日幂等，重跑安全）", days, total);
        return total;
    }

    /**
     * 单指标预警判定（红 > 橙 > 蓝取最高级）
     */
    private Map<String, Object> buildMetricAlert(BiMetricDict dict, BigDecimal value, String unit, String dataFlag)
    {
        String level = null;
        String message = null;

        if (value == null)
        {
            level = "info";
            message = "指标【" + dict.getMetricName() + "】暂无数据（业务侧未产生或未核算）";
        }
        else if ("estimate".equals(dataFlag))
        {
            level = "warning";
            message = "指标【" + dict.getMetricName() + "】为估算口径，仅供参考";
        }

        // 阈值告警优先级最高（字典配置 threshold_red/yellow 后自动生效）：
        // 红线=danger 红、黄线=warning 橙
        if (value != null && dict.getThresholdRed() != null)
        {
            BigDecimal threshold = BigDecimal.valueOf(dict.getThresholdRed());
            boolean hit = "lt".equals(dict.getThresholdDir())
                    ? value.compareTo(threshold) < 0
                    : value.compareTo(threshold) > 0;
            if (hit)
            {
                level = "danger";
                message = "指标【" + dict.getMetricName() + "】当前值 " + value
                        + (unit == null ? "" : unit)
                        + " 已越过红线阈值 " + dict.getThresholdRed();
            }
        }
        if (level == null && value != null && dict.getThresholdYellow() != null)
        {
            BigDecimal threshold = BigDecimal.valueOf(dict.getThresholdYellow());
            boolean hit = "lt".equals(dict.getThresholdDir())
                    ? value.compareTo(threshold) < 0
                    : value.compareTo(threshold) > 0;
            if (hit)
            {
                level = "warning";
                message = "指标【" + dict.getMetricName() + "】当前值 " + value
                        + (unit == null ? "" : unit)
                        + " 已越过黄线阈值 " + dict.getThresholdYellow();
            }
        }

        if (level == null)
        {
            return null;
        }
        Map<String, Object> alert = new HashMap<>();
        alert.put("level", level);
        alert.put("metricCode", dict.getMetricCode());
        alert.put("message", message);
        return alert;
    }
}
