package com.ruoyi.mms.mapper;

import java.util.Date;
import java.util.List;
import com.ruoyi.mms.domain.MmsSchedule;

public interface MmsScheduleMapper {
    public List<MmsSchedule> selectScheduleList(MmsSchedule schedule);
    public MmsSchedule selectScheduleById(Long scheduleId);
    public int insertSchedule(MmsSchedule schedule);
    public int updateSchedule(MmsSchedule schedule);
    public int deleteScheduleByIds(Long[] scheduleIds);

    /** 按日期范围查询排产任务（不分页） */
    public List<MmsSchedule> selectScheduleByDateRange(@org.apache.ibatis.annotations.Param("startDate") String startDate,
                                                       @org.apache.ibatis.annotations.Param("endDate") String endDate);

    /** 冲突检测：同一资源同一时段是否有重叠（排除自身） */
    public int countConflict(@org.apache.ibatis.annotations.Param("resourceId") Long resourceId,
                             @org.apache.ibatis.annotations.Param("planStart") Date planStart,
                             @org.apache.ibatis.annotations.Param("planEnd") Date planEnd,
                             @org.apache.ibatis.annotations.Param("excludeScheduleId") Long excludeScheduleId);

    /** 查询所有有已下达排产记录的工单ID（不限日期范围） */
    public List<Long> selectScheduledWorkOrderIds();

    /** 按工单ID批量取消排产记录（将已下达的排产改为已取消） */
    public int cancelSchedulesByWorkOrderIds(@org.apache.ibatis.annotations.Param("workOrderIds") Long[] workOrderIds);
}
