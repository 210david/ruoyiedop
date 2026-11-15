package com.ruoyi.flowable.domain.dto;

import java.util.Map;
import jakarta.validation.constraints.NotBlank;

/**
 * 审批操作入参
 *
 * @author ruoyi
 */
public class FlowCompleteDTO
{
    /** 任务ID */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /** 审批意见 */
    private String comment;

    /** 审批结果：approved=同意 rejected=驳回（退回） */
    private String outcome;

    /**
     * 退回目标节点ID（outcome=rejected 时必填）
     *
     * 取值来源：办理页「驳回」弹窗里列出的可退回节点（/flowable/task/prevNodes/{taskId}），
     * 可以是本实例已经走过的用户任务节点，也可以是「提交申请」发起人节点（部署时注入）。
     */
    private String targetActivityId;

    /** 任务变量（可选） */
    private Map<String, Object> variables;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getTargetActivityId() { return targetActivityId; }
    public void setTargetActivityId(String targetActivityId) { this.targetActivityId = targetActivityId; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}
