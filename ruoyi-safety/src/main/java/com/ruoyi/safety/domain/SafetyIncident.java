package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SafetyIncident extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long incidentId;
    @Excel(name = "事故编号")
    private String incidentCode;
    @Excel(name = "事故等级")
    @NotBlank(message = "事故等级不能为空")
    private String incidentLevel;
    @Excel(name = "发生时间", width = 160)
    @NotNull(message = "事故发生时间不能为空")
    private Date incidentDate;
    @Excel(name = "事故地点")
    @NotBlank(message = "事故地点不能为空")
    private String incidentLocation;
    @NotBlank(message = "事故描述不能为空")
    private String incidentDesc;
    @Excel(name = "伤亡人数")
    private Integer casualties;
    @Excel(name = "经济损失")
    private BigDecimal economicLoss;
    private String causeAnalysis;
    private String fourNotLetGo;
    private String rectifyMeasures;
    private Long reportPersonId;
    @Excel(name = "报告人")
    private String reportPerson;
    @Excel(name = "报告时间", width = 160)
    private Date reportTime;
    private Long enterpriseId;
    private String delFlag;

    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }
    public String getIncidentCode() { return incidentCode; }
    public void setIncidentCode(String incidentCode) { this.incidentCode = incidentCode; }
    public String getIncidentLevel() { return incidentLevel; }
    public void setIncidentLevel(String incidentLevel) { this.incidentLevel = incidentLevel; }
    public Date getIncidentDate() { return incidentDate; }
    public void setIncidentDate(Date incidentDate) { this.incidentDate = incidentDate; }
    public String getIncidentLocation() { return incidentLocation; }
    public void setIncidentLocation(String incidentLocation) { this.incidentLocation = incidentLocation; }
    public String getIncidentDesc() { return incidentDesc; }
    public void setIncidentDesc(String incidentDesc) { this.incidentDesc = incidentDesc; }
    public Integer getCasualties() { return casualties; }
    public void setCasualties(Integer casualties) { this.casualties = casualties; }
    public BigDecimal getEconomicLoss() { return economicLoss; }
    public void setEconomicLoss(BigDecimal economicLoss) { this.economicLoss = economicLoss; }
    public String getCauseAnalysis() { return causeAnalysis; }
    public void setCauseAnalysis(String causeAnalysis) { this.causeAnalysis = causeAnalysis; }
    public String getFourNotLetGo() { return fourNotLetGo; }
    public void setFourNotLetGo(String fourNotLetGo) { this.fourNotLetGo = fourNotLetGo; }
    public String getRectifyMeasures() { return rectifyMeasures; }
    public void setRectifyMeasures(String rectifyMeasures) { this.rectifyMeasures = rectifyMeasures; }
    public Long getReportPersonId() { return reportPersonId; }
    public void setReportPersonId(Long reportPersonId) { this.reportPersonId = reportPersonId; }
    public String getReportPerson() { return reportPerson; }
    public void setReportPerson(String reportPerson) { this.reportPerson = reportPerson; }
    public Date getReportTime() { return reportTime; }
    public void setReportTime(Date reportTime) { this.reportTime = reportTime; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}