package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.List;

/**
 * 合同 mk_contract
 *
 * @author ruoyi
 */
public class MkContract extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 合同ID */
    private Long contractId;

    /** 合同编号 */
    @Excel(name = "合同编号")
    private String contractNo;

    /** 合同名称 */
    @Excel(name = "合同名称")
    private String contractName;

    /** 合同类型 */
    @Excel(name = "合同类型", dictType = "marketing_contract_type")
    private String contractType;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 关联商机ID */
    private Long opportunityId;

    /** 合同金额 */
    @Excel(name = "合同金额")
    private BigDecimal contractAmount;

    /** 签约日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签约日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date signDate;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 到期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "到期日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 付款方式 */
    @Excel(name = "付款方式", dictType = "marketing_payment_method")
    private String paymentMethod;

    /** 合同状态 */
    @Excel(name = "合同状态", dictType = "marketing_contract_status")
    private String contractStatus;

    /** 负责人ID */
    private Long userId;

    /** 负责人名称 */
    @Excel(name = "负责人")
    private String userName;

/** 部门ID */
private Long deptId;

/** 部门名称 */
@Excel(name = "部门")
private String deptName;

/** 合同附件 */
    private String attachment;

    /** 删除标志 */
    private String delFlag;

    /** 回款计划列表 */
    private List<MkPaymentPlan> paymentPlanList;

    /** 审批记录列表 */
    private List<MkContractApproveLog> approveLogList;

    /** 变更记录列表 */
    private List<MkContractChangeLog> changeLogs;

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public String getContractName() { return contractName; }
    public void setContractName(String contractName) { this.contractName = contractName; }
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public Date getSignDate() { return signDate; }
    public void setSignDate(Date signDate) { this.signDate = signDate; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getContractStatus() { return contractStatus; }
    public void setContractStatus(String contractStatus) { this.contractStatus = contractStatus; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
public Long getDeptId() { return deptId; }
public void setDeptId(Long deptId) { this.deptId = deptId; }
public String getDeptName() { return deptName; }
public void setDeptName(String deptName) { this.deptName = deptName; }
public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<MkPaymentPlan> getPaymentPlanList() { return paymentPlanList; }
    public void setPaymentPlanList(List<MkPaymentPlan> paymentPlanList) { this.paymentPlanList = paymentPlanList; }
    public List<MkContractApproveLog> getApproveLogList() { return approveLogList; }
    public void setApproveLogList(List<MkContractApproveLog> approveLogList) { this.approveLogList = approveLogList; }
    public List<MkContractChangeLog> getChangeLogs() { return changeLogs; }
    public void setChangeLogs(List<MkContractChangeLog> changeLogs) { this.changeLogs = changeLogs; }
}
