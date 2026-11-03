package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 营销业绩月度归集对象 biz_hr_perf_month
 *
 * 需求三：定时任务按月归集（成交/回款/新客/补录），支持手动触发重算（幂等：周期+员工唯一）
 */
public class HrPerfMonth extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 归集ID */
    private Long perfId;

    /** 归集周期（yyyy-MM） */
    @Excel(name = "归集周期")
    private String period;

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名 */
    @Excel(name = "员工姓名")
    private String employeeName;

    /** 系统用户ID（营销业绩归属） */
    private Long userId;

    /** 成交金额（营销模块订单） */
    @Excel(name = "成交金额")
    private BigDecimal dealAmount;

    /** 回款金额（回款记录） */
    @Excel(name = "回款金额")
    private BigDecimal paymentAmount;

    /** 新开客户数（周期内建档且完成首笔成交） */
    @Excel(name = "新开客户数")
    private Integer newCustomerCount;

    /** 审批通过的补录业绩 */
    @Excel(name = "补录业绩")
    private BigDecimal manualAmount;

    /** 合计业绩（成交+补录） */
    @Excel(name = "合计业绩")
    private BigDecimal totalAmount;

    /** 归集时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date aggTime;

    /** 删除标志 */
    private String delFlag;

    public Long getPerfId() { return perfId; }
    public void setPerfId(Long perfId) { this.perfId = perfId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public BigDecimal getDealAmount() { return dealAmount; }
    public void setDealAmount(BigDecimal dealAmount) { this.dealAmount = dealAmount; }
    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }
    public Integer getNewCustomerCount() { return newCustomerCount; }
    public void setNewCustomerCount(Integer newCustomerCount) { this.newCustomerCount = newCustomerCount; }
    public BigDecimal getManualAmount() { return manualAmount; }
    public void setManualAmount(BigDecimal manualAmount) { this.manualAmount = manualAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Date getAggTime() { return aggTime; }
    public void setAggTime(Date aggTime) { this.aggTime = aggTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
