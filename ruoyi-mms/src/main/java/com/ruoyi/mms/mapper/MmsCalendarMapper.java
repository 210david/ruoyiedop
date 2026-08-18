package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsCalendar;

public interface MmsCalendarMapper
{
    public List<MmsCalendar> selectCalendarList(MmsCalendar calendar);
    public MmsCalendar selectCalendarById(Long calendarId);
    public int insertCalendar(MmsCalendar calendar);
    public int updateCalendar(MmsCalendar calendar);
    public int deleteCalendarByIds(Long[] calendarIds);
}
