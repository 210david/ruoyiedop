package com.ruoyi.safety.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 企业安全档案对象 biz_safety_enterprise
 *
 * @author ruoyi
 */
public class SafetyEnterprise extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 企业ID */
    private Long enterpriseId;

    /** 企业名称 */
    @Excel(name = "企业名称")
    private String enterpriseName;

    /** 统一社会信用代码 */
    @Excel(name = "统一社会信用代码")
    private String creditCode;

    /** 行业类别 */
    @Excel(name = "行业类别", readConverterExp = "1=新能源材料,2=绿色铝精加工,3=高端精细化工,4=先进装备制造,5=高原特色食品加工")
    private String industryType;

    /** 是否高危行业 */
    @Excel(name = "是否高危", readConverterExp = "0=否,1=是")
    private String isHighRisk;

    /** 法定代表人 */
    @Excel(name = "法定代表人")
    private String legalPerson;

    /** 安全负责人ID */
    private Long safetyManagerId;

    /** 安全负责人名称 */
    @Excel(name = "安全负责人")
    private String safetyManager;

    /** 安全管理机构 */
    @Excel(name = "安全管理机构")
    private String safetyOrg;

    /** 安全生产责任制 */
    private String responsibility;

    /** 属地监管单位 */
    @Excel(name = "属地监管单位")
    private String superviseDept;

    /** 厂区平面图 */
    @Excel(name = "厂区平面图")
    private String planImage;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getEnterpriseName() { return enterpriseName; }
    public void setEnterpriseName(String enterpriseName) { this.enterpriseName = enterpriseName; }
    public String getCreditCode() { return creditCode; }
    public void setCreditCode(String creditCode) { this.creditCode = creditCode; }
    public String getIndustryType() { return industryType; }
    public void setIndustryType(String industryType) { this.industryType = industryType; }
    public String getIsHighRisk() { return isHighRisk; }
    public void setIsHighRisk(String isHighRisk) { this.isHighRisk = isHighRisk; }
    public String getLegalPerson() { return legalPerson; }
    public void setLegalPerson(String legalPerson) { this.legalPerson = legalPerson; }
    public Long getSafetyManagerId() { return safetyManagerId; }
    public void setSafetyManagerId(Long safetyManagerId) { this.safetyManagerId = safetyManagerId; }
    public String getSafetyManager() { return safetyManager; }
    public void setSafetyManager(String safetyManager) { this.safetyManager = safetyManager; }
    public String getSafetyOrg() { return safetyOrg; }
    public void setSafetyOrg(String safetyOrg) { this.safetyOrg = safetyOrg; }
    public String getResponsibility() { return responsibility; }
    public void setResponsibility(String responsibility) { this.responsibility = responsibility; }
    public String getSuperviseDept() { return superviseDept; }
    public void setSuperviseDept(String superviseDept) { this.superviseDept = superviseDept; }
    public String getPlanImage() { return planImage; }
    public void setPlanImage(String planImage) { this.planImage = planImage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}