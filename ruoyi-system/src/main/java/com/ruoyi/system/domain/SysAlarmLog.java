package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 系统告警日志表 sys_alarm_log
 * 
 * @author ruoyi
 */
public class SysAlarmLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 告警类型 */
    @Excel(name = "告警类型")
    private String alarmType;

    /** 告警级别 INFO/WARN/ERROR */
    @Excel(name = "告警级别")
    private String alarmLevel;

    /** 告警标题 */
    @Excel(name = "告警标题")
    private String alarmTitle;

    /** 告警详情 */
    @Excel(name = "告警详情")
    private String alarmDetail;

    /** 状态 0未处理 1已处理 */
    @Excel(name = "状态", readConverterExp = "0=未处理,1=已处理")
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 处理人 */
    private String handleBy;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getAlarmType()
    {
        return alarmType;
    }

    public void setAlarmType(String alarmType)
    {
        this.alarmType = alarmType;
    }

    public String getAlarmLevel()
    {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel)
    {
        this.alarmLevel = alarmLevel;
    }

    public String getAlarmTitle()
    {
        return alarmTitle;
    }

    public void setAlarmTitle(String alarmTitle)
    {
        this.alarmTitle = alarmTitle;
    }

    public String getAlarmDetail()
    {
        return alarmDetail;
    }

    public void setAlarmDetail(String alarmDetail)
    {
        this.alarmDetail = alarmDetail;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public Date getCreateTime()
    {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    public String getHandleBy()
    {
        return handleBy;
    }

    public void setHandleBy(String handleBy)
    {
        this.handleBy = handleBy;
    }
}
