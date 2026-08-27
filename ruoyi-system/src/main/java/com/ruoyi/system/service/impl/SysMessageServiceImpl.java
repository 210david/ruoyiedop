package com.ruoyi.system.service.impl;

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
    public int selectUnreadCount(Long userId, List<String> roleKeys)
    {
        return messageMapper.selectUnreadCount(userId, roleKeys);
    }

    @Override
    public List<SysMessage> selectMessageListTop(Long userId, List<String> roleKeys, int limit)
    {
        return messageMapper.selectMessageListTop(userId, roleKeys, limit);
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
