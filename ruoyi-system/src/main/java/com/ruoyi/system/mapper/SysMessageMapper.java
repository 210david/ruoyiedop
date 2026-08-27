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
     * @param message  消息查询条件
     * @param userId   当前用户ID
     * @param roleKeys 当前用户的权限标识列表
     * @return 消息集合
     */
    public List<SysMessage> selectMessageListWithReadStatus(@Param("message") SysMessage message, @Param("userId") Long userId, @Param("roleKeys") List<String> roleKeys);

    /**
     * 查询消息列表总数（简单基表COUNT，不走派生表子查询）
     *
     * @param message  消息查询条件
     * @param userId   当前用户ID
     * @param roleKeys 当前用户的权限标识列表
     * @return 总数
     */
    public long selectMessageListCount(@Param("message") SysMessage message, @Param("userId") Long userId, @Param("roleKeys") List<String> roleKeys);

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
     * @param userId   用户ID
     * @param roleKeys 当前用户的权限标识列表
     * @return 未读数量
     */
    public int selectUnreadCount(@Param("userId") Long userId, @Param("roleKeys") List<String> roleKeys);

    /**
     * 查询当前用户消息列表（首页铃铛用，带已读状态）
     *
     * @param userId   用户ID
     * @param roleKeys 当前用户的权限标识列表
     * @param limit   最多返回条数
     * @return 消息列表
     */
    public List<SysMessage> selectMessageListTop(@Param("userId") Long userId, @Param("roleKeys") List<String> roleKeys, @Param("limit") int limit);

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

    /**
     * 标记指定业务单据的所有消息为已处理
     *
     * @param bizSource 业务来源
     * @param bizId     业务单据ID
     * @return 结果
     */
    public int updateBizHandled(@Param("bizSource") String bizSource, @Param("bizId") Long bizId);
}
