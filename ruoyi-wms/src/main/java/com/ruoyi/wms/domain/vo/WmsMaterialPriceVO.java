package com.ruoyi.wms.domain.vo;

import java.math.BigDecimal;

/**
 * 物料取价依据视图（降级估算用）
 *
 * <p>字段口径：</p>
 * <ul>
 *   <li>avgPrice：入库加权均价 = Σ(上架数量 × 入库单价) / Σ(上架数量)，含税
 *       （与 WmsStatsMapper.materialAvgPrice 保持一致）</li>
 *   <li>costPrice：物料「计价参考成本价」，不含税兜底价</li>
 * </ul>
 *
 * @author ruoyi
 */
public class WmsMaterialPriceVO
{
    /** 物料ID */
    private Long materialId;

    /** 入库加权均价（含税） */
    private BigDecimal avgPrice;

    /** 计价参考成本价（不含税） */
    private BigDecimal costPrice;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
}
