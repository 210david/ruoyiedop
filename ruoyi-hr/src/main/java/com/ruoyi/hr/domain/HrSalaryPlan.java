package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrSalaryPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long planId;
    @NotBlank(message = "定薪单号不能为空")
    @Excel(name = "定薪单号")
    private String planNo;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @Excel(name = "类型")
    private String planType;
    @NotNull(message = "生效日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期")
    private Date effectiveDate;
    @Excel(name = "基本工资")
    private BigDecimal basicSalary;
    @Excel(name = "岗位工资")
    private BigDecimal positionSalary;
    @Excel(name = "技能津贴")
    private BigDecimal skillAllowance;
    @Excel(name = "工龄工资")
    private BigDecimal seniorityPay;
    @Excel(name = "夜班津贴标准")
    private BigDecimal nightAllowance;
    @Excel(name = "高温津贴标准")
    private BigDecimal highTempAllowance;
    @Excel(name = "有毒有害津贴")
    private BigDecimal hazardAllowance;
    @Excel(name = "餐补标准")
    private BigDecimal mealAllowance;
    @Excel(name = "交通补贴")
    private BigDecimal transportAllowance;
    @Excel(name = "全勤奖")
    private BigDecimal fullAttendanceBonus;
    @Excel(name = "绩效奖金")
    private BigDecimal performanceBonus;
    @Excel(name = "其他补贴")
    private BigDecimal otherAllowance;
    @Excel(name = "薪资合计")
    private BigDecimal totalSalary;
    @Excel(name = "审批状态")
    private String auditStatus;
    @Excel(name = "审批人")
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间")
    private Date auditTime;
    @Excel(name = "审批意见")
    private String auditRemark;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanNo() { return planNo; }
    public void setPlanNo(String planNo) { this.planNo = planNo; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
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
    public BigDecimal getPerformanceBonus() { return performanceBonus; }
    public void setPerformanceBonus(BigDecimal performanceBonus) { this.performanceBonus = performanceBonus; }
    public BigDecimal getOtherAllowance() { return otherAllowance; }
    public void setOtherAllowance(BigDecimal otherAllowance) { this.otherAllowance = otherAllowance; }
    public BigDecimal getTotalSalary() { return totalSalary; }
    public void setTotalSalary(BigDecimal totalSalary) { this.totalSalary = totalSalary; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
