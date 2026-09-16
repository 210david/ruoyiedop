package com.ruoyi.ai.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.ai.domain.AiCallLog;

/**
 * AI调用审计 Service接口
 *
 * @author ruoyi
 */
public interface IAiAuditService
{
    /** 新增审计日志（自动填充当前登录人，无登录上下文时跳过） */
    public int insertLog(AiCallLog log);

    /** 查询审计日志列表 */
    public List<AiCallLog> selectLogList(AiCallLog log);

    /** 用量统计看板数据（近N天） */
    public Map<String, Object> selectUsageStat(int days);

    /** 按状态统计记录数（状态标签栏数量徽章用） */
    public java.util.List<Map<String, Object>> selectStatusCounts();
}
