package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class MmsWorkReport extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long reportId;
    @Excel(name = "报工单号") private String reportNo;
    private Long workOrderId;
    @Excel(name = "工单号") private String workOrderNo;
    private Long processId;
    @Excel(name = "工序名称") private String processName;
    private Long resourceId;
    @Excel(name = "产能单元") private String resourceName;
    @Excel(name = "合格数") private BigDecimal goodQty;
    @Excel(name = "不良数") private BigDecimal defectQty;
    @Excel(name = "工时") private BigDecimal workHours;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date reportTime;
    @Excel(name = "报工人") private String reportBy;
    @Excel(name = "班组") private String teamName;
    @Excel(name = "班次") private String shiftName;
    @Excel(name = "首件") private String isFirstPiece;
    @Excel(name = "状态", readConverterExp = "0=待审核,1=已审核,2=已驳回") private String status;
    @Excel(name = "来源", readConverterExp = "1=手动报工,2=派工完工") private String source;
    private String auditBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date auditTime;
    private String auditRemark;
    private String delFlag;

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public String getReportNo() { return reportNo; }
    public void setReportNo(String reportNo) { this.reportNo = reportNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public BigDecimal getGoodQty() { return goodQty; }
    public void setGoodQty(BigDecimal goodQty) { this.goodQty = goodQty; }
    public BigDecimal getDefectQty() { return defectQty; }
    public void setDefectQty(BigDecimal defectQty) { this.defectQty = defectQty; }
    public BigDecimal getWorkHours() { return workHours; }
    public void setWorkHours(BigDecimal workHours) { this.workHours = workHours; }
    public Date getReportTime() { return reportTime; }
    public void setReportTime(Date reportTime) { this.reportTime = reportTime; }
    public String getReportBy() { return reportBy; }
    public void setReportBy(String reportBy) { this.reportBy = reportBy; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public String getIsFirstPiece() { return isFirstPiece; }
    public void setIsFirstPiece(String isFirstPiece) { this.isFirstPiece = isFirstPiece; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
