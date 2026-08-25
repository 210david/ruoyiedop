package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * OEE快照 mms_oee_snapshot
 *
 * @author ruoyi
 */
public class MmsOeeSnapshot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long snapshotId;

    /** 派工单ID（工序维度） */
    private Long dispatchId;

    /** 工序顺序号 */
    @Excel(name = "工序号")
    private Integer opSeq;

    /** 工序名称 */
    @Excel(name = "工序名称")
    private String processName;

    @Excel(name = "日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date snapshotDate;

    private Long resourceId;

    @Excel(name = "产能单元")
    private String resourceName;

    private Long workOrderId;

    @Excel(name = "工单编号")
    private String workOrderNo;

    @Excel(name = "计划工时")
    private BigDecimal plannedHours;

    @Excel(name = "运行工时")
    private BigDecimal runHours;

    @Excel(name = "停机工时")
    private BigDecimal downtimeHours;

    @Excel(name = "可用率")
    private BigDecimal availability;

    @Excel(name = "表现率")
    private BigDecimal performance;

    @Excel(name = "质量率")
    private BigDecimal qualityRate;

    @Excel(name = "OEE")
    private BigDecimal oee;

    @Excel(name = "计划数量")
    private BigDecimal planQty;

    @Excel(name = "合格数量")
    private BigDecimal goodQty;

    @Excel(name = "不良数量")
    private BigDecimal defectQty;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getDispatchId() { return dispatchId; }
    public void setDispatchId(Long dispatchId) { this.dispatchId = dispatchId; }
    public Integer getOpSeq() { return opSeq; }
    public void setOpSeq(Integer opSeq) { this.opSeq = opSeq; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Date getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(Date snapshotDate) { this.snapshotDate = snapshotDate; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public BigDecimal getPlannedHours() { return plannedHours; }
    public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }
    public BigDecimal getRunHours() { return runHours; }
    public void setRunHours(BigDecimal runHours) { this.runHours = runHours; }
    public BigDecimal getDowntimeHours() { return downtimeHours; }
    public void setDowntimeHours(BigDecimal downtimeHours) { this.downtimeHours = downtimeHours; }
    public BigDecimal getAvailability() { return availability; }
    public void setAvailability(BigDecimal availability) { this.availability = availability; }
    public BigDecimal getPerformance() { return performance; }
    public void setPerformance(BigDecimal performance) { this.performance = performance; }
    public BigDecimal getQualityRate() { return qualityRate; }
    public void setQualityRate(BigDecimal qualityRate) { this.qualityRate = qualityRate; }
    public BigDecimal getOee() { return oee; }
    public void setOee(BigDecimal oee) { this.oee = oee; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public BigDecimal getGoodQty() { return goodQty; }
    public void setGoodQty(BigDecimal goodQty) { this.goodQty = goodQty; }
    public BigDecimal getDefectQty() { return defectQty; }
    public void setDefectQty(BigDecimal defectQty) { this.defectQty = defectQty; }
}
