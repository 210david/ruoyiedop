package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mms.domain.MmsCalendar;
import com.ruoyi.mms.mapper.MmsCalendarMapper;
import com.ruoyi.mms.service.IMmsCalendarService;

@Service
public class MmsCalendarServiceImpl implements IMmsCalendarService
{
    @Autowired
    private MmsCalendarMapper calendarMapper;

    @Override
    public List<MmsCalendar> selectCalendarList(MmsCalendar calendar)
    {
        return calendarMapper.selectCalendarList(calendar);
    }

    @Override
    public MmsCalendar selectCalendarById(Long calendarId)
    {
        return calendarMapper.selectCalendarById(calendarId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCalendar(MmsCalendar calendar)
    {
        if (StringUtils.isEmpty(calendar.getStatus()))
        {
            calendar.setStatus("0");
        }
        calendar.setDelFlag("0");
        calendar.setCreateBy(SecurityUtils.getUsername());
        calendar.setCreateTime(DateUtils.getNowDate());
        return calendarMapper.insertCalendar(calendar);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCalendar(MmsCalendar calendar)
    {
        calendar.setUpdateBy(SecurityUtils.getUsername());
        return calendarMapper.updateCalendar(calendar);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCalendarByIds(Long[] calendarIds)
    {
        return calendarMapper.deleteCalendarByIds(calendarIds);
    }
}
