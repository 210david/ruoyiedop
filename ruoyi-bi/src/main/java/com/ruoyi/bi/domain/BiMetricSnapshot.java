package com.ruoyi.bi.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * BI指标日快照对象 bi_metric_snapshot
 *
 * 数据权限说明（规范 3.5.1）：统计/追溯类，豁免归属字段自动填充；
 * dept_id 为统计维度列（非归属），驾驶舱查询按用户数据权限过滤该维度。
 *
 * @author ruoyi
 */
public class BiMetricSnapshot implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 指标编码 */
    private String metricCode;

    /** 指标日期 */
    private Date metricDate;

    /** 部门维度（sys_dept.dept_id；null=全部门汇总） */
    private Long deptId;

    /** 指标值（无数据为 null，前端展示 —） */
    private BigDecimal metricValue;

    /** 单位 */
    private String unit;

    /** 数据标记（real实测/estimate估算） */
    private String dataFlag;

    /** 快照时间 */
    private Date snapshotTime;

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

    public Date getMetricDate()
    {
        return metricDate;
    }

    public void setMetricDate(Date metricDate)
    {
        this.metricDate = metricDate;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public BigDecimal getMetricValue()
    {
        return metricValue;
    }

    public void setMetricValue(BigDecimal metricValue)
    {
        this.metricValue = metricValue;
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

    public Date getSnapshotTime()
    {
        return snapshotTime;
    }

    public void setSnapshotTime(Date snapshotTime)
    {
        this.snapshotTime = snapshotTime;
    }
}
