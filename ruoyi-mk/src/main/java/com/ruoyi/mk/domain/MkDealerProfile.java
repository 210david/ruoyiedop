package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 经销商扩展档案 mk_dealer_profile
 *
 * @author ruoyi
 */
public class MkDealerProfile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 档案ID */
    private Long profileId;

    /** 客户ID（经销商客户） */
    private Long customerId;

    /** 经销层级（1一级经销商 2二级经销商） */
    @Excel(name = "经销层级", dictType = "mk_dealer_level")
    private String dealerLevel;

    /** 授权区域（省市区串） */
    @Excel(name = "授权区域")
    private String authRegions;

    /** 授权品类（物料分类ID串） */
    @Excel(name = "授权品类")
    private String authCategories;

    /** 保证金（仅档案金额） */
    @Excel(name = "保证金")
    private BigDecimal depositAmount;

    /** 合作开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合作开始日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date coopStartDate;

    /** 合作结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "合作结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date coopEndDate;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", dictType = "mk_dealer_profile_status")
    private String status;

    /** 客户编号（关联字段） */
    private String customerNo;

    /** 客户名称（关联字段） */
    private String customerName;

    /** 上级经销商ID（关联字段） */
    private Long parentCustomerId;

    /** 上级经销商名称（关联字段） */
    private String parentCustomerName;

    /** 负责人（关联字段） */
    private String userName;

    /** 部门（关联字段） */
    private String deptName;

    /** 信用状态（关联字段：0正常 1冻结） */
    private String creditStatus;

    /** 本期采购额（关联字段：当月有效订单合计） */
    private BigDecimal periodPurchaseAmount;

    /** 未结返利（关联字段：未全部冲抵的返利余额） */
    private BigDecimal unsettledRebate;

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getDealerLevel() { return dealerLevel; }
    public void setDealerLevel(String dealerLevel) { this.dealerLevel = dealerLevel; }
    public String getAuthRegions() { return authRegions; }
    public void setAuthRegions(String authRegions) { this.authRegions = authRegions; }
    public String getAuthCategories() { return authCategories; }
    public void setAuthCategories(String authCategories) { this.authCategories = authCategories; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    public Date getCoopStartDate() { return coopStartDate; }
    public void setCoopStartDate(Date coopStartDate) { this.coopStartDate = coopStartDate; }
    public Date getCoopEndDate() { return coopEndDate; }
    public void setCoopEndDate(Date coopEndDate) { this.coopEndDate = coopEndDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getParentCustomerId() { return parentCustomerId; }
    public void setParentCustomerId(Long parentCustomerId) { this.parentCustomerId = parentCustomerId; }
    public String getParentCustomerName() { return parentCustomerName; }
    public void setParentCustomerName(String parentCustomerName) { this.parentCustomerName = parentCustomerName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getCreditStatus() { return creditStatus; }
    public void setCreditStatus(String creditStatus) { this.creditStatus = creditStatus; }
    public BigDecimal getPeriodPurchaseAmount() { return periodPurchaseAmount; }
    public void setPeriodPurchaseAmount(BigDecimal periodPurchaseAmount) { this.periodPurchaseAmount = periodPurchaseAmount; }
    public BigDecimal getUnsettledRebate() { return unsettledRebate; }
    public void setUnsettledRebate(BigDecimal unsettledRebate) { this.unsettledRebate = unsettledRebate; }
}
