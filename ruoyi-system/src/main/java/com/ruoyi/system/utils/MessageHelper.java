package com.ruoyi.system.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysMessage;
import com.ruoyi.system.service.ISysMessageService;

/**
 * 消息发送工具类
 * 各业务模块 Service 调用此方法发送消息提醒
 * 
 * 消息类型：1=系统通知 2=业务预警 3=审批消息 4=待办事项
 * 消息级别：1=普通 2=重要 3=紧急
 * 业务来源：wms/pms/mms/qms/safety/dms/mk/system
 *
 * @author ruoyi
 */
@Component
public class MessageHelper
{
    private static final Logger log = LoggerFactory.getLogger(MessageHelper.class);

    @Autowired
    private ISysMessageService messageService;

    /**
     * 发送消息提醒（按角色权限分发）
     *
     * @param title            消息标题
     * @param content          消息内容
     * @param type             消息类型：1=系统通知 2=业务预警 3=审批消息 4=待办事项
     * @param level            消息级别：1=普通 2=重要 3=紧急
     * @param bizSource        业务来源：wms/pms/mms/qms/safety/dms/mk/system
     * @param bizId            关联业务单据ID
     * @param redirectUrl      跳转链接
     * @param recipientRoleKey 接收角色权限标识（如 pms:order:approve），为空则全员可见
     */
    public void sendMessage(String title, String content, String type,
            String level, String bizSource, Long bizId,
            String redirectUrl, String recipientRoleKey)
    {
        sendMessage(title, content, type, level, bizSource, bizId, redirectUrl, recipientRoleKey, null, null);
    }

    /**
     * 发送消息提醒（带业务状态）
     */
    public void sendMessage(String title, String content, String type,
            String level, String bizSource, Long bizId,
            String redirectUrl, String recipientRoleKey, String bizStatus)
    {
        sendMessage(title, content, type, level, bizSource, bizId, redirectUrl, recipientRoleKey, bizStatus, null);
    }

    /**
     * 发送消息提醒（完整参数，带业务状态和处理入口名称）
     *
     * @param title            消息标题
     * @param content          消息内容
     * @param type             消息类型
     * @param level            消息级别
     * @param bizSource        业务来源
     * @param bizId            关联业务单据ID
     * @param redirectUrl      跳转链接
     * @param recipientRoleKey 接收角色权限标识
     * @param bizStatus        消息创建时的业务状态
     * @param bizEntryName     业务处理入口名称（如：排产工作台、入库作业等，用于前端展示）
     */
    public void sendMessage(String title, String content, String type,
            String level, String bizSource, Long bizId,
            String redirectUrl, String recipientRoleKey, String bizStatus, String bizEntryName)
    {
        try
        {
            SysMessage message = new SysMessage();
            message.setMessageTitle(title);
            message.setMessageContent(content);
            message.setMessageType(type);
            message.setMessageLevel(level);
            message.setBizSource(bizSource);
            message.setBizId(bizId);
            message.setRedirectUrl(redirectUrl);
            message.setRecipientRoleKey(recipientRoleKey);
            message.setBizStatus(bizStatus);
            message.setBizEntryName(bizEntryName);
            message.setStatus("1"); // 已发送/有效
            // 定时任务线程无登录上下文，降级为system
            String username;
            try
            {
                username = SecurityUtils.getUsername();
            }
            catch (Exception ex)
            {
                username = "system";
            }
            message.setCreateBy(username);
            messageService.insertMessage(message);
            log.info("消息已发送：[{}] bizSource={}, bizId={}, entry={}",
                    title, bizSource, bizId, bizEntryName);
        }
        catch (Exception e)
        {
            // 消息发送失败不影响主业务流程
            log.error("消息发送失败：title={}, bizSource={}, bizId={}", title, bizSource, bizId, e);
        }
    }

    /**
     * 发送全员消息（无角色权限限制）
     */
    public void sendMessageToAll(String title, String content, String type,
            String level, String bizSource, Long bizId, String redirectUrl)
    {
        sendMessage(title, content, type, level, bizSource, bizId, redirectUrl, null);
    }

    /**
     * 标记指定业务单据的所有消息为已处理
     * 业务状态流转后调用此方法（如工单排产后、审批通过后等）
     *
     * @param bizSource 业务来源（mms/wms/pms/qms/safety/dms/mk）
     * @param bizId     业务单据ID
     */
    public void markHandled(String bizSource, Long bizId)
    {
        try
        {
            messageService.markHandled(bizSource, bizId);
            log.info("消息已标记处理：bizSource={}, bizId={}", bizSource, bizId);
        }
        catch (Exception e)
        {
            log.error("标记消息处理失败：bizSource={}, bizId={}", bizSource, bizId, e);
        }
    }
}
