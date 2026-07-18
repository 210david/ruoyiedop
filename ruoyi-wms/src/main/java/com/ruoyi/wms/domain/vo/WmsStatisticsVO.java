package com.ruoyi.wms.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 仓库管理统计报表 VO
 *
 * 用于各类统计查询的通用返回对象，字段按需使用。
 * 继承 BaseEntity 以支持 params 日期范围查询参数。
 *
 * @author ruoyi
 */
public class WmsStatisticsVO extends BaseEntity
{
    /** 分类名称（仓库名/物料名/类型名/日期等） */
    private String name;

    /** 分类编码 */
    private String code;

    /** 数量 */
    private BigDecimal qty;

    /** 锁定数量 */
    private BigDecimal lockQty;

    /** 总数量（可用+锁定） */
    private BigDecimal totalQty;

    /** 计数 */
    private Integer count;

    /** 入库数量 */
    private BigDecimal inboundQty;

    /** 出库数量 */
    private BigDecimal outboundQty;

    /** 金额 */
    private BigDecimal amount;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 规格型号 */
    private String specModel;

    /** 单位 */
    private String unit;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 物料类型 */
    private String materialType;

    /** 安全库存下限 */
    private BigDecimal safetyStockMin;

    /** 安全库存上限 */
    private BigDecimal safetyStockMax;

    /** 日期（趋势统计用） */
    private String date;

    /** 类型（入库类型/出库类型） */
    private String orderType;

    /** 预警类型（low_stock=库存不足, overstock=库存积压, near_expiry=临期预警, expired=过期预警） */
    @Excel(name = "预警类型", readConverterExp = "low_stock=库存不足,overstock=库存积压,near_expiry=临期预警,expired=过期预警")
    private String alertType;

    /** 生产日期（效期预警用） */
    @Excel(name = "生产日期")
    private Date productionDate;

    /** 有效期（效期预警用） */
    @Excel(name = "有效期")
    private Date expiryDate;

    /** 距过期天数（效期预警用） */
    @Excel(name = "距过期天数")
    private Integer daysUntilExpiry;

    /** 临期预警天数（物料主数据） */
    private Integer expiryAlertDays;

    /** 是否效期管理（0否 1是） */
    private String isExpiryManage;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public BigDecimal getLockQty() { return lockQty; }
    public void setLockQty(BigDecimal lockQty) { this.lockQty = lockQty; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public BigDecimal getInboundQty() { return inboundQty; }
    public void setInboundQty(BigDecimal inboundQty) { this.inboundQty = inboundQty; }
    public BigDecimal getOutboundQty() { return outboundQty; }
    public void setOutboundQty(BigDecimal outboundQty) { this.outboundQty = outboundQty; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public BigDecimal getSafetyStockMin() { return safetyStockMin; }
    public void setSafetyStockMin(BigDecimal safetyStockMin) { this.safetyStockMin = safetyStockMin; }
    public BigDecimal getSafetyStockMax() { return safetyStockMax; }
    public void setSafetyStockMax(BigDecimal safetyStockMax) { this.safetyStockMax = safetyStockMax; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public Date getProductionDate() { return productionDate; }
    public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public Integer getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(Integer daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }
    public Integer getExpiryAlertDays() { return expiryAlertDays; }
    public void setExpiryAlertDays(Integer expiryAlertDays) { this.expiryAlertDays = expiryAlertDays; }
    public String getIsExpiryManage() { return isExpiryManage; }
    public void setIsExpiryManage(String isExpiryManage) { this.isExpiryManage = isExpiryManage; }
}
