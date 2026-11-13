package com.ruoyi.bi.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * BI指标对账日志对象 bi_metric_check_log
 *
 * 每日快照后由对账任务写入：实时值 vs 快照值比对结果，
 * 仅管理员可见（bi:metric:check 权限）。
 *
 * @author ruoyi
 */
public class BiMetricCheckLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 指标编码 */
    private String metricCode;

    /** 对账日期 */
    private Date checkDate;

    /** 部门维度 */
    private Long deptId;

    /** 快照值 */
    private BigDecimal snapshotValue;

    /** 实时值 */
    private BigDecimal realtimeValue;

    /** 差异值 */
    private BigDecimal diffValue;

    /** 对账结果（0一致 1不一致） */
    private String checkStatus;

    /** 记录时间 */
    private Date createTime;

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

    public Date getCheckDate()
    {
        return checkDate;
    }

    public void setCheckDate(Date checkDate)
    {
        this.checkDate = checkDate;
    }

    public Long getDeptId()
    {
        return deptId;
    }

    public void setDeptId(Long deptId)
    {
        this.deptId = deptId;
    }

    public BigDecimal getSnapshotValue()
    {
        return snapshotValue;
    }

    public void setSnapshotValue(BigDecimal snapshotValue)
    {
        this.snapshotValue = snapshotValue;
    }

    public BigDecimal getRealtimeValue()
    {
        return realtimeValue;
    }

    public void setRealtimeValue(BigDecimal realtimeValue)
    {
        this.realtimeValue = realtimeValue;
    }

    public BigDecimal getDiffValue()
    {
        return diffValue;
    }

    public void setDiffValue(BigDecimal diffValue)
    {
        this.diffValue = diffValue;
    }

    public String getCheckStatus()
    {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus)
    {
        this.checkStatus = checkStatus;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
