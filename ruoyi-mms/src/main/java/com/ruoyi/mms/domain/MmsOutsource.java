package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外协管理 mms_outsource
 *
 * @author ruoyi
 */
public class MmsOutsource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long outsourceId;

    @Excel(name = "外协单号")
    private String outsourceNo;

    private Long workOrderId;

    @Excel(name = "工单编号")
    private String workOrderNo;

    @Excel(name = "工序顺序号")
    private Integer opSeq;

    @Excel(name = "工序名称")
    private String processName;

    private Long supplierId;

    @Excel(name = "供应商")
    private String supplierName;

    @Excel(name = "外协数量")
    private BigDecimal outQty;

    @Excel(name = "单价")
    private BigDecimal unitPrice;

    @Excel(name = "总金额")
    private BigDecimal totalAmount;

    @Excel(name = "外协日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date outDate;

    @Excel(name = "计划回厂", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planBackDate;

    @Excel(name = "实际回厂", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualBackDate;

    @Excel(name = "回厂数量")
    private BigDecimal backQty;

    @Excel(name = "损耗数量")
    private BigDecimal lossQty;

    @Excel(name = "状态", readConverterExp = "0=已外协,1=已回厂,2=已结算")
    private String status;

    private String delFlag;

    public Long getOutsourceId() { return outsourceId; }
    public void setOutsourceId(Long outsourceId) { this.outsourceId = outsourceId; }
    public String getOutsourceNo() { return outsourceNo; }
    public void setOutsourceNo(String outsourceNo) { this.outsourceNo = outsourceNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Integer getOpSeq() { return opSeq; }
    public void setOpSeq(Integer opSeq) { this.opSeq = opSeq; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getOutQty() { return outQty; }
    public void setOutQty(BigDecimal outQty) { this.outQty = outQty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Date getOutDate() { return outDate; }
    public void setOutDate(Date outDate) { this.outDate = outDate; }
    public Date getPlanBackDate() { return planBackDate; }
    public void setPlanBackDate(Date planBackDate) { this.planBackDate = planBackDate; }
    public Date getActualBackDate() { return actualBackDate; }
    public void setActualBackDate(Date actualBackDate) { this.actualBackDate = actualBackDate; }
    public BigDecimal getBackQty() { return backQty; }
    public void setBackQty(BigDecimal backQty) { this.backQty = backQty; }
    public BigDecimal getLossQty() { return lossQty; }
    public void setLossQty(BigDecimal lossQty) { this.lossQty = lossQty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
