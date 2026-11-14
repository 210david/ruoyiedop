package com.ruoyi.kingdee.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 金蝶采购入库单（一行 = 一张单据的一条商品分录）
 */
public class KdPurInStock extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String kdBillId;
    private String kdEntryId;
    private String billDate;
    private String billNo;
    private String supplierName;
    private String purchaserName;
    private String deptName;
    private String payStatus;
    private BigDecimal unpaidAmount;
    private BigDecimal paidAmount;
    private String materialName;
    private String warehouseName;
    private String unitName;
    private BigDecimal qty;
    private BigDecimal price;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal returnQty;
    private Date syncTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getKdBillId() { return kdBillId; }
    public void setKdBillId(String kdBillId) { this.kdBillId = kdBillId; }
    public String getKdEntryId() { return kdEntryId; }
    public void setKdEntryId(String kdEntryId) { this.kdEntryId = kdEntryId; }
    public String getBillDate() { return billDate; }
    public void setBillDate(String billDate) { this.billDate = billDate; }
    public String getBillNo() { return billNo; }
    public void setBillNo(String billNo) { this.billNo = billNo; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getPurchaserName() { return purchaserName; }
    public void setPurchaserName(String purchaserName) { this.purchaserName = purchaserName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }
    public BigDecimal getUnpaidAmount() { return unpaidAmount; }
    public void setUnpaidAmount(BigDecimal unpaidAmount) { this.unpaidAmount = unpaidAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getReturnQty() { return returnQty; }
    public void setReturnQty(BigDecimal returnQty) { this.returnQty = returnQty; }
    public Date getSyncTime() { return syncTime; }
    public void setSyncTime(Date syncTime) { this.syncTime = syncTime; }
}
