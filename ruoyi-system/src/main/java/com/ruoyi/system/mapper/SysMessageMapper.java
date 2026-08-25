package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysMessage;
import org.apache.ibatis.annotations.Param;

/**
 * 消息中心 数据层
 *
 * @author ruoyi
 */
public interface SysMessageMapper
{
    /**
     * 查询消息信息
     *
     * @param messageId 消息ID
     * @return 消息信息
     */
    public SysMessage selectMessageById(Long messageId);

    /**
     * 查询消息列表（管理端，带当前用户已读状态）
     *
     * @param message 消息查询条件
     * @param userId  当前用户ID
     * @return 消息集合
     */
    public List<SysMessage> selectMessageListWithReadStatus(@Param("message") SysMessage message, @Param("userId") Long userId);

    /**
     * 新增消息
     *
     * @param message 消息信息
     * @return 结果
     */
    public int insertMessage(SysMessage message);

    /**
     * 查询当前用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    public int selectUnreadCount(@Param("userId") Long userId);

    /**
     * 查询当前用户消息列表（首页铃铛用，带已读状态）
     *
     * @param userId 用户ID
     * @param limit  最多返回条数
     * @return 消息列表
     */
    public List<SysMessage> selectMessageListTop(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 新增消息已读记录（幂等）
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 结果
     */
    public int insertMessageRead(@Param("messageId") Long messageId, @Param("userId") Long userId);

    /**
     * 批量标记已读
     *
     * @param userId     用户ID
     * @param messageIds 消息ID数组
     * @return 结果
     */
    public int insertMessageReadBatch(@Param("userId") Long userId, @Param("messageIds") Long[] messageIds);
}
