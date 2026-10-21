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

    /** 按状态统计记录数（状态标签栏数量徽章用） */
    public java.util.List<java.util.Map<String, Object>> selectStatusCounts();

    /** 按日期统计调用量与耗时趋势 */
    public List<java.util.Map<String, Object>> selectDailyTrend(java.util.Map<String, Object> params);

    /** 按操作人统计（TOP 15） */
    public List<java.util.Map<String, Object>> selectUserStat(java.util.Map<String, Object> params);

    /** 按小时时段统计 */
    public List<java.util.Map<String, Object>> selectHourStat(java.util.Map<String, Object> params);

    /** 状态分布统计 */
    public List<java.util.Map<String, Object>> selectStatusStat(java.util.Map<String, Object> params);

    /** 模块×日期 联合趋势（堆叠面积图用） */
    public List<java.util.Map<String, Object>> selectModuleDailyStat(java.util.Map<String, Object> params);
}
