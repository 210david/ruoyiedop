package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存流水表 wms_inventory_log
 *
 * @author ruoyi
 */
public class WmsInventoryLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流水ID */
    private Long logId;

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

    /** 仓库名称 */
    @Excel(name = "仓库")
    private String warehouseName;

    /** 库位ID */
    private Long locationId;

    /** 库位编码 */
    @Excel(name = "库位编码")
    private String locationCode;

    /** 库位名称 */
    @Excel(name = "库位")
    private String locationName;

    /** 库区名称 */
    @Excel(name = "库区")
    private String areaName;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 变动类型 */
    @Excel(name = "变动类型", readConverterExp = "0=入库,1=出库,2=移库入,3=移库出,4=盘盈,5=盘亏,6=锁定,7=解锁")
    private String changeType;

    /** 变动数量 */
    @Excel(name = "变动数量")
    private BigDecimal changeQty;

    /** 变动前数量 */
    @Excel(name = "变动前数量")
    private BigDecimal beforeQty;

    /** 变动后数量 */
    @Excel(name = "变动后数量")
    private BigDecimal afterQty;

    /** 关联单据类型 */
    @Excel(name = "单据类型")
    private String refOrderType;

    /** 关联业务单据号 */
    @Excel(name = "关联单据号")
    private String refOrderNo;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public BigDecimal getChangeQty() { return changeQty; }
    public void setChangeQty(BigDecimal changeQty) { this.changeQty = changeQty; }
    public BigDecimal getBeforeQty() { return beforeQty; }
    public void setBeforeQty(BigDecimal beforeQty) { this.beforeQty = beforeQty; }
    public BigDecimal getAfterQty() { return afterQty; }
    public void setAfterQty(BigDecimal afterQty) { this.afterQty = afterQty; }
    public String getRefOrderType() { return refOrderType; }
    public void setRefOrderType(String refOrderType) { this.refOrderType = refOrderType; }
    public String getRefOrderNo() { return refOrderNo; }
    public void setRefOrderNo(String refOrderNo) { this.refOrderNo = refOrderNo; }
}
