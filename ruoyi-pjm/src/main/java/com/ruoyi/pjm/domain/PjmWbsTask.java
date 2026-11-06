package com.ruoyi.pjm.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * WBS工作分解任务表 pjm_wbs_task
 */
public class PjmWbsTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;
    private Long projectId;
    private Long parentTaskId;
    private String taskCode;
    private String taskName;
    private String taskType;   // 1工作项 2里程碑 3交付物
    private String status;     // 0未开始 1进行中 2完成 3取消 4延期
    private String priority;
    private BigDecimal progress;

    /** ★ 采购关联 */
    private String needPurchase;        // 0否 1是
    private Long purchaseRequestId;

    private Long ownerId;
    private String ownerName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualEndDate;

    private BigDecimal planHours;
    private BigDecimal actualHours;
    private String description;
    private String delFlag;

    /** 子任务列表（树结构组装） */
    private List<PjmWbsTask> children;

    // ============ getter/setter ============
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(Long parentTaskId) { this.parentTaskId = parentTaskId; }
    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public BigDecimal getProgress() { return progress; }
    public void setProgress(BigDecimal progress) { this.progress = progress; }
    public String getNeedPurchase() { return needPurchase; }
    public void setNeedPurchase(String needPurchase) { this.needPurchase = needPurchase; }
    public Long getPurchaseRequestId() { return purchaseRequestId; }
    public void setPurchaseRequestId(Long purchaseRequestId) { this.purchaseRequestId = purchaseRequestId; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Date getPlanStartDate() { return planStartDate; }
    public void setPlanStartDate(Date planStartDate) { this.planStartDate = planStartDate; }
    public Date getPlanEndDate() { return planEndDate; }
    public void setPlanEndDate(Date planEndDate) { this.planEndDate = planEndDate; }
    public Date getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(Date actualStartDate) { this.actualStartDate = actualStartDate; }
    public Date getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(Date actualEndDate) { this.actualEndDate = actualEndDate; }
    public BigDecimal getPlanHours() { return planHours; }
    public void setPlanHours(BigDecimal planHours) { this.planHours = planHours; }
    public BigDecimal getActualHours() { return actualHours; }
    public void setActualHours(BigDecimal actualHours) { this.actualHours = actualHours; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PjmWbsTask> getChildren() { return children; }
    public void setChildren(List<PjmWbsTask> children) { this.children = children; }
}
