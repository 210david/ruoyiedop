package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrEntry extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long entryId;
    @NotBlank(message = "入职单号不能为空")
    @Excel(name = "入职单号")
    private String entryNo;
    @NotBlank(message = "姓名不能为空")
    @Excel(name = "姓名")
    private String employeeName;
    @Excel(name = "身份证号")
    private String idCard;
    @Excel(name = "性别")
    private String gender;
    @Excel(name = "联系电话")
    private String phone;
    @Excel(name = "组织ID")
    private Long orgId;
    @Excel(name = "组织名称")
    private String orgName;
    @Excel(name = "岗位ID")
    private Long positionId;
    @Excel(name = "岗位名称")
    private String positionName;
    @NotNull(message = "入职日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入职日期")
    private Date entryDate;
    @Excel(name = "试用期月数")
    private Integer probationMonths;
    @Excel(name = "试用工资")
    private BigDecimal probationSalary;
    @Excel(name = "转正工资")
    private BigDecimal regularSalary;
    @Excel(name = "合同类型")
    private String contractType;
    @Excel(name = "合同期限")
    private String contractPeriod;
    @Excel(name = "材料清单")
    private String materialsStatus;
    @Excel(name = "岗前体检")
    private String healthCheck;
    @Excel(name = "三级安全教育")
    private String safetyEducation;
    @Excel(name = "审批状态")
    private String auditStatus;
    @Excel(name = "审批人")
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间")
    private Date auditTime;
    @Excel(name = "审批意见")
    private String auditRemark;
    @Excel(name = "员工档案ID")
    private Long employeeId;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }
    public String getEntryNo() { return entryNo; }
    public void setEntryNo(String entryNo) { this.entryNo = entryNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public Date getEntryDate() { return entryDate; }
    public void setEntryDate(Date entryDate) { this.entryDate = entryDate; }
    public Integer getProbationMonths() { return probationMonths; }
    public void setProbationMonths(Integer probationMonths) { this.probationMonths = probationMonths; }
    public BigDecimal getProbationSalary() { return probationSalary; }
    public void setProbationSalary(BigDecimal probationSalary) { this.probationSalary = probationSalary; }
    public BigDecimal getRegularSalary() { return regularSalary; }
    public void setRegularSalary(BigDecimal regularSalary) { this.regularSalary = regularSalary; }
    public String getContractType() { return contractType; }
    public void setContractType(String contractType) { this.contractType = contractType; }
    public String getContractPeriod() { return contractPeriod; }
    public void setContractPeriod(String contractPeriod) { this.contractPeriod = contractPeriod; }
    public String getMaterialsStatus() { return materialsStatus; }
    public void setMaterialsStatus(String materialsStatus) { this.materialsStatus = materialsStatus; }
    public String getHealthCheck() { return healthCheck; }
    public void setHealthCheck(String healthCheck) { this.healthCheck = healthCheck; }
    public String getSafetyEducation() { return safetyEducation; }
    public void setSafetyEducation(String safetyEducation) { this.safetyEducation = safetyEducation; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
