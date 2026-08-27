package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrHealthCheck extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long healthId;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @NotBlank(message = "体检类型不能为空")
    @Excel(name = "体检类型")
    private String checkType;
    @NotNull(message = "体检日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "体检日期")
    private Date checkDate;
    @Excel(name = "体检机构")
    private String checkOrg;
    @Excel(name = "职业危害因素")
    private String hazardType;
    @Excel(name = "结论")
    private String checkResult;
    @Excel(name = "体检结论")
    private String conclusion;
    @Excel(name = "附件URL")
    private String attachmentUrl;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "下次体检日期")
    private Date nextCheckDate;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getHealthId() { return healthId; }
    public void setHealthId(Long healthId) { this.healthId = healthId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }
    public Date getCheckDate() { return checkDate; }
    public void setCheckDate(Date checkDate) { this.checkDate = checkDate; }
    public String getCheckOrg() { return checkOrg; }
    public void setCheckOrg(String checkOrg) { this.checkOrg = checkOrg; }
    public String getHazardType() { return hazardType; }
    public void setHazardType(String hazardType) { this.hazardType = hazardType; }
    public String getCheckResult() { return checkResult; }
    public void setCheckResult(String checkResult) { this.checkResult = checkResult; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public Date getNextCheckDate() { return nextCheckDate; }
    public void setNextCheckDate(Date nextCheckDate) { this.nextCheckDate = nextCheckDate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
