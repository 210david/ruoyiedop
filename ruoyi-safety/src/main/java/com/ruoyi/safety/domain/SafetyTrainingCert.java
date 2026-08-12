package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 培训证书对象 biz_safety_training_cert
 *
 * @author admin
 */
public class SafetyTrainingCert extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long certId;

    @NotBlank(message = "证书编号不能为空")
    @Excel(name = "证书编号")
    private String certNo;

    @NotBlank(message = "证书名称不能为空")
    @Excel(name = "证书名称")
    private String certName;

    @NotBlank(message = "证书类型不能为空")
    @Excel(name = "证书类型")
    private String certType;

    private Long userId;

    @NotBlank(message = "获证人员不能为空")
    @Excel(name = "获证人员")
    private String userName;

    private Long deptId;

    @Excel(name = "所属部门")
    private String deptName;

    private Long recordId;
    private Long courseId;

    @NotNull(message = "发证日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发证日期", width = 120)
    private Date issueDate;

    @NotNull(message = "有效期至不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "有效期至", width = 120)
    private Date expireDate;

    @NotBlank(message = "发证机构不能为空")
    @Excel(name = "发证机构")
    private String issueOrg;

    private String attachment;
    private String remindStatus;
    private Long enterpriseId;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getCertId() { return certId; }
    public void setCertId(Long certId) { this.certId = certId; }
    public String getCertNo() { return certNo; }
    public void setCertNo(String certNo) { this.certNo = certNo; }
    public String getCertName() { return certName; }
    public void setCertName(String certName) { this.certName = certName; }
    public String getCertType() { return certType; }
    public void setCertType(String certType) { this.certType = certType; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getIssueOrg() { return issueOrg; }
    public void setIssueOrg(String issueOrg) { this.issueOrg = issueOrg; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
