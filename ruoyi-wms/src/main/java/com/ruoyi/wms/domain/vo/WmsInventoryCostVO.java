package com.ruoyi.wms.domain.vo;

import java.math.BigDecimal;

/**
 * 库存成本台账只读视图（对应财务 fin_inventory_cost）
 *
 * <p>WMS 只读引用，不持有财务模块依赖。用于把「四维键 + 结存金额」批量带回到库存列表，
 * 避免逐行查询造成 N+1。</p>
 *
 * @author ruoyi
 */
public class WmsInventoryCostVO
{
    /** 物料ID */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 结存数量 */
    private BigDecimal qty;

    /** 不含税单位成本（台账为 4 位小数） */
    private BigDecimal unitCost;

    /** 结存成本金额 */
    private BigDecimal totalCost;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    /**
     * 业务主键：物料+仓库+库位+批次（与台账唯一索引口径一致，批次空串参与比较）
     */
    public String dimensionKey()
    {
        return dimensionKey(materialId, warehouseId, locationId, batchNo);
    }

    /**
     * 统一构造四维键，供取数侧与匹配侧共用，避免两处拼接规则漂移。
     */
    public static String dimensionKey(Long materialId, Long warehouseId, Long locationId, String batchNo)
    {
        return (materialId == null ? 0L : materialId) + "|"
                + (warehouseId == null ? 0L : warehouseId) + "|"
                + (locationId == null ? 0L : locationId) + "|"
                + (batchNo == null ? "" : batchNo);
    }
}
