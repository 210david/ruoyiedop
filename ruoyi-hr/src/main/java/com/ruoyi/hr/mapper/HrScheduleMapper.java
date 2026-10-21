package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrSchedule;
import org.apache.ibatis.annotations.Param;

public interface HrScheduleMapper {
    public HrSchedule selectHrScheduleById(Long scheduleId);
    public List<HrSchedule> selectHrScheduleList(HrSchedule hrSchedule);
    public int insertHrSchedule(HrSchedule hrSchedule);
    public int updateHrSchedule(HrSchedule hrSchedule);
    public int deleteHrScheduleByIds(Long[] scheduleIds);
    public int deleteHrScheduleById(Long scheduleId);

    /** 根据工号+日期查询排班（考勤导入时自动匹配班次） */
    public HrSchedule selectByEmployeeNoAndDate(@Param("employeeNo") String employeeNo, @Param("scheduleDate") String scheduleDate);

    /** 批量插入排班 */
    public int batchInsertHrSchedule(@Param("list") List<HrSchedule> list);

    /** 查询某日期范围内已存在的排班（用于去重） */
    public List<HrSchedule> selectExistingSchedule(@Param("employeeId") Long employeeId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    /** 按员工+日期范围查询月度排班日历 */
    public List<HrSchedule> selectScheduleCalendar(@Param("employeeIds") List<Long> employeeIds, @Param("orgId") Long orgId, @Param("startDate") String startDate, @Param("endDate") String endDate);

    /** 按日期范围+员工ID删除排班（批量清除） */
    public int deleteScheduleByRange(@Param("employeeIds") List<Long> employeeIds, @Param("orgId") Long orgId, @Param("startDate") String startDate, @Param("endDate") String endDate);
}
