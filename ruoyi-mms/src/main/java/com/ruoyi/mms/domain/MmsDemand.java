package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 生产需求 mms_demand
 *
 * @author ruoyi
 */
public class MmsDemand extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long demandId;

    @Excel(name = "需求单号")
    private String demandNo;

    @Excel(name = "需求类型", readConverterExp = "0=订单,1=预测,2=补库,3=插单")
    private String demandType;

    @Excel(name = "来源订单号")
    private String sourceOrderNo;

    private Long productId;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "需求数量")
    private BigDecimal demandQty;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "需求交期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date requireDate;

    @Excel(name = "优先级", readConverterExp = "0=高,1=中,2=低")
    private String priority;

    private Long customerId;

    @Excel(name = "客户编号")
    private String customerNo;

    @Excel(name = "客户名称")
    private String customerName;

    @Excel(name = "状态", readConverterExp = "0=草稿,1=已确认,2=已排产,3=已取消")
    private String status;

    private String delFlag;

    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
    public String getDemandNo() { return demandNo; }
    public void setDemandNo(String demandNo) { this.demandNo = demandNo; }
    public String getDemandType() { return demandType; }
    public void setDemandType(String demandType) { this.demandType = demandType; }
    public String getSourceOrderNo() { return sourceOrderNo; }
    public void setSourceOrderNo(String sourceOrderNo) { this.sourceOrderNo = sourceOrderNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getDemandQty() { return demandQty; }
    public void setDemandQty(BigDecimal demandQty) { this.demandQty = demandQty; }
    public Date getRequireDate() { return requireDate; }
    public void setRequireDate(Date requireDate) { this.requireDate = requireDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
