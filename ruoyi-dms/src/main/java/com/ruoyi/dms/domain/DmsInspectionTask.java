package com.ruoyi.dms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/** 点检任务 dms_inspection_task */
public class DmsInspectionTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;
    @Excel(name = "任务编号") private String taskNo;
    private Long routeId;
    @Excel(name = "路线名称") private String routeName;
    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "计划日期", width = 20) private Date planDate;
    private Long inspectorId;
    @Excel(name = "点检人") private String inspectorName;
    @Excel(name = "状态", readConverterExp = "0=待执行,1=执行中,2=已完成,3=已逾期") private String taskStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date completeTime;
    private Integer abnormalCount;
    private String resultData;
    /** 点检照片URL（JSON数组） */
    private String photoUrls;
    private String delFlag;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public Date getPlanDate() { return planDate; }
    public void setPlanDate(Date planDate) { this.planDate = planDate; }
    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }
    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }
    public Integer getAbnormalCount() { return abnormalCount; }
    public void setAbnormalCount(Integer abnormalCount) { this.abnormalCount = abnormalCount; }
    public String getResultData() { return resultData; }
    public void setResultData(String resultData) { this.resultData = resultData; }
    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
