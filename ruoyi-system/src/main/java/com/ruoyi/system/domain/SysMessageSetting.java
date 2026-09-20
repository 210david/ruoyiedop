package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息通知设置 sys_message_setting
 * 按用户隔离：每人一行，控制顶部铃铛对各消息类型的提醒开关
 *
 * @author ruoyi
 */
public class SysMessageSetting implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 系统通知提醒开关（1开 0关） */
    private String notifySystem;

    /** 业务预警提醒开关（1开 0关） */
    private String notifyAlert;

    /** 审批消息提醒开关（1开 0关） */
    private String notifyAudit;

    /** 待办事项提醒开关（1开 0关） */
    private String notifyTodo;

    /** 更新时间 */
    private Date updateTime;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getNotifySystem() { return notifySystem; }
    public void setNotifySystem(String notifySystem) { this.notifySystem = notifySystem; }
    public String getNotifyAlert() { return notifyAlert; }
    public void setNotifyAlert(String notifyAlert) { this.notifyAlert = notifyAlert; }
    public String getNotifyAudit() { return notifyAudit; }
    public void setNotifyAudit(String notifyAudit) { this.notifyAudit = notifyAudit; }
    public String getNotifyTodo() { return notifyTodo; }
    public void setNotifyTodo(String notifyTodo) { this.notifyTodo = notifyTodo; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
