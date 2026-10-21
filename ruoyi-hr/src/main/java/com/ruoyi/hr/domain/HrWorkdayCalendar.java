package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotNull;

/**
 * 工作日期维护 对象 biz_hr_workday_calendar
 * 用于管理企业运营日历，标记每个日期是工作日、休息日还是法定节假日
 */
public class HrWorkdayCalendar extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日历ID */
    private Long calendarId;

    /** 日期 */
    @NotNull(message = "日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "日期", dateFormat = "yyyy-MM-dd")
    private Date calendarDate;

    /** 年份 */
    @Excel(name = "年份")
    private Integer year;

    /** 月份 */
    @Excel(name = "月份")
    private Integer month;

    /** 星期(1-7, 1=周一) */
    @Excel(name = "星期")
    private Integer weekDay;

    /** 日期类型(0=工作日, 1=休息日, 2=法定节假日) */
    @Excel(name = "日期类型")
    private String dateType;

    /** 班次ID(工作日关联的班次) */
    private Long shiftId;

    /** 班次名称 */
    @Excel(name = "班次名称")
    private String shiftName;

    /** 节假日名称 */
    @Excel(name = "节假日名称")
    private String holidayName;

    /** 状态(0=启用 1=停用) */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getCalendarId() { return calendarId; }
    public void setCalendarId(Long calendarId) { this.calendarId = calendarId; }
    public Date getCalendarDate() { return calendarDate; }
    public void setCalendarDate(Date calendarDate) { this.calendarDate = calendarDate; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }
    public Integer getWeekDay() { return weekDay; }
    public void setWeekDay(Integer weekDay) { this.weekDay = weekDay; }
    public String getDateType() { return dateType; }
    public void setDateType(String dateType) { this.dateType = dateType; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public String getHolidayName() { return holidayName; }
    public void setHolidayName(String holidayName) { this.holidayName = holidayName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
