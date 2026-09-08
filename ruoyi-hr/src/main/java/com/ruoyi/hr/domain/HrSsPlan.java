package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * HR社保方案对象 biz_hr_ss_plan
 *
 * 全公司默认社保公积金比例，按年度维护；
 * 员工社保记录通过 plan_id 引用，个别员工有差异时在其记录上覆盖比例字段。
 */
public class HrSsPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long planId;
    @NotBlank(message = "方案名称不能为空")
    @Excel(name = "方案名称")
    private String planName;
    @NotNull(message = "适用年度不能为空")
    @Excel(name = "适用年度")
    private Integer year;
    @Excel(name = "默认方案")
    private Integer isDefault;
    @Excel(name = "基数下限")
    private BigDecimal baseLower;
    @Excel(name = "基数上限")
    private BigDecimal baseUpper;
    @Excel(name = "养老单位比例")
    private BigDecimal pensionEmployer;
    @Excel(name = "养老个人比例")
    private BigDecimal pensionEmployee;
    @Excel(name = "医疗单位比例")
    private BigDecimal medicalEmployer;
    @Excel(name = "医疗个人比例")
    private BigDecimal medicalEmployee;
    @Excel(name = "失业单位比例")
    private BigDecimal unemploymentEmployer;
    @Excel(name = "失业个人比例")
    private BigDecimal unemploymentEmployee;
    @Excel(name = "工伤单位比例")
    private BigDecimal injuryEmployer;
    @Excel(name = "生育单位比例")
    private BigDecimal maternityEmployer;
    @Excel(name = "公积金单位比例")
    private BigDecimal fundEmployer;
    @Excel(name = "公积金个人比例")
    private BigDecimal fundEmployee;
    @Excel(name = "状态")
    private String status;
    private String delFlag;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getIsDefault() { return isDefault; }
    public void setIsDefault(Integer isDefault) { this.isDefault = isDefault; }
    public BigDecimal getBaseLower() { return baseLower; }
    public void setBaseLower(BigDecimal baseLower) { this.baseLower = baseLower; }
    public BigDecimal getBaseUpper() { return baseUpper; }
    public void setBaseUpper(BigDecimal baseUpper) { this.baseUpper = baseUpper; }
    public BigDecimal getPensionEmployer() { return pensionEmployer; }
    public void setPensionEmployer(BigDecimal pensionEmployer) { this.pensionEmployer = pensionEmployer; }
    public BigDecimal getPensionEmployee() { return pensionEmployee; }
    public void setPensionEmployee(BigDecimal pensionEmployee) { this.pensionEmployee = pensionEmployee; }
    public BigDecimal getMedicalEmployer() { return medicalEmployer; }
    public void setMedicalEmployer(BigDecimal medicalEmployer) { this.medicalEmployer = medicalEmployer; }
    public BigDecimal getMedicalEmployee() { return medicalEmployee; }
    public void setMedicalEmployee(BigDecimal medicalEmployee) { this.medicalEmployee = medicalEmployee; }
    public BigDecimal getUnemploymentEmployer() { return unemploymentEmployer; }
    public void setUnemploymentEmployer(BigDecimal unemploymentEmployer) { this.unemploymentEmployer = unemploymentEmployer; }
    public BigDecimal getUnemploymentEmployee() { return unemploymentEmployee; }
    public void setUnemploymentEmployee(BigDecimal unemploymentEmployee) { this.unemploymentEmployee = unemploymentEmployee; }
    public BigDecimal getInjuryEmployer() { return injuryEmployer; }
    public void setInjuryEmployer(BigDecimal injuryEmployer) { this.injuryEmployer = injuryEmployer; }
    public BigDecimal getMaternityEmployer() { return maternityEmployer; }
    public void setMaternityEmployer(BigDecimal maternityEmployer) { this.maternityEmployer = maternityEmployer; }
    public BigDecimal getFundEmployer() { return fundEmployer; }
    public void setFundEmployer(BigDecimal fundEmployer) { this.fundEmployer = fundEmployer; }
    public BigDecimal getFundEmployee() { return fundEmployee; }
    public void setFundEmployee(BigDecimal fundEmployee) { this.fundEmployee = fundEmployee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
