package com.ruoyi.hr.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * HR 统计扩展 Mapper（决策支持指标专用）
 *
 * 人工成本占比（HR_COST_RATIO）分母 = 当月完工入库产值（口径已确认 v1）。
 * 产值数据源为 MMS/WMS 模块表，单库单体下以 SQL 联查实现（零 Maven 依赖，无架构耦合）；
 * 若后续拆分服务，该查询应迁移至 BI 快照层。
 *
 * @author ruoyi
 */
public interface HrStatsMapper
{
    /**
     * 查询指定月份区间的完工入库产值
     *
     * 口径：Σ(完工入库单合格数量 × 物料参考单价)
     * 物料参考单价 = 该物料全部入库明细的平均入库单价（wms_inbound_order_detail）
     * 仅统计已入库（status=1）且物料在库有价记录的完工单行
     *
     * @param start 月起始（含）
     * @param end   月结束（不含）
     * @return 产值（元），无数据返回 null
     */
    BigDecimal selectFinishOutputValue(@Param("start") Date start, @Param("end") Date end);

    /**
     * 查询指定年月已核算工资条的实发工资合计
     *
     * @param yearMonth 年月（yyyy-MM）
     * @return 实发工资合计（元），无数据返回 null
     */
    BigDecimal selectNetSalaryTotal(@Param("yearMonth") String yearMonth);

    /**
     * 员工状态分布计数
     *
     * @return [{status, cnt}]
     */
    List<Map<String, Object>> selectEmployeeStatusCount();
}
