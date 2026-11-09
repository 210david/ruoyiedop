package com.ruoyi.pjm.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * WBS 任务工时 pjm_task_worklog
 */
public class PjmTaskWorklog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long worklogId;
    private String worklogNo;
    private Long taskId;
    private Long projectId;
    private Long userId;
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date workDate;
    private BigDecimal workHours;
    private String workContent;
    /** 工时附件（多个url逗号分隔） */
    private String attachment;
    private String delFlag;

    private String projectName;
    private String taskName;

    public Long getWorklogId() { return worklogId; }
    public void setWorklogId(Long worklogId) { this.worklogId = worklogId; }
    public String getWorklogNo() { return worklogNo; }
    public void setWorklogNo(String worklogNo) { this.worklogNo = worklogNo; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }
    public BigDecimal getWorkHours() { return workHours; }
    public void setWorkHours(BigDecimal workHours) { this.workHours = workHours; }
    public String getWorkContent() { return workContent; }
    public void setWorkContent(String workContent) { this.workContent = workContent; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
}
