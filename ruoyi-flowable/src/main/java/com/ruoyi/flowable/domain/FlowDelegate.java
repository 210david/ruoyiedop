package com.ruoyi.flowable.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 流程转办记录
 *
 * 与抄送记录（flow_cc）同理：转办是一次明确的操作，必须留痕，
 * 记录「谁在什么时间把哪个环节转给了谁」，用于流程状态 / 流程图展示转办过程。
 *
 * @author ruoyi
 */
public class FlowDelegate
{
    /** 转办记录ID */
    private Long delegateId;

    /** 流程实例ID */
    private String instanceId;

    /** 任务ID */
    private String taskId;

    /** 环节名称 */
    private String taskName;

    /** 转办人ID */
    private Long fromId;

    /** 转办人姓名 */
    private String fromName;

    /** 被转办人ID */
    private Long toId;

    /** 被转办人姓名 */
    private String toName;

    /** 转办说明 */
    private String comment;

    /** 转办时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public Long getDelegateId()
    {
        return delegateId;
    }

    public void setDelegateId(Long delegateId)
    {
        this.delegateId = delegateId;
    }

    public String getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(String instanceId)
    {
        this.instanceId = instanceId;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public Long getFromId()
    {
        return fromId;
    }

    public void setFromId(Long fromId)
    {
        this.fromId = fromId;
    }

    public String getFromName()
    {
        return fromName;
    }

    public void setFromName(String fromName)
    {
        this.fromName = fromName;
    }

    public Long getToId()
    {
        return toId;
    }

    public void setToId(Long toId)
    {
        this.toId = toId;
    }

    public String getToName()
    {
        return toName;
    }

    public void setToName(String toName)
    {
        this.toName = toName;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
