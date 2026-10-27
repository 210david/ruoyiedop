package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsSchedule;

/**
 * 排产计划 Service接口
 *
 * @author ruoyi
 */
public interface IMmsScheduleService
{
    /** 标准CRUD */
    public List<MmsSchedule> selectScheduleList(MmsSchedule schedule);
    public MmsSchedule selectScheduleById(Long scheduleId);
    public int insertSchedule(MmsSchedule schedule);
    public int updateSchedule(MmsSchedule schedule);
    public int deleteScheduleByIds(Long[] scheduleIds);

    /** 排产确认（已废弃：流程简化后直接下达，保留接口兼容） */
    public int confirmSchedule(Long scheduleId);

    /** 排产下达（已废弃：流程简化后直接下达，保留接口兼容） */
    public int releaseSchedule(Long scheduleId);

    /** 排产取消：0(已下达) → 1(已取消) */
    public int cancelSchedule(Long scheduleId, String cancelReason);

    // ========== 甘特图排产工作台 ==========

    /** 按日期范围查询排产任务（不分页，供甘特图渲染） */
    public List<MmsSchedule> selectScheduleByDateRange(String startDate, String endDate);

    /** 拖拽保存：更新排产任务的时间和资源 */
    public int dragUpdateSchedule(MmsSchedule schedule);

    /** 批量排产：将多个工单自动排到指定产能单元（冲突时提示但仍保存） */
    public java.util.Map<String, Object> batchSchedule(List<MmsSchedule> scheduleList);

    /** 冲突检测：检查同一资源同一时段是否有重叠 */
    public boolean checkConflict(Long resourceId, java.util.Date planStart, java.util.Date planEnd, Long excludeScheduleId);

    /** 查询所有有已下达排产记录的工单ID（不限日期范围，供待排产列表排除） */
    public List<Long> selectScheduledWorkOrderIds();
}
