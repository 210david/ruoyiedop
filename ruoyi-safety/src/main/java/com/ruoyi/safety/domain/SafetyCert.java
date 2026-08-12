package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyCert extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long certId;
    @Excel(name = "证照名称")
    private String certName;
    @NotBlank(message = "证照编号不能为空")
    @Excel(name = "证照编号")
    private String certNo;
    @Excel(name = "证照类型")
    private String certType;
    @Excel(name = "发证机关")
    private String issueOrg;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发证日期", width = 120)
    private Date issueDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至", width = 120)
    private Date expireDate;
    private String remindStatus;
    private Long enterpriseId;
    private String attachment;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getCertId() { return certId; }
    public void setCertId(Long certId) { this.certId = certId; }
    public String getCertName() { return certName; }
    public void setCertName(String certName) { this.certName = certName; }
    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
    public String getCertType() { return certType; }
    public void setCertType(String certType) { this.certType = certType; }
    public String getIssueOrg() { return issueOrg; }
    public void setIssueOrg(String issueOrg) { this.issueOrg = issueOrg; }
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}