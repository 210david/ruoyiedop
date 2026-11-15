package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 流程抄送（待阅）对象 flow_cc
 *
 * 对标泛微「抄送」：节点上配置抄送人员后，节点到达或办理完成时生成一条待阅记录，
 * 被抄送人在「待办流程-待阅」可见并可查看流程详情，只读不可办理。
 *
 * @author ruoyi
 */
public class FlowCc extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 抄送ID */
    private Long ccId;

    /** 流程实例ID */
    private String instanceId;

    /** 流程定义Key */
    private String processKey;

    /** 流程名称 */
    private String processName;

    /** 业务主键 */
    private String businessKey;

    /** 流程标题 */
    private String title;

    /** 来源任务ID */
    private String taskId;

    /** 来源节点名称 */
    private String taskName;

    /** 抄送人（触发抄送的人）用户ID */
    private Long senderId;

    /** 抄送人姓名 */
    private String senderName;

    /** 接收人用户ID */
    private Long receiverId;

    /** 接收人姓名 */
    private String receiverName;

    /** 阅读状态（0未读 1已读） */
    private String readFlag;

    /** 阅读时间 */
    private java.util.Date readTime;

    public Long getCcId() { return ccId; }
    public void setCcId(Long ccId) { this.ccId = ccId; }
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    public String getProcessKey() { return processKey; }
    public void setProcessKey(String processKey) { this.processKey = processKey; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReadFlag() { return readFlag; }
    public void setReadFlag(String readFlag) { this.readFlag = readFlag; }
    public java.util.Date getReadTime() { return readTime; }
    public void setReadTime(java.util.Date readTime) { this.readTime = readTime; }
}
