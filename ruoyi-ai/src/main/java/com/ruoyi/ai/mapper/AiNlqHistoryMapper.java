package com.ruoyi.ai.mapper;

import java.util.List;
import com.ruoyi.ai.domain.AiNlqHistory;

/**
 * AI数据查询历史Mapper
 *
 * @author ruoyi
 */
public interface AiNlqHistoryMapper
{
    /** 新增数据查询记录（仅本人数据） */
    public int insertHistory(AiNlqHistory history);

    /** 查询用户本人的历史记录（按时间倒序） */
    public List<AiNlqHistory> selectHistoryByUser(AiNlqHistory query);

    /** 清空用户本人的历史记录 */
    public int deleteHistoryByUser(Long userId);

    /** 本月提问总数（配置页双口径统计） */
    public int selectMonthlyCount();
}
