package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsCalendar;

public interface IMmsCalendarService
{
    public List<MmsCalendar> selectCalendarList(MmsCalendar calendar);
    public MmsCalendar selectCalendarById(Long calendarId);
    public int insertCalendar(MmsCalendar calendar);
    public int updateCalendar(MmsCalendar calendar);
    public int deleteCalendarByIds(Long[] calendarIds);
}
