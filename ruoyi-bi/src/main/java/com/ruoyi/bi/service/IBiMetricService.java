package com.ruoyi.bi.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.ruoyi.bi.domain.BiMetricAlert;
import com.ruoyi.bi.domain.BiMetricCheckLog;
import com.ruoyi.bi.domain.BiMetricDict;
import com.ruoyi.bi.domain.BiMetricDictLog;

/**
 * BI指标服务接口（统一指标层核心）
 *
 * @author ruoyi
 */
public interface IBiMetricService
{
    /**
     * 生成指标日快照（幂等：同日重跑先删后插）
     *
     * 只写入当前已有 IModuleStatsProvider 实现的指标；
     * 未接入 Provider 的指标跳过并计数，不报错（支持分批接入）。
     *
     * @param metricDate 指标日期（每日 01:00 任务跑 T-1）
     * @return 成功写入快照的指标数
     */
    public int snapshotMetrics(Date metricDate);

    /**
     * 指标对账：实时值 vs 快照值比对，结果写 bi_metric_check_log
     *
     * @param metricDate 对账日期（与快照日期一致，T-1）
     * @return 不一致指标数
     */
    public int checkMetrics(Date metricDate);

    /**
     * F7 阈值预警匹配（每日 01:10 快照后执行）
     *
     * 规则（PRD v1.2 F7）：
     * - 匹配：当日快照值 vs 字典红/黄线阈值（threshold_dir 决定方向）；
     * - 抑制：同一指标同一级别 24h 内不重复推送；级别升级（黄→红）立即推送；
     *         连续未恢复的预警每 3 天汇总提醒一次；
     * - 推送：经 MessageHelper 写 sys_message（type=2 业务预警，按角色分发）；
     * - 恢复：上次未恢复预警当值回到阈值内时置已恢复并记录恢复时间；
     * - 幂等：按 uk_alert 唯一键 upsert，可安全手动重跑。
     *
     * @param metricDate 预警数据日期（与快照日期一致，T-1）
     * @return 本次触发（新建或级别升级）的预警数
     */
    public int alertMetrics(Date metricDate);

    /**
     * 条件查询预警记录列表（F7 预警历史）
     */
    public List<BiMetricAlert> selectAlertList(BiMetricAlert query);

    /**
     * 决策驾驶舱总览：最新快照指标 + 预警横幅 + 近 N 天趋势
     *
     * 预警规则（规范 6.0 buildAlerts，红/橙/蓝三级）：
     * - 红：快照值越过 threshold_red（threshold_dir=gt 超限告警 / lt 低于告警）；
     * - 橙：data_flag=estimate（估算口径提示）；
     * - 蓝：快照值为 null（无数据提示，前端展示"—"）。
     *
     * @param trendDays 趋势天数（建议 30）
     * @return {snapshotDate, metrics:[...], alerts:[...], trend:[...]}
     */
    public Map<String, Object> getDashboardOverview(int trendDays);

    /**
     * F3 主题看板数据（按域过滤）：本域指标快照 + 预警横幅 + 近 N 天趋势
     *
     * 数据范围与驾驶舱一致（按登录人可见部门过滤快照 dept 维度）。
     *
     * @param domain 域编码（sales/purchase/production/inventory/quality）
     * @param trendDays 趋势天数（建议 30）
     * @return {snapshotDate, domain, metrics:[...], alerts:[...], trend:[...]}
     */
    public Map<String, Object> getBoardData(String domain, int trendDays);

    /**
     * F1.3 历史快照回补（PRD 开放问题 3）：从 T-days 逐日向前补齐快照
     *
     * 复用 snapshotMetrics 的幂等设计（同日先删后插），可安全重跑；
     * 已有数据的日期重跑仅刷新数值，不产生重复行。
     *
     * @param days 回补天数（从昨日往前推，建议 ≤180）
     * @return 成功写入的快照总行数
     */
    public int backfillMetrics(int days);

    /**
     * F6 跨模块价值链视图（PRD v1.2）
     *
     * 基于 22 指标快照组装两条跨模块链路，节点值取最新快照，
     * 对比值取上月最后一个快照日（月粒度对比）：
     * - 销售到现金链：线索转化 → 商机赢单 → 投标中标 → 月签约 → 工单完成 → 月回款；
     * - 供应保障链：到货及时 → 库存周转 → 检验合格 → 工单完成 → 设备效率。
     *
     * @return {snapshotDate, chains:[{code,name,desc,nodes:[...]}]}
     */
    public Map<String, Object> getValueChain();

    /**
     * F8 报表导出行组装：最新快照指标全量行（编码/名称/域/值/单位/口径/基准日）
     */
    public List<com.ruoyi.bi.domain.BiDashboardExport> buildExportRows();

    /**
     * 条件查询指标字典列表（分页）
     */
    public List<BiMetricDict> selectDictList(BiMetricDict biMetricDict);

    /**
     * 根据主键查询字典
     */
    public BiMetricDict selectDictById(Long id);

    /**
     * 新增字典（写入 INSERT 留痕）
     *
     * @return 新增记录 id
     */
    public int insertDict(BiMetricDict biMetricDict);

    /**
     * 修改字典（逐字段 diff 写 UPDATE 留痕）
     */
    public int updateDict(BiMetricDict biMetricDict);

    /**
     * 批量逻辑删除字典（写 DELETE 留痕）
     */
    public int deleteDictByIds(Long[] ids, String updateBy);

    /**
     * 校验指标编码唯一性
     *
     * @return true=可用
     */
    public boolean checkMetricCodeUnique(BiMetricDict biMetricDict);

    /**
     * 查询指标字典变更留痕
     */
    public List<BiMetricDictLog> selectLogByMetricCode(String metricCode);

    /**
     * 条件查询对账日志
     */
    public List<BiMetricCheckLog> selectCheckLogList(BiMetricCheckLog biMetricCheckLog);
}
