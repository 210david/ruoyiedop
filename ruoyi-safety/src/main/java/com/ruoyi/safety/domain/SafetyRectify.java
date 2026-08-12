package com.ruoyi.safety.domain;

import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyRectify extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long rectifyId;
    private Long hazardId;
    @Excel(name = "整改类型")
    private String rectifyType;
    private String rectifyPlan;
    private String solutionDesc;
    private String beforePhotos;
    private String afterPhotos;
    private Long rectifyPersonId;
    @Excel(name = "整改人")
    private String rectifyPerson;
    @Excel(name = "整改时间", width = 160)
    private Date rectifyTime;
    private Long verifyPersonId;
    @Excel(name = "验收人")
    private String verifyPerson;
    @Excel(name = "验收时间", width = 160)
    private Date verifyTime;
    @Excel(name = "验收结果", readConverterExp = "1=通过,2=不通过")
    private String verifyResult;
    private String verifyComment;
    private String delFlag;

    public Long getRectifyId() { return rectifyId; }
    public void setRectifyId(Long rectifyId) { this.rectifyId = rectifyId; }
    public Long getHazardId() { return hazardId; }
    public void setHazardId(Long hazardId) { this.hazardId = hazardId; }
    public String getRectifyType() { return rectifyType; }
    public void setRectifyType(String rectifyType) { this.rectifyType = rectifyType; }
    public String getRectifyPlan() { return rectifyPlan; }
    public void setRectifyPlan(String rectifyPlan) { this.rectifyPlan = rectifyPlan; }
    public String getSolutionDesc() { return solutionDesc; }
    public void setSolutionDesc(String solutionDesc) { this.solutionDesc = solutionDesc; }
    public String getBeforePhotos() { return beforePhotos; }
    public void setBeforePhotos(String beforePhotos) { this.beforePhotos = beforePhotos; }
    public String getAfterPhotos() { return afterPhotos; }
    public void setAfterPhotos(String afterPhotos) { this.afterPhotos = afterPhotos; }
    public Long getRectifyPersonId() { return rectifyPersonId; }
    public void setRectifyPersonId(Long rectifyPersonId) { this.rectifyPersonId = rectifyPersonId; }
    public String getRectifyPerson() { return rectifyPerson; }
    public void setRectifyPerson(String rectifyPerson) { this.rectifyPerson = rectifyPerson; }
    public Date getRectifyTime() { return rectifyTime; }
    public void setRectifyTime(Date rectifyTime) { this.rectifyTime = rectifyTime; }
    public Long getVerifyPersonId() { return verifyPersonId; }
    public void setVerifyPersonId(Long verifyPersonId) { this.verifyPersonId = verifyPersonId; }
    public String getVerifyPerson() { return verifyPerson; }
    public void setVerifyPerson(String verifyPerson) { this.verifyPerson = verifyPerson; }
    public Date getVerifyTime() { return verifyTime; }
    public void setVerifyTime(Date verifyTime) { this.verifyTime = verifyTime; }
    public String getVerifyResult() { return verifyResult; }
    public void setVerifyResult(String verifyResult) { this.verifyResult = verifyResult; }
    public String getVerifyComment() { return verifyComment; }
    public void setVerifyComment(String verifyComment) { this.verifyComment = verifyComment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}