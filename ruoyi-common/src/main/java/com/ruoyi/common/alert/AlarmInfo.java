package com.ruoyi.common.alert;

import java.util.Date;

/**
 * 告警信息实体
 * 
 * @author ruoyi
 */
public class AlarmInfo
{
    /** 告警类型 */
    private String type;

    /** 告警级别 INFO/WARN/ERROR */
    private String level;

    /** 告警标题 */
    private String title;

    /** 告警内容 */
    private String content;

    /** 告警时间 */
    private Date time;

    public AlarmInfo()
    {
    }

    public AlarmInfo(String type, String level, String title, String content)
    {
        this.type = type;
        this.level = level;
        this.title = title;
        this.content = content;
        this.time = new Date();
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getLevel()
    {
        return level;
    }

    public void setLevel(String level)
    {
        this.level = level;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public Date getTime()
    {
        return time;
    }

    public void setTime(Date time)
    {
        this.time = time;
    }

    @Override
    public String toString()
    {
        return "AlarmInfo{" +
                "type='" + type + '\'' +
                ", level='" + level + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", time=" + time +
                '}';
    }
}
