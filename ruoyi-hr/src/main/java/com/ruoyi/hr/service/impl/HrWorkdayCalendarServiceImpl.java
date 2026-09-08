package com.ruoyi.hr.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.hr.domain.HrWorkdayCalendar;
import com.ruoyi.hr.mapper.HrWorkdayCalendarMapper;
import com.ruoyi.hr.service.IHrWorkdayCalendarService;

/**
 * 工作日期维护 Service实现
 */
@Service
public class HrWorkdayCalendarServiceImpl implements IHrWorkdayCalendarService
{
    @Autowired
    private HrWorkdayCalendarMapper hrWorkdayCalendarMapper;

    @Override
    public HrWorkdayCalendar selectHrWorkdayCalendarById(Long calendarId)
    {
        return hrWorkdayCalendarMapper.selectHrWorkdayCalendarById(calendarId);
    }

    @Override
    public List<HrWorkdayCalendar> selectHrWorkdayCalendarList(HrWorkdayCalendar hrWorkdayCalendar)
    {
        return hrWorkdayCalendarMapper.selectHrWorkdayCalendarList(hrWorkdayCalendar);
    }

    @Override
    public int insertHrWorkdayCalendar(HrWorkdayCalendar hrWorkdayCalendar)
    {
        preFillFields(hrWorkdayCalendar);
        // 检查日期是否已存在
        String dateStr = formatDate(hrWorkdayCalendar.getCalendarDate());
        HrWorkdayCalendar existing = hrWorkdayCalendarMapper.selectByDate(dateStr);
        if (existing != null)
        {
            throw new ServiceException("日期 " + dateStr + " 的工作日历已存在，请勿重复添加");
        }
        return hrWorkdayCalendarMapper.insertHrWorkdayCalendar(hrWorkdayCalendar);
    }

    @Override
    public int updateHrWorkdayCalendar(HrWorkdayCalendar hrWorkdayCalendar)
    {
        preFillFields(hrWorkdayCalendar);
        return hrWorkdayCalendarMapper.updateHrWorkdayCalendar(hrWorkdayCalendar);
    }

    @Override
    public int deleteHrWorkdayCalendarByIds(Long[] calendarIds)
    {
        return hrWorkdayCalendarMapper.deleteHrWorkdayCalendarByIds(calendarIds);
    }

    @Override
    public int deleteHrWorkdayCalendarById(Long calendarId)
    {
        return hrWorkdayCalendarMapper.deleteHrWorkdayCalendarById(calendarId);
    }

    @Override
    @Transactional
    public int generateYearlyCalendar(Integer year, boolean weekendRest, Long defaultShiftId, String defaultShiftName, String createBy)
    {
        if (year == null || year < 2000 || year > 2100)
        {
            throw new ServiceException("年份不合法");
        }
        // 若该年份已有数据，先物理删除再重新生成
        int existing = hrWorkdayCalendarMapper.countByYear(year);
        if (existing > 0)
        {
            hrWorkdayCalendarMapper.deleteByYear(year);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<HrWorkdayCalendar> list = new ArrayList<>();
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_YEAR);

        for (int i = 0; i < maxDay; i++)
        {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday, ..., 7=Saturday
            int weekDay = dayOfWeek == 1 ? 7 : dayOfWeek - 1; // 转为1=周一...7=周日

            HrWorkdayCalendar calendar = new HrWorkdayCalendar();
            calendar.setCalendarDate(cal.getTime());
            calendar.setYear(year);
            calendar.setMonth(cal.get(Calendar.MONTH) + 1);
            calendar.setWeekDay(weekDay);

            if (weekendRest && (weekDay == 6 || weekDay == 7))
            {
                calendar.setDateType("1"); // 休息日
            }
            else
            {
                calendar.setDateType("0"); // 工作日
                calendar.setShiftId(defaultShiftId);
                calendar.setShiftName(defaultShiftName);
            }
            calendar.setStatus("0");
            calendar.setDelFlag("0");
            calendar.setCreateBy(createBy);
            list.add(calendar);

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // 批量插入，每500条一批
        int total = 0;
        int batchSize = 500;
        for (int i = 0; i < list.size(); i += batchSize)
        {
            int end = Math.min(i + batchSize, list.size());
            List<HrWorkdayCalendar> batch = list.subList(i, end);
            total += hrWorkdayCalendarMapper.batchInsertHrWorkdayCalendar(batch);
        }
        return total;
    }

    @Override
    public int batchAdjustDateType(Long[] calendarIds, String dateType, String holidayName, String updateBy)
    {
        return hrWorkdayCalendarMapper.batchUpdateDateType(calendarIds, dateType, holidayName, updateBy);
    }

    /**
     * 自动填充年份、月份、星期等字段
     */
    private void preFillFields(HrWorkdayCalendar calendar)
    {
        if (calendar.getCalendarDate() != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(calendar.getCalendarDate());
            if (calendar.getYear() == null)
            {
                calendar.setYear(cal.get(Calendar.YEAR));
            }
            if (calendar.getMonth() == null)
            {
                calendar.setMonth(cal.get(Calendar.MONTH) + 1);
            }
            if (calendar.getWeekDay() == null)
            {
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                calendar.setWeekDay(dayOfWeek == 1 ? 7 : dayOfWeek - 1);
            }
        }
    }

    /**
     * 格式化日期为 yyyy-MM-dd
     */
    private String formatDate(java.util.Date date)
    {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
