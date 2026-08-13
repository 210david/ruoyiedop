package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsCapa extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long capaId;
    @Excel(name = "CAPA编号") private String capaNo;
    @Excel(name = "来源类型") private String sourceType;
    @Excel(name = "来源编号") private String sourceNo;
    @Excel(name = "问题标题") private String title;
    private String problemDesc;
    private String teamMembers;
    private String interimAction;
    private String rootCause;
    private String permanentAction;
    private String verifyResult;
    private String preventAction;
    private String closeRemark;
    @Excel(name = "状态") private String capaStatus;
    @Excel(name = "责任人") private String responsiblePerson;
    private Long responsiblePersonId;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date planCloseTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date closeTime;
    private String delFlag;
    private String status;

    public Long getCapaId() { return capaId; }
    public void setCapaId(Long capaId) { this.capaId = capaId; }
    public String getCapaNo() { return capaNo; }
    public void setCapaNo(String capaNo) { this.capaNo = capaNo; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String problemDesc) { this.problemDesc = problemDesc; }
    public String getTeamMembers() { return teamMembers; }
    public void setTeamMembers(String teamMembers) { this.teamMembers = teamMembers; }
    public String getInterimAction() { return interimAction; }
    public void setInterimAction(String interimAction) { this.interimAction = interimAction; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public String getPermanentAction() { return permanentAction; }
    public void setPermanentAction(String permanentAction) { this.permanentAction = permanentAction; }
    public String getVerifyResult() { return verifyResult; }
    public void setVerifyResult(String verifyResult) { this.verifyResult = verifyResult; }
    public String getPreventAction() { return preventAction; }
    public void setPreventAction(String preventAction) { this.preventAction = preventAction; }
    public String getCloseRemark() { return closeRemark; }
    public void setCloseRemark(String closeRemark) { this.closeRemark = closeRemark; }
    public String getCapaStatus() { return capaStatus; }
    public void setCapaStatus(String capaStatus) { this.capaStatus = capaStatus; }
    public String getResponsiblePerson() { return responsiblePerson; }
    public void setResponsiblePerson(String responsiblePerson) { this.responsiblePerson = responsiblePerson; }
    public Long getResponsiblePersonId() { return responsiblePersonId; }
    public void setResponsiblePersonId(Long responsiblePersonId) { this.responsiblePersonId = responsiblePersonId; }
    public Date getPlanCloseTime() { return planCloseTime; }
    public void setPlanCloseTime(Date planCloseTime) { this.planCloseTime = planCloseTime; }
    public Date getCloseTime() { return closeTime; }
    public void setCloseTime(Date closeTime) { this.closeTime = closeTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
