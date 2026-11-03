package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 提成核算表对象 biz_hr_commission_bill
 *
 * 需求三：每期自动生成提成核算表（口径/方案/金额为生成时快照），财务/人力确认后锁定
 */
public class HrCommissionBill extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 核算表ID */
    private Long billId;

    /** 核算周期（yyyy-MM） */
    @Excel(name = "核算周期")
    private String period;

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名 */
    @Excel(name = "员工姓名")
    private String employeeName;

    /** 提成方案ID */
    private Long planId;

    /** 提成方案名称 */
    @Excel(name = "提成方案")
    private String planName;

    /** 计算口径（快照） */
    @Excel(name = "计算口径", dictType = "hr_commission_base")
    private String calcBase;

    /** 计算基数金额 */
    @Excel(name = "计算基数")
    private BigDecimal baseAmount;

    /** 提成金额 */
    @Excel(name = "提成金额")
    private BigDecimal commissionAmount;

    /** 状态（0待确认 1已确认 2已锁定） */
    @Excel(name = "状态", dictType = "hr_bill_status")
    private String billStatus;

    /** 确认人 */
    private String confirmer;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    /** 删除标志 */
    private String delFlag;

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getCalcBase() { return calcBase; }
    public void setCalcBase(String calcBase) { this.calcBase = calcBase; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
    public BigDecimal getCommissionAmount() { return commissionAmount; }
    public void setCommissionAmount(BigDecimal commissionAmount) { this.commissionAmount = commissionAmount; }
    public String getBillStatus() { return billStatus; }
    public void setBillStatus(String billStatus) { this.billStatus = billStatus; }
    public String getConfirmer() { return confirmer; }
    public void setConfirmer(String confirmer) { this.confirmer = confirmer; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
