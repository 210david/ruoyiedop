package com.ruoyi.bi.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 决策驾驶舱导出行对象（F8 报表导出）
 *
 * 最新快照指标全量行：编码/名称/域/值/单位/口径/基准日。
 *
 * @author ruoyi
 */
public class BiDashboardExport
{
    /** 指标编码 */
    @Excel(name = "指标编码")
    private String metricCode;

    /** 指标名称 */
    @Excel(name = "指标名称")
    private String metricName;

    /** 所属域 */
    @Excel(name = "所属域")
    private String domain;

    /** 当前值（无数据显示"暂无数据"） */
    @Excel(name = "当前值")
    private String value;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 数据口径（real=实测,estimate=估算） */
    @Excel(name = "数据口径", readConverterExp = "real=实测,estimate=估算")
    private String dataFlag;

    /** 快照基准日 */
    @Excel(name = "快照基准日", width = 20, dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date snapshotDate;

    public String getMetricCode()
    {
        return metricCode;
    }

    public void setMetricCode(String metricCode)
    {
        this.metricCode = metricCode;
    }

    public String getMetricName()
    {
        return metricName;
    }

    public void setMetricName(String metricName)
    {
        this.metricName = metricName;
    }

    public String getDomain()
    {
        return domain;
    }

    public void setDomain(String domain)
    {
        this.domain = domain;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public String getDataFlag()
    {
        return dataFlag;
    }

    public void setDataFlag(String dataFlag)
    {
        this.dataFlag = dataFlag;
    }

    public Date getSnapshotDate()
    {
        return snapshotDate;
    }

    public void setSnapshotDate(Date snapshotDate)
    {
        this.snapshotDate = snapshotDate;
    }
}
