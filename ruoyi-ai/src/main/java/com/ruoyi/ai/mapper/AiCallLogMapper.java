package com.ruoyi.ai.mapper;

import java.util.List;
import com.ruoyi.ai.domain.AiCallLog;

/**
 * AI调用审计日志 Mapper接口
 *
 * @author ruoyi
 */
public interface AiCallLogMapper
{
    /** 新增审计日志 */
    public int insertLog(AiCallLog log);

    /** 查询审计日志列表 */
    public List<AiCallLog> selectLogList(AiCallLog log);

    /** 按模块统计近N天调用量（module, date, total, success） */
    public List<java.util.Map<String, Object>> selectModuleStat(java.util.Map<String, Object> params);

    /** 按功能统计调用次数 */
    public List<java.util.Map<String, Object>> selectFeatureStat(java.util.Map<String, Object> params);

    /** 总计统计（总次数/成功/降级/失败/限额/平均耗时） */
    public java.util.Map<String, Object> selectTotalStat(java.util.Map<String, Object> params);
}
