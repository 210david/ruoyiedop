package com.ruoyi.flowable.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程任务视图对象（待办/已办）
 *
 * @author ruoyi
 */
public class FlowTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private String taskId;

    /** 任务名称 */
    private String taskName;

    /** 任务定义Key（BPMN 节点ID） */
    private String taskDefinitionKey;

    /** 流程实例ID */
    private String instanceId;

    /** 流程定义ID */
    private String processDefinitionId;

    /** 流程定义名称 */
    private String processDefinitionName;

    /** 业务主键 */
    private String businessKey;

    /** 办理人ID */
    private Long assigneeId;

    /** 办理人姓名 */
    private String assigneeName;

    /** 发起人姓名 */
    private String startUserName;

    /** 任务创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 任务完成时间（已办返回） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 审批意见（已办返回） */
    private String comment;

    /** 审批结果（已办返回）：approved/rejected */
    private String outcome;

    /** 流程标题（流程实例名称，发起时生成） */
    private String title;

    /** 流程定义Key（用于按流程过滤） */
    private String processDefinitionKey;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public String getTaskDefinitionKey() { return taskDefinitionKey; }
    public void setTaskDefinitionKey(String taskDefinitionKey) { this.taskDefinitionKey = taskDefinitionKey; }
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    public String getProcessDefinitionId() { return processDefinitionId; }
    public void setProcessDefinitionId(String processDefinitionId) { this.processDefinitionId = processDefinitionId; }
    public String getProcessDefinitionName() { return processDefinitionName; }
    public void setProcessDefinitionName(String processDefinitionName) { this.processDefinitionName = processDefinitionName; }
    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public String getStartUserName() { return startUserName; }
    public void setStartUserName(String startUserName) { this.startUserName = startUserName; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getProcessDefinitionKey() { return processDefinitionKey; }
    public void setProcessDefinitionKey(String processDefinitionKey) { this.processDefinitionKey = processDefinitionKey; }
}
