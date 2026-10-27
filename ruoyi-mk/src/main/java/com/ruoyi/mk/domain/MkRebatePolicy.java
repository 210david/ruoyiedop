package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 返利政策（阶梯组） mk_rebate_policy
 * 同 policy_no 多行 = 一个阶梯组
 *
 * @author ruoyi
 */
public class MkRebatePolicy extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 阶梯ID */
    private Long policyId;

    /** 政策编号（同编号多行=阶梯组） */
    @Excel(name = "政策编号")
    private String policyNo;

    /** 政策名称 */
    @Excel(name = "政策名称")
    private String policyName;

    /** 客户ID（空=全部客户） */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 考核口径（1金额 2数量） */
    @Excel(name = "考核口径", dictType = "mk_rebate_basis")
    private String basis;

    /** 周期类型（1月 2季 3年） */
    @Excel(name = "周期类型", dictType = "mk_rebate_period")
    private String periodType;

    /** 阶梯下限（含） */
    @Excel(name = "阶梯下限")
    private BigDecimal tierMin;

    /** 阶梯上限（含，空=不封顶） */
    @Excel(name = "阶梯上限")
    private BigDecimal tierMax;

    /** 返利比例% */
    @Excel(name = "返利比例%")
    private BigDecimal rebateRate;

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
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getBasis() { return basis; }
    public void setBasis(String basis) { this.basis = basis; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    public BigDecimal getTierMin() { return tierMin; }
    public void setTierMin(BigDecimal tierMin) { this.tierMin = tierMin; }
    public BigDecimal getTierMax() { return tierMax; }
    public void setTierMax(BigDecimal tierMax) { this.tierMax = tierMax; }
    public BigDecimal getRebateRate() { return rebateRate; }
    public void setRebateRate(BigDecimal rebateRate) { this.rebateRate = rebateRate; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
