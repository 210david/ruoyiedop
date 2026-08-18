package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工厂日历 mms_factory_calendar
 *
 * @author ruoyi
 */
public class MmsCalendar extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long calendarId;

    @Excel(name = "工作日期", dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date workDate;

    @Excel(name = "日期类型", readConverterExp = "0=工作日,1=休息日,2=节假日")
    private String dayType;

    private String shiftJson;

    @Excel(name = "可用工时")
    private BigDecimal availHours;

    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    private String delFlag;

    public Long getCalendarId() { return calendarId; }
    public void setCalendarId(Long calendarId) { this.calendarId = calendarId; }
    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }
    public String getDayType() { return dayType; }
    public void setDayType(String dayType) { this.dayType = dayType; }
    public String getShiftJson() { return shiftJson; }
    public void setShiftJson(String shiftJson) { this.shiftJson = shiftJson; }
    public BigDecimal getAvailHours() { return availHours; }
    public void setAvailHours(BigDecimal availHours) { this.availHours = availHours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
