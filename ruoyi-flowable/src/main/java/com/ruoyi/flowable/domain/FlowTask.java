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

    /** 紧急程度（1普通 2紧急 3特急，来自业务表系统字段） */
    private String urgency;

    /** 流程定义Key（用于按流程过滤） */
    private String processDefinitionKey;

    /** 流程实例状态（已办返回）：running 进行中 / completed 已办结 / canceled 已撤销 */
    private String status;

    /** 当前所在环节名称（流程未办结时返回） */
    private String currentTaskName;

    /** 已办页签筛选（仅查询参数）：all 全部 / running 未结束 / finished 已结束 */
    private String statusScope;

    /**
     * 参与状态（流程状态/审批意见展示用）：
     * done 已办理 / pending 待办理 / undone 未办理（是候选人但流程已被他人处理或已走过）
     */
    private String state;

    /**
     * 是否候选人展开行：同一节点多名候选人时，为每人各展开一行；
     * true 表示该行是按候选人身份补出来的，不是实际办理人那一行。
     */
    private Boolean candidate;

    /** 是否「被退回」的待办（true=上游节点驳回退回给本人，待办「退回」页签展示） */
    private Boolean returned;

    /** 退回来源环节（哪个节点退回来的） */
    private String returnFrom;

    /** 退回操作人姓名 */
    private String returnByName;

    /** 退回时间（yyyy-MM-dd HH:mm:ss） */
    private String returnTime;

    /** 待办查询条件：true=仅看被退回的 / false=仅看正常待办 / null=全部 */
    private Boolean returnedOnly;

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
    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
    public String getProcessDefinitionKey() { return processDefinitionKey; }
    public void setProcessDefinitionKey(String processDefinitionKey) { this.processDefinitionKey = processDefinitionKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentTaskName() { return currentTaskName; }
    public void setCurrentTaskName(String currentTaskName) { this.currentTaskName = currentTaskName; }
    public String getStatusScope() { return statusScope; }
    public void setStatusScope(String statusScope) { this.statusScope = statusScope; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Boolean getCandidate() { return candidate; }
    public void setCandidate(Boolean candidate) { this.candidate = candidate; }
    public Boolean getReturned() { return returned; }
    public void setReturned(Boolean returned) { this.returned = returned; }
    public String getReturnFrom() { return returnFrom; }
    public void setReturnFrom(String returnFrom) { this.returnFrom = returnFrom; }
    public String getReturnByName() { return returnByName; }
    public void setReturnByName(String returnByName) { this.returnByName = returnByName; }
    public String getReturnTime() { return returnTime; }
    public void setReturnTime(String returnTime) { this.returnTime = returnTime; }
    public Boolean getReturnedOnly() { return returnedOnly; }
    public void setReturnedOnly(Boolean returnedOnly) { this.returnedOnly = returnedOnly; }
}
