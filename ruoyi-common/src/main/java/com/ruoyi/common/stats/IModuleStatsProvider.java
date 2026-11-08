package com.ruoyi.common.stats;

import java.util.List;

/**
 * 模块统计指标提供者接口（决策支持模块统一接入规范）
 *
 * 约定（依据 doc/plans/PRD-决策支持模块-功能需求方案.md v1.2 F1.2）：
 * 1. 接口定义在 ruoyi-common，各业务模块提供实现；
 * 2. 决策支持模块（ruoyi-bi）通过 Spring 注入 List&lt;IModuleStatsProvider&gt; 收集全部实现，
 *    bi 模块 Maven 层不依赖任何业务模块；
 * 3. 实现类应基于本模块现有 DashboardService/StatisticsService 扩展，复用既有取数逻辑，
 *    禁止新建平行统计实现（统一开发规范 2.8）；
 * 4. 快照任务（biMetricSnapshotTask）每日按指标日期逐指标调用 getMetric，
 *    实现必须保证幂等可重跑、不得抛出异常中断整批快照（无数据返回 value=null）。
 *
 * @author ruoyi
 */
public interface IModuleStatsProvider
{
    /**
     * 模块标识（mk / pms / mms / qms / wms / dms / safety / hr / bid ...）
     */
    String getModuleCode();

    /**
     * 本模块支持的指标编码集合（对应 bi_metric_dict.metric_code）
     */
    List<String> supportedMetricCodes();

    /**
     * 查询指定日期、指定部门维度的指标值（实时计算）
     *
     * @param metricCode 指标编码
     * @param metricDate 指标日期（快照粒度：日；月度指标由实现按所属月份聚合）
     * @param deptId     部门维度（sys_dept.dept_id），null 表示全部门汇总
     * @return 指标值；无数据时返回 value 为 null 的对象，不得抛出异常
     */
    MetricValue getMetric(String metricCode, java.util.Date metricDate, Long deptId);
}
