package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrWorkdayCalendar;

/**
 * 工作日期维护 Service接口
 */
public interface IHrWorkdayCalendarService
{
    public HrWorkdayCalendar selectHrWorkdayCalendarById(Long calendarId);

    public List<HrWorkdayCalendar> selectHrWorkdayCalendarList(HrWorkdayCalendar hrWorkdayCalendar);

    public int insertHrWorkdayCalendar(HrWorkdayCalendar hrWorkdayCalendar);

    public int updateHrWorkdayCalendar(HrWorkdayCalendar hrWorkdayCalendar);

    public int deleteHrWorkdayCalendarByIds(Long[] calendarIds);

    public int deleteHrWorkdayCalendarById(Long calendarId);

    /**
     * 生成年度工作日历
     * @param year 年份
     * @param weekendRest 周末是否休息（true=周六日休息，false=全工作日）
     * @param defaultShiftId 默认班次ID
     * @param defaultShiftName 默认班次名称
     * @param createBy 创建者
     * @return 生成记录数
     */
    public int generateYearlyCalendar(Integer year, boolean weekendRest, Long defaultShiftId, String defaultShiftName, String createBy);

    /**
     * 批量调整日期类型
     */
    public int batchAdjustDateType(Long[] calendarIds, String dateType, String holidayName, String updateBy);
}
