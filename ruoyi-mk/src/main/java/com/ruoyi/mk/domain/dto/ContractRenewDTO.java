package com.ruoyi.mk.domain.dto;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 合同续签请求DTO
 */
public class ContractRenewDTO
{
    /** 新合同名称 */
    private String newContractName;

    /** 新合同金额 */
    private BigDecimal newAmount;

    /** 新生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date newEffectiveDate;

    /** 新到期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date newExpireDate;

    /** 新付款方式 */
    private String newPaymentMethod;

    /** 合同附件 */
    private String attachment;

    /** 备注/续签说明 */
    private String remark;

    public String getNewContractName() { return newContractName; }
    public void setNewContractName(String newContractName) { this.newContractName = newContractName; }
    public BigDecimal getNewAmount() { return newAmount; }
    public void setNewAmount(BigDecimal newAmount) { this.newAmount = newAmount; }
    public Date getNewEffectiveDate() { return newEffectiveDate; }
    public void setNewEffectiveDate(Date newEffectiveDate) { this.newEffectiveDate = newEffectiveDate; }
    public Date getNewExpireDate() { return newExpireDate; }
    public void setNewExpireDate(Date newExpireDate) { this.newExpireDate = newExpireDate; }
    public String getNewPaymentMethod() { return newPaymentMethod; }
    public void setNewPaymentMethod(String newPaymentMethod) { this.newPaymentMethod = newPaymentMethod; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
