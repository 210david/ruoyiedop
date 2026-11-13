package com.ruoyi.wms.mapper;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.ibatis.annotations.Param;

/**
 * WMS 决策支持统计 Mapper 接口（供 WmsStatsProvider 使用）
 *
 * 新增专用查询，不改动现有 WmsStatisticsMapper（遵循"扩展现有实现、声明改动范围"约定）。
 *
 * 取价口径（已确认 v1）：物料入库均价 = Σ(入库上架数量 × 入库单价) / Σ(入库上架数量)，
 * 来源 wms_inbound_order_detail（putaway_qty × unit_price）。
 *
 * @author ruoyi
 */
public interface WmsStatsMapper
{
    /**
     * 区间出库成本（按入库单价取价）
     *
     * @param beginDate 统计起始日（月频指标取当月 1 日）
     * @param endDate   统计截止日（含当日）
     */
    public BigDecimal selectOutboundCost(@Param("beginDate") Date beginDate, @Param("endDate") Date endDate);

    /**
     * 当前库存金额（库存数量 × 物料入库均价，时点值）
     */
    public BigDecimal selectInventoryAmount();

    /**
     * 呆滞库存金额：超 90 天无出库记录的库存金额（不含委外/在制）【已确认 v1】
     *
     * @param metricDate 统计基准日
     */
    public BigDecimal selectSlowStockAmount(@Param("metricDate") Date metricDate);
}
