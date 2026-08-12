package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyWorker extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long workerId;
    @Excel(name = "姓名")
    private String workerName;
    @Excel(name = "工号")
    private String workerNo;
    @NotNull(message = "所属部门不能为空")
    private Long deptId;
    @NotBlank(message = "所属部门不能为空")
    @Excel(name = "所属部门")
    private String deptName;
    @Excel(name = "岗位")
    private String position;
    @Excel(name = "联系电话")
    private String phone;
    @Excel(name = "特种人员", readConverterExp = "0=否,1=是")
    private String isSpecial;
    @NotBlank(message = "作业类别不能为空")
    @Excel(name = "作业类别")
    private String jobType;
    @NotBlank(message = "作业证号不能为空")
    @Excel(name = "证号")
    private String certNo;
    @NotNull(message = "发证日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发证日期", width = 120)
    private Date certIssueDate;
    @NotNull(message = "有效期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至", width = 120)
    private Date validTo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "复审日期", width = 120)
    private Date reviewDate;
    private String remindStatus;
    private Long enterpriseId;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getWorkerNo() { return workerNo; }
    public void setWorkerNo(String workerNo) { this.workerNo = workerNo; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getIsSpecial() { return isSpecial; }
    public void setIsSpecial(String isSpecial) { this.isSpecial = isSpecial; }
    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }
    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
    public Date getCertIssueDate() { return certIssueDate; }
    public void setCertIssueDate(Date certIssueDate) { this.certIssueDate = certIssueDate; }
    public Date getValidTo() { return validTo; }
    public void setValidTo(Date validTo) { this.validTo = validTo; }
    public Date getReviewDate() { return reviewDate; }
    public void setReviewDate(Date reviewDate) { this.reviewDate = reviewDate; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}