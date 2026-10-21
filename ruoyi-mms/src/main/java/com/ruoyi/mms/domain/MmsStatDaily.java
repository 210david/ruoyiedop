package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 生产日报 mms_stat_daily
 *
 * @author ruoyi
 */
public class MmsStatDaily extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long statId;

    @Excel(name = "生产日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date statDate;

    @Excel(name = "工单数")
    private Integer workOrderCount;

    @Excel(name = "已完工数")
    private Integer completedCount;

    @Excel(name = "在制数")
    private Integer inProgressCount;

    @Excel(name = "异常数")
    private Integer abnormalCount;

    @Excel(name = "计划数量")
    private BigDecimal totalPlanQty;

    @Excel(name = "合格数量")
    private BigDecimal totalGoodQty;

    @Excel(name = "不良数量")
    private BigDecimal totalDefectQty;

    @Excel(name = "平均OEE")
    private BigDecimal avgOee;

    @Excel(name = "报工次数")
    private Integer reportCount;

    public Long getStatId() { return statId; }
    public void setStatId(Long statId) { this.statId = statId; }
    public Date getStatDate() { return statDate; }
    public void setStatDate(Date statDate) { this.statDate = statDate; }
    public Integer getWorkOrderCount() { return workOrderCount; }
    public void setWorkOrderCount(Integer workOrderCount) { this.workOrderCount = workOrderCount; }
    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
    public Integer getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(Integer inProgressCount) { this.inProgressCount = inProgressCount; }
    public Integer getAbnormalCount() { return abnormalCount; }
    public void setAbnormalCount(Integer abnormalCount) { this.abnormalCount = abnormalCount; }
    public BigDecimal getTotalPlanQty() { return totalPlanQty; }
    public void setTotalPlanQty(BigDecimal totalPlanQty) { this.totalPlanQty = totalPlanQty; }
    public BigDecimal getTotalGoodQty() { return totalGoodQty; }
    public void setTotalGoodQty(BigDecimal totalGoodQty) { this.totalGoodQty = totalGoodQty; }
    public BigDecimal getTotalDefectQty() { return totalDefectQty; }
    public void setTotalDefectQty(BigDecimal totalDefectQty) { this.totalDefectQty = totalDefectQty; }
    public BigDecimal getAvgOee() { return avgOee; }
    public void setAvgOee(BigDecimal avgOee) { this.avgOee = avgOee; }
    public Integer getReportCount() { return reportCount; }
    public void setReportCount(Integer reportCount) { this.reportCount = reportCount; }
}
