package com.ruoyi.safety.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 风险点管理对象 biz_safety_risk_point
 *
 * @author ruoyi
 */
public class SafetyRiskPoint extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 风险点ID */
    private Long riskPointId;

    /** 风险点编号 */
    @Excel(name = "风险点编号")
    private String riskCode;

    /** 风险点名称 */
    @Excel(name = "风险点名称")
    private String riskName;

    /** 所属区域ID */
    private Long areaId;

    /** 所属区域名称 */
    @Excel(name = "所属区域")
    private String areaName;

    /** 危险源描述 */
    @Excel(name = "危险源描述")
    private String hazardDesc;

    /** 事故类别 */
    @Excel(name = "事故类别")
    private String accidentType;

    /** L值 */
    @Excel(name = "L值")
    private BigDecimal lValue;

    /** E值 */
    @Excel(name = "E值")
    private BigDecimal eValue;

    /** C值 */
    @Excel(name = "C值")
    private BigDecimal cValue;

    /** D值 */
    @Excel(name = "D值")
    private BigDecimal dValue;

    /** 风险等级 */
    @Excel(name = "风险等级", readConverterExp = "1=红,2=橙,3=黄,4=蓝")
    private String riskLevel;

    /** 管控层级 */
    @Excel(name = "管控层级", readConverterExp = "1=公司,2=部门,3=车间,4=班组")
    private String controlLevel;

    /** 工程技术措施 */
    private String measureEngine;

    /** 管理措施 */
    private String measureManage;

    /** 培训教育措施 */
    private String measureTraining;

    /** 个体防护措施 */
    private String measurePpe;

    /** 应急处置措施 */
    private String measureEmergency;

    /** 检查标准 */
    @Excel(name = "检查标准")
    private String checkStandard;

    /** 排查频次 */
    @Excel(name = "排查频次", readConverterExp = "1=每班,2=每日,3=每周,4=每月,5=每季")
    private String checkFrequency;

    /** 是否重大隐患 */
    @Excel(name = "是否重大隐患", readConverterExp = "0=否,1=是")
    private String isMajorHazard;

    /** 重大隐患判定规则 */
    private String majorHazardRule;

    /** 合规文件 */
    private String complianceFile;

    /** X坐标 */
    private BigDecimal xCoord;

    /** Y坐标 */
    private BigDecimal yCoord;

    /** 责任人ID */
    private Long personId;

    /** 责任人名称 */
    @Excel(name = "责任人")
    private String personName;

    /** 评价日期 */
    @Excel(name = "评价日期")
    private String evaluateDate;

    /** 复评日期 */
    @Excel(name = "复评日期")
    private String reEvaluateDate;

    /** 所属企业ID */
    private Long enterpriseId;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getRiskPointId() { return riskPointId; }
    public void setRiskPointId(Long riskPointId) { this.riskPointId = riskPointId; }
    public String getRiskCode() { return riskCode; }
    public void setRiskCode(String riskCode) { this.riskCode = riskCode; }
    public String getRiskName() { return riskName; }
    public void setRiskName(String riskName) { this.riskName = riskName; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getHazardDesc() { return hazardDesc; }
    public void setHazardDesc(String hazardDesc) { this.hazardDesc = hazardDesc; }
    public String getAccidentType() { return accidentType; }
    public void setAccidentType(String accidentType) { this.accidentType = accidentType; }
    public BigDecimal getLValue() { return lValue; }
    public void setLValue(BigDecimal lValue) { this.lValue = lValue; }
    public BigDecimal getEValue() { return eValue; }
    public void setEValue(BigDecimal eValue) { this.eValue = eValue; }
    public BigDecimal getCValue() { return cValue; }
    public void setCValue(BigDecimal cValue) { this.cValue = cValue; }
    public BigDecimal getDValue() { return dValue; }
    public void setDValue(BigDecimal dValue) { this.dValue = dValue; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getControlLevel() { return controlLevel; }
    public void setControlLevel(String controlLevel) { this.controlLevel = controlLevel; }
    public String getMeasureEngine() { return measureEngine; }
    public void setMeasureEngine(String measureEngine) { this.measureEngine = measureEngine; }
    public String getMeasureManage() { return measureManage; }
    public void setMeasureManage(String measureManage) { this.measureManage = measureManage; }
    public String getMeasureTraining() { return measureTraining; }
    public void setMeasureTraining(String measureTraining) { this.measureTraining = measureTraining; }
    public String getMeasurePpe() { return measurePpe; }
    public void setMeasurePpe(String measurePpe) { this.measurePpe = measurePpe; }
    public String getMeasureEmergency() { return measureEmergency; }
    public void setMeasureEmergency(String measureEmergency) { this.measureEmergency = measureEmergency; }
    public String getCheckStandard() { return checkStandard; }
    public void setCheckStandard(String checkStandard) { this.checkStandard = checkStandard; }
    public String getCheckFrequency() { return checkFrequency; }
    public void setCheckFrequency(String checkFrequency) { this.checkFrequency = checkFrequency; }
    public String getIsMajorHazard() { return isMajorHazard; }
    public void setIsMajorHazard(String isMajorHazard) { this.isMajorHazard = isMajorHazard; }
    public String getMajorHazardRule() { return majorHazardRule; }
    public void setMajorHazardRule(String majorHazardRule) { this.majorHazardRule = majorHazardRule; }
    public String getComplianceFile() { return complianceFile; }
    public void setComplianceFile(String complianceFile) { this.complianceFile = complianceFile; }
    public BigDecimal getXCoord() { return xCoord; }
    public void setXCoord(BigDecimal xCoord) { this.xCoord = xCoord; }
    public BigDecimal getYCoord() { return yCoord; }
    public void setYCoord(BigDecimal yCoord) { this.yCoord = yCoord; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public String getEvaluateDate() { return evaluateDate; }
    public void setEvaluateDate(String evaluateDate) { this.evaluateDate = evaluateDate; }
    public String getReEvaluateDate() { return reEvaluateDate; }
    public void setReEvaluateDate(String reEvaluateDate) { this.reEvaluateDate = reEvaluateDate; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}