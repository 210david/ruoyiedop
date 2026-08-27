package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

public class HrSocialSecurity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long ssId;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @NotNull(message = "年度不能为空")
    @Excel(name = "年度")
    private Integer year;
    @NotNull(message = "缴费基数不能为空")
    @Excel(name = "缴费基数")
    private BigDecimal baseAmount;
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
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getSsId() { return ssId; }
    public void setSsId(Long ssId) { this.ssId = ssId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
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
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
