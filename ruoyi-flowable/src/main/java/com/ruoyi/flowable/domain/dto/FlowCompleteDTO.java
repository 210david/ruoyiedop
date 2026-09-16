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

    /** 审批结果：approved=同意 rejected=驳回 */
    private String outcome;

    /** 任务变量（可选） */
    private Map<String, Object> variables;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public Map<String, Object> getVariables() { return variables; }
    public void setVariables(Map<String, Object> variables) { this.variables = variables; }
}
