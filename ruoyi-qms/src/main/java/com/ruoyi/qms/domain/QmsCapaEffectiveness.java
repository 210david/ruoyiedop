package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsCapaEffectiveness extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long effectivenessId;
    private Long capaId;
    @Excel(name = "CAPA编号") private String capaNo;
    @Excel(name = "检查周期") private String checkPeriod;
    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "检查日期", dateFormat = "yyyy-MM-dd") private Date checkDate;
    @Excel(name = "检查人") private String checker;
    private String checkMethod;
    @Excel(name = "检查结果") private String checkResult;
    private String evidence;
    private String recurrenceFlag;
    private String recurrenceDesc;
    private String actionTaken;
    private String esigUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date esigTime;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long getEffectivenessId() { return effectivenessId; }
    public void setEffectivenessId(Long effectivenessId) { this.effectivenessId = effectivenessId; }
    public Long getCapaId() { return capaId; }
    public void setCapaId(Long capaId) { this.capaId = capaId; }
    public String getCapaNo() { return capaNo; }
    public void setCapaNo(String capaNo) { this.capaNo = capaNo; }
    public String getCheckPeriod() { return checkPeriod; }
    public void setCheckPeriod(String checkPeriod) { this.checkPeriod = checkPeriod; }
    public Date getCheckDate() { return checkDate; }
    public void setCheckDate(Date checkDate) { this.checkDate = checkDate; }
    public String getChecker() { return checker; }
    public void setChecker(String checker) { this.checker = checker; }
    public String getCheckMethod() { return checkMethod; }
    public void setCheckMethod(String checkMethod) { this.checkMethod = checkMethod; }
    public String getCheckResult() { return checkResult; }
    public void setCheckResult(String checkResult) { this.checkResult = checkResult; }
    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }
    public String getRecurrenceFlag() { return recurrenceFlag; }
    public void setRecurrenceFlag(String recurrenceFlag) { this.recurrenceFlag = recurrenceFlag; }
    public String getRecurrenceDesc() { return recurrenceDesc; }
    public void setRecurrenceDesc(String recurrenceDesc) { this.recurrenceDesc = recurrenceDesc; }
    public String getActionTaken() { return actionTaken; }
    public void setActionTaken(String actionTaken) { this.actionTaken = actionTaken; }
    public String getEsigUser() { return esigUser; }
    public void setEsigUser(String esigUser) { this.esigUser = esigUser; }
    public Date getEsigTime() { return esigTime; }
    public void setEsigTime(Date esigTime) { this.esigTime = esigTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
