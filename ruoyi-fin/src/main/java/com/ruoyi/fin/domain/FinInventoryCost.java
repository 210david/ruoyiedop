package com.ruoyi.fin.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 存货成本台账 fin_inventory_cost
 *
 * <p>【Q3 决策 · 旁路设计】本表独立维护存货成本，**不修改 wms_inventory**。
 * WMS 只管数量，财务只管金额，两者通过 material_id + warehouse_id +
 * location_id + batch_no 四维对齐。</p>
 *
 * <p>【Q4 决策】unit_cost 存**不含税**单位成本（4 位小数），
 * 由 FinCostEngine 从含税入库单价除税得到。</p>
 *
 * <p>【并发】version 乐观锁 + Mapper 层 FOR UPDATE 行锁双保险，
 * 照抄工程既有的 wms_inventory 控制方式。</p>
 *
 * @author ruoyi
 */
public class FinInventoryCost extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计价方法：加权平均 */
    public static final String METHOD_WEIGHTED_AVG = "WEIGHTED_AVG";

    /** 计价方法：先进先出 */
    public static final String METHOD_FIFO = "FIFO";

    /** 成本记录ID */
    private Long costId;

    /** 预留账套ID */
    private Long companyId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次号（非空，空串参与唯一索引） */
    private String batchNo;

    /** 结存数量 */
    @Excel(name = "结存数量")
    private BigDecimal qty;

    /** 不含税单位成本（4 位小数） */
    @Excel(name = "单位成本")
    private BigDecimal unitCost;

    /** 结存成本金额 */
    @Excel(name = "结存金额")
    private BigDecimal totalCost;

    /** 计价方法 */
    private String costMethod;

    /** 来源模块 wms/pms */
    private String sourceModule;

    /** 来源单据类型 inbound/purchase_return */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 入库单ID（反查成本来源） */
    private Long inboundOrderId;

    /** 最近入库日期 */
    private Date lastInDate;

    /** 乐观锁版本号 */
    private Integer version;

    public Long getCostId() { return costId; }
    public void setCostId(Long costId) { this.costId = costId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

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

    public String getCostMethod() { return costMethod; }
    public void setCostMethod(String costMethod) { this.costMethod = costMethod; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public Long getInboundOrderId() { return inboundOrderId; }
    public void setInboundOrderId(Long inboundOrderId) { this.inboundOrderId = inboundOrderId; }

    public Date getLastInDate() { return lastInDate; }
    public void setLastInDate(Date lastInDate) { this.lastInDate = lastInDate; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}