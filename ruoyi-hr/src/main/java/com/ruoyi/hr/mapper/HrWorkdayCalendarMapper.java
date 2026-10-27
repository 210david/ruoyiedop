package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrWorkdayCalendar;
import org.apache.ibatis.annotations.Param;

/**
 * 工作日期维护 Mapper
 */
public interface HrWorkdayCalendarMapper
{
    public HrWorkdayCalendar selectHrWorkdayCalendarById(Long calendarId);

    public List<HrWorkdayCalendar> selectHrWorkdayCalendarList(HrWorkdayCalendar hrWorkdayCalendar);

    public HrWorkdayCalendar selectByDate(@Param("calendarDate") String calendarDate);

    public int insertHrWorkdayCalendar(HrWorkdayCalendar hrWorkdayCalendar);

    public int batchInsertHrWorkdayCalendar(List<HrWorkdayCalendar> list);

    public int updateHrWorkdayCalendar(HrWorkdayCalendar hrWorkdayCalendar);

    public int deleteHrWorkdayCalendarById(Long calendarId);

    public int deleteHrWorkdayCalendarByIds(Long[] calendarIds);

    /** 批量更新日期类型 */
    public int batchUpdateDateType(@Param("calendarIds") Long[] calendarIds, @Param("dateType") String dateType, @Param("holidayName") String holidayName, @Param("updateBy") String updateBy);

    /** 查询某年已存在的日期数量 */
    public int countByYear(@Param("year") Integer year);

    /** 删除某年的所有日历数据（物理删除，用于重新生成） */
    public int deleteByYear(@Param("year") Integer year);
}
