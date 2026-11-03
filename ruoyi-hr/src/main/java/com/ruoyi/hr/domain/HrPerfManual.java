package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 线下业绩手工补录对象 biz_hr_perf_manual
 *
 * 需求三：线下业绩手工补录需审批，审批通过后计入归集
 */
public class HrPerfManual extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 补录ID */
    private Long manualId;

    /** 归集周期（yyyy-MM） */
    @Excel(name = "归集周期")
    private String period;

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名 */
    @Excel(name = "员工姓名")
    private String employeeName;

    /** 补录业绩金额 */
    @Excel(name = "补录金额")
    private BigDecimal manualAmount;

    /** 补录原因 */
    private String manualReason;

    /** 状态（0待审批 1已通过 2已驳回） */
    @Excel(name = "状态", dictType = "hr_manual_status")
    private String perfStatus;

    /** 审批人 */
    private String approver;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批意见 */
    private String approveOpinion;

    /** 删除标志 */
    private String delFlag;

    public Long getManualId() { return manualId; }
    public void setManualId(Long manualId) { this.manualId = manualId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public BigDecimal getManualAmount() { return manualAmount; }
    public void setManualAmount(BigDecimal manualAmount) { this.manualAmount = manualAmount; }
    public String getManualReason() { return manualReason; }
    public void setManualReason(String manualReason) { this.manualReason = manualReason; }
    public String getPerfStatus() { return perfStatus; }
    public void setPerfStatus(String perfStatus) { this.perfStatus = perfStatus; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
    public String getApproveOpinion() { return approveOpinion; }
    public void setApproveOpinion(String approveOpinion) { this.approveOpinion = approveOpinion; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
