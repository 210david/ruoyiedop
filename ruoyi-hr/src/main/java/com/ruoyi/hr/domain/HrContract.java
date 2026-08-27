package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrContract extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long contractId;
    @NotBlank(message = "合同编号不能为空")
    @Excel(name = "合同编号")
    private String contractNo;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "合同类型")
    private String contractType;
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始日期")
    private Date startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束日期")
    private Date endDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "签订日期")
    private Date signDate;
    @Excel(name = "试用期月数")
    private Integer probationMonths;
    @Excel(name = "试用期工资")
    private BigDecimal probationSalary;
    @Excel(name = "转正工资")
    private BigDecimal regularSalary;
    @Excel(name = "续签次数")
    private Integer renewCount;
    @Excel(name = "预警状态")
    private String remindStatus;
    @Excel(name = "附件URL")
    private String attachmentUrl;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Date getSignDate() { return signDate; }
    public void setSignDate(Date signDate) { this.signDate = signDate; }
    public Integer getProbationMonths() { return probationMonths; }
    public void setProbationMonths(Integer probationMonths) { this.probationMonths = probationMonths; }
    public BigDecimal getProbationSalary() { return probationSalary; }
    public void setProbationSalary(BigDecimal probationSalary) { this.probationSalary = probationSalary; }
    public BigDecimal getRegularSalary() { return regularSalary; }
    public void setRegularSalary(BigDecimal regularSalary) { this.regularSalary = regularSalary; }
    public Integer getRenewCount() { return renewCount; }
    public void setRenewCount(Integer renewCount) { this.renewCount = renewCount; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
