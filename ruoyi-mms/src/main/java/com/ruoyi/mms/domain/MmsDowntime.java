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

    @Excel(name = "停机类型")
    private String dtType;

    @Excel(name = "停机原因")
    private String reason;

    @Excel(name = "停机时长(分钟)")
    private Integer minutes;

    @Excel(name = "状态", readConverterExp = "0=停机中,1=已恢复")
    private String status;

    private String delFlag;

    public Long getDowntimeId() { return downtimeId; }
    public void setDowntimeId(Long downtimeId) { this.downtimeId = downtimeId; }
    public String getDowntimeNo() { return downtimeNo; }
    public void setDowntimeNo(String downtimeNo) { this.downtimeNo = downtimeNo; }
    public Long getDispatchId() { return dispatchId; }
    public void setDispatchId(Long dispatchId) { this.dispatchId = dispatchId; }
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
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
