package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrEmployeeQualification extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long qualificationId;
    @NotNull(message = "员工ID不能为空") @Excel(name = "员工ID")
    private Long employeeId;

    @Excel(name = "类型")
    private String qualType;

    @Excel(name = "名称")
    private String qualName;

    @Excel(name = "证书编号")
    private String qualNo;

    @Excel(name = "发证机构")
    private String issueOrg;

    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "发证日期")
    private Date issueDate;

    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "有效起始日")
    private Date validFrom;

    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "有效期至")
    private Date validTo;

    @Excel(name = "成绩/等级")
    private String score;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "删除标志")
    private String delFlag;


    public Long getQualificationId() { return qualificationId; }
    public void setQualificationId(Long qualificationId) { this.qualificationId = qualificationId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getQualType() { return qualType; }
    public void setQualType(String qualType) { this.qualType = qualType; }
    public String getQualName() { return qualName; }
    public void setQualName(String qualName) { this.qualName = qualName; }
    public String getQualNo() { return qualNo; }
    public void setQualNo(String qualNo) { this.qualNo = qualNo; }
    public String getIssueOrg() { return issueOrg; }
    public void setIssueOrg(String issueOrg) { this.issueOrg = issueOrg; }
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public Date getValidFrom() { return validFrom; }
    public void setValidFrom(Date validFrom) { this.validFrom = validFrom; }
    public Date getValidTo() { return validTo; }
    public void setValidTo(Date validTo) { this.validTo = validTo; }
    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
