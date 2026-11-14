package com.ruoyi.kingdee.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 金蝶销售出库单同步数据（单据表头 + 商品分录合成一行）
 *
 * @author ruoyi
 */
public class KdSalOutbound extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 金蝶单据ID */
    private String kdBillId;

    /** 金蝶分录ID */
    private String kdEntryId;

    /** 单据日期 */
    private String billDate;

    /** 单据编号 */
    private String billNo;

    /** 客户 */
    private String customerName;

    /** 业务员 */
    private String salesmanName;

    /** 部门 */
    private String deptName;

    /** 收款状态 */
    private String receiptStatus;

    /** 未收款金额 */
    private BigDecimal unpaidAmount;

    /** 已收款金额 */
    private BigDecimal paidAmount;

    /** 商品名称 */
    private String materialName;

    /** 仓库 */
    private String warehouseName;

    /** 单位 */
    private String unitName;

    /** 数量 */
    private BigDecimal qty;

    /** 单价 */
    private BigDecimal price;

    /** 税额 */
    private BigDecimal taxAmount;

    /** 价税合计 */
    private BigDecimal totalAmount;

    /** 退货数量 */
    private BigDecimal returnQty;

    /** 同步时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date syncTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getKdBillId()
    {
        return kdBillId;
    }

    public void setKdBillId(String kdBillId)
    {
        this.kdBillId = kdBillId;
    }

    public String getKdEntryId()
    {
        return kdEntryId;
    }

    public void setKdEntryId(String kdEntryId)
    {
        this.kdEntryId = kdEntryId;
    }

    public String getBillDate()
    {
        return billDate;
    }

    public void setBillDate(String billDate)
    {
        this.billDate = billDate;
    }

    public String getBillNo()
    {
        return billNo;
    }

    public void setBillNo(String billNo)
    {
        this.billNo = billNo;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getSalesmanName()
    {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName)
    {
        this.salesmanName = salesmanName;
    }

    public String getDeptName()
    {
        return deptName;
    }

    public void setDeptName(String deptName)
    {
        this.deptName = deptName;
    }

    public String getReceiptStatus()
    {
        return receiptStatus;
    }

    public void setReceiptStatus(String receiptStatus)
    {
        this.receiptStatus = receiptStatus;
    }

    public BigDecimal getUnpaidAmount()
    {
        return unpaidAmount;
    }

    public void setUnpaidAmount(BigDecimal unpaidAmount)
    {
        this.unpaidAmount = unpaidAmount;
    }

    public BigDecimal getPaidAmount()
    {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount)
    {
        this.paidAmount = paidAmount;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getWarehouseName()
    {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName)
    {
        this.warehouseName = warehouseName;
    }

    public String getUnitName()
    {
        return unitName;
    }

    public void setUnitName(String unitName)
    {
        this.unitName = unitName;
    }

    public BigDecimal getQty()
    {
        return qty;
    }

    public void setQty(BigDecimal qty)
    {
        this.qty = qty;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getTaxAmount()
    {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount)
    {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReturnQty()
    {
        return returnQty;
    }

    public void setReturnQty(BigDecimal returnQty)
    {
        this.returnQty = returnQty;
    }

    public Date getSyncTime()
    {
        return syncTime;
    }

    public void setSyncTime(Date syncTime)
    {
        this.syncTime = syncTime;
    }
}
