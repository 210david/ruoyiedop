package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 价格政策 mk_price_policy
 *
 * 取价优先级：客户专价 → 等级价 → 无（下单回退手填）
 *
 * @author ruoyi
 */
public class MkPricePolicy extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 政策ID */
    private Long policyId;

    /** 政策编号 */
    @Excel(name = "政策编号")
    private String policyNo;

    /** 政策名称 */
    @Excel(name = "政策名称")
    private String policyName;

    /** 价格类型（1客户专价 2等级价） */
    @Excel(name = "价格类型", dictType = "mk_price_type")
    private String priceType;

    /** 客户ID（客户专价必填） */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 客户等级（等级价必填） */
    @Excel(name = "客户等级", dictType = "marketing_customer_level")
    private String customerLevel;

    /** 物料ID */
    private Long materialId;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 政策单价 */
    @Excel(name = "政策单价")
    private BigDecimal unitPrice;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "失效日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 状态（0启用 1停用） */
    @Excel(name = "状态")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getPolicyNo() { return policyNo; }
    public void setPolicyNo(String policyNo) { this.policyNo = policyNo; }
    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getPriceType() { return priceType; }
    public void setPriceType(String priceType) { this.priceType = priceType; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerLevel() { return customerLevel; }
    public void setCustomerLevel(String customerLevel) { this.customerLevel = customerLevel; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
