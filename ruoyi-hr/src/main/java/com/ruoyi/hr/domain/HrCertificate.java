package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrCertificate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long certId;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @NotBlank(message = "证书类别不能为空")
    @Excel(name = "证书类别")
    private String certType;
    @NotBlank(message = "证书名称不能为空")
    @Excel(name = "证书名称")
    private String certName;
    @Excel(name = "证书编号")
    private String certNo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发证日期")
    private Date issueDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效起始日")
    private Date validFrom;
    @NotNull(message = "有效期至不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至")
    private Date validTo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "复审日期")
    private Date reviewDate;
    @Excel(name = "发证机构")
    private String issueOrg;
    @Excel(name = "附件URL")
    private String attachmentUrl;
    @Excel(name = "预警状态")
    private String remindStatus;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getCertId() { return certId; }
    public void setCertId(Long certId) { this.certId = certId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getCertType() { return certType; }
    public void setCertType(String certType) { this.certType = certType; }
    public String getCertName() { return certName; }
    public void setCertName(String certName) { this.certName = certName; }
    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public Date getValidFrom() { return validFrom; }
    public void setValidFrom(Date validFrom) { this.validFrom = validFrom; }
    public Date getValidTo() { return validTo; }
    public void setValidTo(Date validTo) { this.validTo = validTo; }
    public Date getReviewDate() { return reviewDate; }
    public void setReviewDate(Date reviewDate) { this.reviewDate = reviewDate; }
    public String getIssueOrg() { return issueOrg; }
    public void setIssueOrg(String issueOrg) { this.issueOrg = issueOrg; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
