package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 消息中心表 sys_message
 *
 * @author ruoyi
 */
public class SysMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long messageId;

    /** 消息标题 */
    private String messageTitle;

    /** 消息类型（1系统通知 2业务预警 3审批消息 4待办事项） */
    private String messageType;

    /** 消息内容 */
    private String messageContent;

    /** 消息级别（1普通 2重要 3紧急） */
    private String messageLevel;

    /** 业务来源（如：workorder、inventory、quality等） */
    private String bizSource;

    /** 业务ID（关联的业务单据ID） */
    private Long bizId;

    /** 跳转链接（点击消息后跳转的前端路由） */
    private String redirectUrl;

    /** 接收人ID（为空则全员可见） */
    private Long recipientId;

    /** 接收角色权限标识（为空则全员可见，否则只有拥有该权限的角色对应用户可见） */
    private String recipientRoleKey;

    /** 消息状态（0待发送 1已发送 2已撤回） */
    private String status;

    /** 消息创建时的业务状态（用于判断业务是否已流转过该提醒节点） */
    private String bizStatus;

    /** 是否已读 */
    @JsonProperty("isRead")
    private boolean isRead;

    /** 业务是否已处理（0未处理 1已处理，业务状态流转时更新） */
    @JsonProperty("bizHandled")
    private String bizHandled;

    /** 业务处理入口名称（如：排产工作台、入库作业等，用于前端展示） */
    private String bizEntryName;

    /** 阅读时间 */
    private java.util.Date readTime;

    /** 阅读状态查询条件（0未读 1已读） */
    private String readStatus;

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    @NotBlank(message = "消息标题不能为空")
    @Size(min = 0, max = 200, message = "消息标题不能超过200个字符")
    public String getMessageTitle()
    {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle)
    {
        this.messageTitle = messageTitle;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public String getMessageContent()
    {
        return messageContent;
    }

    public void setMessageContent(String messageContent)
    {
        this.messageContent = messageContent;
    }

    public String getMessageLevel()
    {
        return messageLevel;
    }

    public void setMessageLevel(String messageLevel)
    {
        this.messageLevel = messageLevel;
    }

    public String getBizSource()
    {
        return bizSource;
    }

    public void setBizSource(String bizSource)
    {
        this.bizSource = bizSource;
    }

    public Long getBizId()
    {
        return bizId;
    }

    public void setBizId(Long bizId)
    {
        this.bizId = bizId;
    }

    public String getRedirectUrl()
    {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl)
    {
        this.redirectUrl = redirectUrl;
    }

    public Long getRecipientId()
    {
        return recipientId;
    }

    public void setRecipientId(Long recipientId)
    {
        this.recipientId = recipientId;
    }

    public String getRecipientRoleKey()
    {
        return recipientRoleKey;
    }

    public void setRecipientRoleKey(String recipientRoleKey)
    {
        this.recipientRoleKey = recipientRoleKey;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public boolean getIsRead()
    {
        return isRead;
    }

    public void setIsRead(boolean isRead)
    {
        this.isRead = isRead;
    }

    public java.util.Date getReadTime()
    {
        return readTime;
    }

    public void setReadTime(java.util.Date readTime)
    {
        this.readTime = readTime;
    }

    public String getReadStatus()
    {
        return readStatus;
    }

    public void setReadStatus(String readStatus)
    {
        this.readStatus = readStatus;
    }

    public String getBizStatus()
    {
        return bizStatus;
    }

    public void setBizStatus(String bizStatus)
    {
        this.bizStatus = bizStatus;
    }

    public String getBizHandled()
    {
        return bizHandled;
    }

    public void setBizHandled(String bizHandled)
    {
        this.bizHandled = bizHandled;
    }

    public String getBizEntryName()
    {
        return bizEntryName;
    }

    public void setBizEntryName(String bizEntryName)
    {
        this.bizEntryName = bizEntryName;
    }
}
