package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyStdLibrary extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long libraryId;
    @Excel(name = "行业类型")
    private String industryType;
    @Excel(name = "区域类型")
    private String areaType;
    @Excel(name = "风险点名称")
    private String riskPointName;
    private String hazardDesc;
    private BigDecimal lValue;
    private BigDecimal eValue;
    private BigDecimal cValue;
    private BigDecimal dValue;
    private String riskLevel;
    private String checkStandard;
    @Excel(name = "排查频次")
    private String checkFrequency;
    private String controlMeasures;
    private String isMajorHazard;
    private String majorHazardRule;
    private String complianceFile;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getLibraryId() { return libraryId; }
    public void setLibraryId(Long libraryId) { this.libraryId = libraryId; }
    public String getIndustryType() { return industryType; }
    public void setIndustryType(String industryType) { this.industryType = industryType; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }
    public String getRiskPointName() { return riskPointName; }
    public void setRiskPointName(String riskPointName) { this.riskPointName = riskPointName; }
    public String getHazardDesc() { return hazardDesc; }
    public void setHazardDesc(String hazardDesc) { this.hazardDesc = hazardDesc; }
    public BigDecimal getlValue() { return lValue; }
    public void setlValue(BigDecimal lValue) { this.lValue = lValue; }
    public BigDecimal geteValue() { return eValue; }
    public void seteValue(BigDecimal eValue) { this.eValue = eValue; }
    public BigDecimal getcValue() { return cValue; }
    public void setcValue(BigDecimal cValue) { this.cValue = cValue; }
    public BigDecimal getdValue() { return dValue; }
    public void setdValue(BigDecimal dValue) { this.dValue = dValue; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getCheckStandard() { return checkStandard; }
    public void setCheckStandard(String checkStandard) { this.checkStandard = checkStandard; }
    public String getCheckFrequency() { return checkFrequency; }
    public void setCheckFrequency(String checkFrequency) { this.checkFrequency = checkFrequency; }
    public String getControlMeasures() { return controlMeasures; }
    public void setControlMeasures(String controlMeasures) { this.controlMeasures = controlMeasures; }
    public String getIsMajorHazard() { return isMajorHazard; }
    public void setIsMajorHazard(String isMajorHazard) { this.isMajorHazard = isMajorHazard; }
    public String getMajorHazardRule() { return majorHazardRule; }
    public void setMajorHazardRule(String majorHazardRule) { this.majorHazardRule = majorHazardRule; }
    public String getComplianceFile() { return complianceFile; }
    public void setComplianceFile(String complianceFile) { this.complianceFile = complianceFile; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}