package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrPayslip extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long payslipId;
    @NotBlank(message = "年月不能为空")
    @Excel(name = "年月")
    private String yearMonth;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "组织ID")
    private Long orgId;
    @Excel(name = "基本工资")
    private BigDecimal basicSalary;
    @Excel(name = "岗位工资")
    private BigDecimal positionSalary;
    @Excel(name = "技能津贴")
    private BigDecimal skillAllowance;
    @Excel(name = "工龄工资")
    private BigDecimal seniorityPay;
    @Excel(name = "夜班津贴")
    private BigDecimal nightAllowance;
    @Excel(name = "高温津贴")
    private BigDecimal highTempAllowance;
    @Excel(name = "有毒有害津贴")
    private BigDecimal hazardAllowance;
    @Excel(name = "餐补")
    private BigDecimal mealAllowance;
    @Excel(name = "交通补贴")
    private BigDecimal transportAllowance;
    @Excel(name = "全勤奖")
    private BigDecimal fullAttendanceBonus;
    @Excel(name = "计件工资")
    private BigDecimal pieceworkPay;
    @Excel(name = "加班费")
    private BigDecimal overtimePay;
    @Excel(name = "绩效奖金")
    private BigDecimal performanceBonus;
    @Excel(name = "缺勤扣款")
    private BigDecimal absenceDeduction;
    @Excel(name = "其他加项")
    private BigDecimal otherAdd;
    @Excel(name = "其他扣项")
    private BigDecimal otherDeduct;
    @Excel(name = "应发合计")
    private BigDecimal grossSalary;
    @Excel(name = "社保个人")
    private BigDecimal socialInsurance;
    @Excel(name = "公积金个人")
    private BigDecimal houseFund;
    @Excel(name = "个人所得税")
    private BigDecimal incomeTax;
    @Excel(name = "其他扣款")
    private BigDecimal otherDeduction;
    @Excel(name = "实发工资")
    private BigDecimal netSalary;
    @Excel(name = "状态")
    private String calcStatus;
    @Excel(name = "锁定人")
    private String sealedBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "锁定时间")
    private Date sealedTime;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发放日期")
    private Date payDate;
    @Excel(name = "银行回单号")
    private String bankReceiptNo;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getPayslipId() { return payslipId; }
    public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public void setBasicSalary(BigDecimal basicSalary) { this.basicSalary = basicSalary; }
    public BigDecimal getPositionSalary() { return positionSalary; }
    public void setPositionSalary(BigDecimal positionSalary) { this.positionSalary = positionSalary; }
    public BigDecimal getSkillAllowance() { return skillAllowance; }
    public void setSkillAllowance(BigDecimal skillAllowance) { this.skillAllowance = skillAllowance; }
    public BigDecimal getSeniorityPay() { return seniorityPay; }
    public void setSeniorityPay(BigDecimal seniorityPay) { this.seniorityPay = seniorityPay; }
    public BigDecimal getNightAllowance() { return nightAllowance; }
    public void setNightAllowance(BigDecimal nightAllowance) { this.nightAllowance = nightAllowance; }
    public BigDecimal getHighTempAllowance() { return highTempAllowance; }
    public void setHighTempAllowance(BigDecimal highTempAllowance) { this.highTempAllowance = highTempAllowance; }
    public BigDecimal getHazardAllowance() { return hazardAllowance; }
    public void setHazardAllowance(BigDecimal hazardAllowance) { this.hazardAllowance = hazardAllowance; }
    public BigDecimal getMealAllowance() { return mealAllowance; }
    public void setMealAllowance(BigDecimal mealAllowance) { this.mealAllowance = mealAllowance; }
    public BigDecimal getTransportAllowance() { return transportAllowance; }
    public void setTransportAllowance(BigDecimal transportAllowance) { this.transportAllowance = transportAllowance; }
    public BigDecimal getFullAttendanceBonus() { return fullAttendanceBonus; }
    public void setFullAttendanceBonus(BigDecimal fullAttendanceBonus) { this.fullAttendanceBonus = fullAttendanceBonus; }
    public BigDecimal getPieceworkPay() { return pieceworkPay; }
    public void setPieceworkPay(BigDecimal pieceworkPay) { this.pieceworkPay = pieceworkPay; }
    public BigDecimal getOvertimePay() { return overtimePay; }
    public void setOvertimePay(BigDecimal overtimePay) { this.overtimePay = overtimePay; }
    public BigDecimal getPerformanceBonus() { return performanceBonus; }
    public void setPerformanceBonus(BigDecimal performanceBonus) { this.performanceBonus = performanceBonus; }
    public BigDecimal getAbsenceDeduction() { return absenceDeduction; }
    public void setAbsenceDeduction(BigDecimal absenceDeduction) { this.absenceDeduction = absenceDeduction; }
    public BigDecimal getOtherAdd() { return otherAdd; }
    public void setOtherAdd(BigDecimal otherAdd) { this.otherAdd = otherAdd; }
    public BigDecimal getOtherDeduct() { return otherDeduct; }
    public void setOtherDeduct(BigDecimal otherDeduct) { this.otherDeduct = otherDeduct; }
    public BigDecimal getGrossSalary() { return grossSalary; }
    public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }
    public BigDecimal getSocialInsurance() { return socialInsurance; }
    public void setSocialInsurance(BigDecimal socialInsurance) { this.socialInsurance = socialInsurance; }
    public BigDecimal getHouseFund() { return houseFund; }
    public void setHouseFund(BigDecimal houseFund) { this.houseFund = houseFund; }
    public BigDecimal getIncomeTax() { return incomeTax; }
    public void setIncomeTax(BigDecimal incomeTax) { this.incomeTax = incomeTax; }
    public BigDecimal getOtherDeduction() { return otherDeduction; }
    public void setOtherDeduction(BigDecimal otherDeduction) { this.otherDeduction = otherDeduction; }
    public BigDecimal getNetSalary() { return netSalary; }
    public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
    public String getCalcStatus() { return calcStatus; }
    public void setCalcStatus(String calcStatus) { this.calcStatus = calcStatus; }
    public String getSealedBy() { return sealedBy; }
    public void setSealedBy(String sealedBy) { this.sealedBy = sealedBy; }
    public Date getSealedTime() { return sealedTime; }
    public void setSealedTime(Date sealedTime) { this.sealedTime = sealedTime; }
    public Date getPayDate() { return payDate; }
    public void setPayDate(Date payDate) { this.payDate = payDate; }
    public String getBankReceiptNo() { return bankReceiptNo; }
    public void setBankReceiptNo(String bankReceiptNo) { this.bankReceiptNo = bankReceiptNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
