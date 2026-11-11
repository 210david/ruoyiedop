package com.ruoyi.flowable.domain.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

/**
 * 办理页手动抄送入参
 *
 * @author ruoyi
 */
public class FlowCcSendDTO
{
    /** 当前任务ID */
    @NotBlank(message = "任务ID不能为空")
    private String taskId;

    /** 抄送接收人用户ID */
    private List<Long> userIds;

    /** 抄送说明（可选） */
    private String comment;

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
