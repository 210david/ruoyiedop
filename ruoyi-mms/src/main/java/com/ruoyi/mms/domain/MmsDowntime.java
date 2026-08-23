package com.ruoyi.mms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 停机记录 mms_downtime
 *
 * @author ruoyi
 */
public class MmsDowntime extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long downtimeId;

    @Excel(name = "停机单号")
    private String downtimeNo;

    private Long dispatchId;

    /** 关联异常单ID */
    private Long abnormalId;

    /** 关联异常单号(冗余) */
    @Excel(name = "关联异常单号")
    private String abnormalNo;

    private Long workOrderId;

    @Excel(name = "产能单元")
    private String resourceName;

    private Long resourceId;

    @Excel(name = "开始时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @Excel(name = "结束时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 停机类型（计划停机/故障停机/换型停机/物料停机/其他停机） */
    @Excel(name = "停机类型", readConverterExp = "0=计划停机,1=故障停机,2=换型停机,3=物料停机,9=其他停机")
    private String dtType;

    /** 停机分类（0=计划停机,1=非计划停机）——EMS标准分类 */
    @Excel(name = "停机分类", readConverterExp = "0=计划停机,1=非计划停机")
    private String dtCategory;

    /** 停机级别（0=一般,1=重要,2=紧急） */
    @Excel(name = "停机级别", readConverterExp = "0=一般,1=重要,2=紧急")
    private String dtLevel;

    @Excel(name = "停机原因")
    private String reason;

    @Excel(name = "停机时长(分钟)")
    private Integer minutes;

    /** 状态（0=停机中,1=已恢复） */
    @Excel(name = "状态", readConverterExp = "0=停机中,1=已恢复")
    private String status;

    /** 上报人 */
    @Excel(name = "上报人")
    private String reportBy;

    /** 处理人 */
    @Excel(name = "处理人")
    private String handleBy;

    /** 处理结果 */
    @Excel(name = "处理结果")
    private String handleResult;

    private String delFlag;

    public Long getDowntimeId() { return downtimeId; }
    public void setDowntimeId(Long downtimeId) { this.downtimeId = downtimeId; }
    public String getDowntimeNo() { return downtimeNo; }
    public void setDowntimeNo(String downtimeNo) { this.downtimeNo = downtimeNo; }
    public Long getDispatchId() { return dispatchId; }
    public void setDispatchId(Long dispatchId) { this.dispatchId = dispatchId; }
    public Long getAbnormalId() { return abnormalId; }
    public void setAbnormalId(Long abnormalId) { this.abnormalId = abnormalId; }
    public String getAbnormalNo() { return abnormalNo; }
    public void setAbnormalNo(String abnormalNo) { this.abnormalNo = abnormalNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getDtType() { return dtType; }
    public void setDtType(String dtType) { this.dtType = dtType; }
    public String getDtCategory() { return dtCategory; }
    public void setDtCategory(String dtCategory) { this.dtCategory = dtCategory; }
    public String getDtLevel() { return dtLevel; }
    public void setDtLevel(String dtLevel) { this.dtLevel = dtLevel; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReportBy() { return reportBy; }
    public void setReportBy(String reportBy) { this.reportBy = reportBy; }
    public String getHandleBy() { return handleBy; }
    public void setHandleBy(String handleBy) { this.handleBy = handleBy; }
    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String handleResult) { this.handleResult = handleResult; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
