package com.ruoyi.common.stats;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 统一指标值传输对象（决策支持模块）
 *
 * 各模块 IModuleStatsProvider 实现的统一返回类型；
 * 注意：这是跨模块传输对象，放在 common 而非各模块 domain/vo
 * （各模块对外的 Dashboard/Statistics 页面 VO 仍遵循统一开发规范 2.8，放本模块 domain/vo）。
 *
 * @author ruoyi
 */
public class MetricValue implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 指标编码 */
    private String metricCode;

    /** 指标日期 */
    private Date metricDate;

    /** 部门维度（sys_dept.dept_id；null=全部门汇总） */
    private Long deptId;

    /** 指标值（无数据为 null，前端展示「—」，不得用 0 充当无数据） */
    private BigDecimal value;

    /** 单位（元 / 个 / % / 小时） */
    private String unit;

    /** 数据标记（real 实测 / estimate 估算，估算类指标如 DMS_OEE 前端须显示角标） */
    private String dataFlag;

    public MetricValue()
    {
    }

    public MetricValue(String metricCode, Date metricDate, Long deptId, BigDecimal value, String unit, String dataFlag)
    {
        this.metricCode = metricCode;
        this.metricDate = metricDate;
        this.deptId = deptId;
        this.value = value;
        this.unit = unit;
        this.dataFlag = dataFlag;
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

    public BigDecimal getValue()
    {
        return value;
    }

    public void setValue(BigDecimal value)
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
}
