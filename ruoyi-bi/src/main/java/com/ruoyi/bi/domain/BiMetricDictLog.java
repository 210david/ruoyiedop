package com.ruoyi.bi.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * BI指标字典变更留痕对象 bi_metric_dict_log
 *
 * 口径是决策数据的"法律条文"，任何变更必须留痕可追溯（PRD F1.2）。
 *
 * @author ruoyi
 */
public class BiMetricDictLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 指标编码 */
    private String metricCode;

    /** 变更类型（INSERT新增/UPDATE修改/DELETE删除） */
    private String changeType;

    /** 变更字段名 */
    private String fieldName;

    /** 旧值 */
    private String oldValue;

    /** 新值 */
    private String newValue;

    /** 变更人 */
    private String changeBy;

    /** 变更时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date changeTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getMetricCode()
    {
        return metricCode;
    }

    public void setMetricCode(String metricCode)
    {
        this.metricCode = metricCode;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public void setOldValue(String oldValue)
    {
        this.oldValue = oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    public String getChangeBy()
    {
        return changeBy;
    }

    public void setChangeBy(String changeBy)
    {
        this.changeBy = changeBy;
    }

    public Date getChangeTime()
    {
        return changeTime;
    }

    public void setChangeTime(Date changeTime)
    {
        this.changeTime = changeTime;
    }
}
