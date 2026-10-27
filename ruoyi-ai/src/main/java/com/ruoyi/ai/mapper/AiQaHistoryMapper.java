package com.ruoyi.ai.mapper;

import java.util.List;
import com.ruoyi.ai.domain.AiQaHistory;

/**
 * AI问答历史Mapper
 *
 * @author ruoyi
 */
public interface AiQaHistoryMapper
{
    /** 新增问答记录（仅本人数据） */
    public int insertHistory(AiQaHistory history);

    /** 查询用户本人的历史记录（按时间倒序） */
    public List<AiQaHistory> selectHistoryByUser(AiQaHistory query);

    /** 清空用户本人的历史记录 */
    public int deleteHistoryByUser(Long userId);

    /** 本月提问总数（配置页双口径统计：提问次数 vs 调用次数） */
    public int selectMonthlyCount();
}
