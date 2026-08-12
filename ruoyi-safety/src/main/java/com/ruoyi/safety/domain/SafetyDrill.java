package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SafetyDrill extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long drillId;
    @Excel(name = "演练编号")
    private String drillCode;
    private Long planId;
    @Excel(name = "演练类型")
    @NotBlank(message = "演练类型不能为空")
    private String drillType;
    @Excel(name = "演练名称")
    @NotBlank(message = "演练名称不能为空")
    private String drillName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "演练日期", width = 120)
    @NotNull(message = "演练日期不能为空")
    private Date drillDate;
    @Excel(name = "演练地点")
    @NotBlank(message = "演练地点不能为空")
    private String drillLocation;
    @Excel(name = "演练指挥")
    @NotBlank(message = "演练指挥不能为空")
    private String drillCommander;
    @Excel(name = "演练目的")
    private String drillObjective;
    @Excel(name = "参与人员")
    @NotBlank(message = "参与人员不能为空")
    private String participants;
    @Excel(name = "参与人数")
    private Integer participantCount;
    @NotBlank(message = "演练过程描述不能为空")
    private String drillDesc;
    @NotBlank(message = "演练评估不能为空")
    private String evalResult;
    private String problems;
    private String improvements;
    private String attachments;
    private Long enterpriseId;
    private String delFlag;

    public Long getDrillId() { return drillId; }
    public void setDrillId(Long drillId) { this.drillId = drillId; }
    public String getDrillCode() { return drillCode; }
    public void setDrillCode(String drillCode) { this.drillCode = drillCode; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getDrillType() { return drillType; }
    public void setDrillType(String drillType) { this.drillType = drillType; }
    public String getDrillName() { return drillName; }
    public void setDrillName(String drillName) { this.drillName = drillName; }
    public Date getDrillDate() { return drillDate; }
    public void setDrillDate(Date drillDate) { this.drillDate = drillDate; }
    public String getDrillLocation() { return drillLocation; }
    public void setDrillLocation(String drillLocation) { this.drillLocation = drillLocation; }
    public String getDrillCommander() { return drillCommander; }
    public void setDrillCommander(String drillCommander) { this.drillCommander = drillCommander; }
    public String getDrillObjective() { return drillObjective; }
    public void setDrillObjective(String drillObjective) { this.drillObjective = drillObjective; }
    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
    public Integer getParticipantCount() { return participantCount; }
    public void setParticipantCount(Integer participantCount) { this.participantCount = participantCount; }
    public String getDrillDesc() { return drillDesc; }
    public void setDrillDesc(String drillDesc) { this.drillDesc = drillDesc; }
    public String getEvalResult() { return evalResult; }
    public void setEvalResult(String evalResult) { this.evalResult = evalResult; }
    public String getProblems() { return problems; }
    public void setProblems(String problems) { this.problems = problems; }
    public String getImprovements() { return improvements; }
    public void setImprovements(String improvements) { this.improvements = improvements; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}