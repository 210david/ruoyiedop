package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyEmergencyPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long planId;
    @Excel(name = "预案编号")
    private String planCode;
    @Excel(name = "预案名称")
    private String planName;
    @Excel(name = "预案类型", readConverterExp = "1=综合预案,2=专项预案,3=现场处置方案")
    private String planType;
    @Excel(name = "预案级别", readConverterExp = "1=Ⅰ级（特别重大）,2=Ⅱ级（重大）,3=Ⅲ级（较大）,4=Ⅳ级（一般）")
    private String planLevel;
    @Excel(name = "版本号")
    private String version;
    @Excel(name = "备案号")
    private String regNo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "备案日期", width = 120)
    private Date regDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", width = 120)
    private Date effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "修订到期", width = 120)
    private Date expireDate;
    private String remindStatus;
    private String content;
    private String attachment;
    private Long enterpriseId;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public String getPlanLevel() { return planLevel; }
    public void setPlanLevel(String planLevel) { this.planLevel = planLevel; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public Date getRegDate() { return regDate; }
    public void setRegDate(Date regDate) { this.regDate = regDate; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}