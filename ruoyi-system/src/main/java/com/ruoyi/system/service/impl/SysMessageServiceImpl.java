package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysMessage;
import com.ruoyi.system.mapper.SysMessageMapper;
import com.ruoyi.system.service.ISysMessageService;

/**
 * 消息中心 服务层实现
 *
 * @author ruoyi
 */
@Service
public class SysMessageServiceImpl implements ISysMessageService
{
    @Autowired
    private SysMessageMapper messageMapper;

    @Override
    public SysMessage selectMessageById(Long messageId)
    {
        return messageMapper.selectMessageById(messageId);
    }

    @Override
    public List<SysMessage> selectMessageListWithReadStatus(SysMessage message, Long userId, List<String> roleKeys)
    {
        return messageMapper.selectMessageListWithReadStatus(message, userId, roleKeys);
    }

    @Override
    public long selectMessageListCount(SysMessage message, Long userId, List<String> roleKeys)
    {
        return messageMapper.selectMessageListCount(message, userId, roleKeys);
    }

    @Override
    public int insertMessage(SysMessage message)
    {
        return messageMapper.insertMessage(message);
    }

    @Override
    public int selectUnreadCount(Long userId, List<String> roleKeys, List<String> notifyTypes)
    {
        return messageMapper.selectUnreadCount(userId, roleKeys, notifyTypes);
    }

    @Override
    public List<SysMessage> selectMessageListTop(Long userId, List<String> roleKeys, int limit, List<String> notifyTypes)
    {
        return messageMapper.selectMessageListTop(userId, roleKeys, limit, notifyTypes);
    }

    // ==================== 通知设置（2026-09-19） ====================

    @Autowired
    private com.ruoyi.system.mapper.SysMessageSettingMapper settingMapper;

    /** 获取用户通知设置（无记录时兜底全开） */
    @Override
    public com.ruoyi.system.domain.SysMessageSetting getNotifySetting(Long userId)
    {
        com.ruoyi.system.domain.SysMessageSetting s = settingMapper.selectSettingByUserId(userId);
        if (s == null)
        {
            s = new com.ruoyi.system.domain.SysMessageSetting();
            s.setUserId(userId);
            s.setNotifySystem("1");
            s.setNotifyAlert("1");
            s.setNotifyAudit("1");
            s.setNotifyTodo("1");
        }
        return s;
    }

    /** 保存用户通知设置（无记录插入，有记录更新） */
    @Override
    public int saveNotifySetting(com.ruoyi.system.domain.SysMessageSetting setting)
    {
        com.ruoyi.system.domain.SysMessageSetting exist = settingMapper.selectSettingByUserId(setting.getUserId());
        if (exist == null)
        {
            return settingMapper.insertSetting(setting);
        }
        return settingMapper.updateSetting(setting);
    }

    /** 由通知设置换算铃铛放行的消息类型列表（null=全放行） */
    @Override
    public List<String> resolveNotifyTypes(com.ruoyi.system.domain.SysMessageSetting s)
    {
        if (s == null)
        {
            return null;
        }
        List<String> types = new ArrayList<>();
        if ("1".equals(s.getNotifySystem()))
        {
            types.add("1");
        }
        if ("1".equals(s.getNotifyAlert()))
        {
            types.add("2");
        }
        if ("1".equals(s.getNotifyAudit()))
        {
            types.add("3");
        }
        if ("1".equals(s.getNotifyTodo()))
        {
            types.add("4");
        }
        // 全关时返回空列表会被 <if> 跳过导致全量放行，违背用户意图；
        // 约定：返回包含不可能匹配的哨兵值，保证空结果
        if (types.isEmpty())
        {
            types.add("-1");
        }
        return types;
    }

    @Override
    public void markRead(Long messageId, Long userId)
    {
        messageMapper.insertMessageRead(messageId, userId);
    }

    @Override
    public void markReadBatch(Long userId, Long[] messageIds)
    {
        if (messageIds == null || messageIds.length == 0)
        {
            return;
        }
        messageMapper.insertMessageReadBatch(userId, messageIds);
    }

    @Override
    public void markHandled(String bizSource, Long bizId)
    {
        messageMapper.updateBizHandled(bizSource, bizId);
    }
}
