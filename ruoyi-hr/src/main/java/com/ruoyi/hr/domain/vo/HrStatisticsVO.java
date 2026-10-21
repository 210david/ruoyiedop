package com.ruoyi.hr.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 人力资源统计报表 VO
 *
 * 用于各类统计查询的通用返回对象，字段按需使用。
 * 继承 BaseEntity 以支持 params 日期范围查询参数。
 *
 * @author ruoyi
 */
public class HrStatisticsVO extends BaseEntity
{
    /** 部门/组织名称 */
    @Excel(name = "部门/组织")
    private String name;

    /** 部门ID */
    private Long deptId;

    /** 在册人数（在职+试用期） */
    @Excel(name = "在册人数")
    private Integer count;

    /** 男性人数 */
    @Excel(name = "男性人数")
    private Integer maleCount;

    /** 女性人数 */
    @Excel(name = "女性人数")
    private Integer femaleCount;

    /** 入职人数 */
    @Excel(name = "入职人数")
    private Integer entryCount;

    /** 离职人数 */
    @Excel(name = "离职人数")
    private Integer leaveCount;

    /** 出勤天数 */
    @Excel(name = "出勤天数")
    private BigDecimal attendDays;

    /** 迟到次数 */
    @Excel(name = "迟到次数")
    private Integer lateCount;

    /** 早退次数 */
    private Integer earlyCount;

    /** 缺勤天数 */
    @Excel(name = "缺勤天数")
    private BigDecimal absentDays;

    /** 请假天数 */
    @Excel(name = "请假天数")
    private BigDecimal leaveDays;

    /** 加班工时 */
    @Excel(name = "加班工时")
    private BigDecimal overtimeHours;

    /** 夜班次数 */
    private Integer nightShiftCount;

    /** 应出勤人数（参与月度考勤人数） */
    private Integer requiredPeople;

    /** 应发工资总额（应发合计） */
    @Excel(name = "应发工资总额")
    private BigDecimal grossAmount;

    /** 个人社保总额 */
    @Excel(name = "个人社保")
    private BigDecimal socialAmount;

    /** 个人公积金总额 */
    @Excel(name = "个人公积金")
    private BigDecimal fundAmount;

    /** 个人所得税总额 */
    @Excel(name = "个人所得税")
    private BigDecimal taxAmount;

    /** 实发工资总额 */
    @Excel(name = "实发工资总额")
    private BigDecimal netAmount;

    /** 人工成本总额（应发+社保+公积金） */
    @Excel(name = "人工成本总额")
    private BigDecimal costAmount;

    /** 年月（yyyy-MM） */
    @Excel(name = "年月")
    private String yearMonth;

    /** 日期（趋势统计用） */
    private String date;

    /** 工资批次号（薪酬发放台账） */
    @Excel(name = "批次号")
    private String batchNo;

    /** 批次名称 */
    @Excel(name = "批次名称")
    private String batchName;

    /** 发放日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发放日期", dateFormat = "yyyy-MM-dd")
    private Date payDate;

    /** 发放银行 */
    @Excel(name = "发放银行")
    private String bankName;

    /** 发放人数 */
    @Excel(name = "发放人数")
    private Integer totalCount;

    /** 批次金额 */
    @Excel(name = "批次金额")
    private BigDecimal totalAmount;

    /** 审批状态（0待审批 1已审核 2已驳回） */
    @Excel(name = "审批状态", readConverterExp = "0=待审批,1=已审核,2=已驳回")
    private String auditStatus;

    /** 报表类型（导出分流：headcountOrg/flow/attendance/cost/ledger） */
    private String reportType;

    /** 封账状态 */
    private String sealedStatus;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Integer getMaleCount() { return maleCount; }
    public void setMaleCount(Integer maleCount) { this.maleCount = maleCount; }
    public Integer getFemaleCount() { return femaleCount; }
    public void setFemaleCount(Integer femaleCount) { this.femaleCount = femaleCount; }
    public Integer getEntryCount() { return entryCount; }
    public void setEntryCount(Integer entryCount) { this.entryCount = entryCount; }
    public Integer getLeaveCount() { return leaveCount; }
    public void setLeaveCount(Integer leaveCount) { this.leaveCount = leaveCount; }
    public BigDecimal getAttendDays() { return attendDays; }
    public void setAttendDays(BigDecimal attendDays) { this.attendDays = attendDays; }
    public Integer getLateCount() { return lateCount; }
    public void setLateCount(Integer lateCount) { this.lateCount = lateCount; }
    public Integer getEarlyCount() { return earlyCount; }
    public void setEarlyCount(Integer earlyCount) { this.earlyCount = earlyCount; }
    public BigDecimal getAbsentDays() { return absentDays; }
    public void setAbsentDays(BigDecimal absentDays) { this.absentDays = absentDays; }
    public BigDecimal getLeaveDays() { return leaveDays; }
    public void setLeaveDays(BigDecimal leaveDays) { this.leaveDays = leaveDays; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public Integer getNightShiftCount() { return nightShiftCount; }
    public void setNightShiftCount(Integer nightShiftCount) { this.nightShiftCount = nightShiftCount; }
    public Integer getRequiredPeople() { return requiredPeople; }
    public void setRequiredPeople(Integer requiredPeople) { this.requiredPeople = requiredPeople; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getSocialAmount() { return socialAmount; }
    public void setSocialAmount(BigDecimal socialAmount) { this.socialAmount = socialAmount; }
    public BigDecimal getFundAmount() { return fundAmount; }
    public void setFundAmount(BigDecimal fundAmount) { this.fundAmount = fundAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public BigDecimal getCostAmount() { return costAmount; }
    public void setCostAmount(BigDecimal costAmount) { this.costAmount = costAmount; }
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public Date getPayDate() { return payDate; }
    public void setPayDate(Date payDate) { this.payDate = payDate; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getSealedStatus() { return sealedStatus; }
    public void setSealedStatus(String sealedStatus) { this.sealedStatus = sealedStatus; }
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
}
