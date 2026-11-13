package com.ruoyi.bi.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * BI指标预警记录对象 bi_metric_alert
 *
 * F7 阈值预警的留痕中心：每日快照后按字典阈值匹配红/黄线，
 * 按 PRD v1.1 抑制规则推送站内消息（同级别 24h 不重复、升级立即推、
 * 未恢复每 3 天汇总重推），恢复后记录 recover_time。
 *
 * @author ruoyi
 */
public class BiMetricAlert extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 指标编码 */
    private String metricCode;

    /** 预警数据日期（快照日期） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date metricDate;

    /** 部门ID（0=全部门汇总） */
    private Long deptId;

    /** 预警级别（1黄线 2红线） */
    private String alertLevel;

    /** 触发时指标值 */
    private BigDecimal metricValue;

    /** 触发时阈值 */
    private BigDecimal thresholdValue;

    /** 预警内容 */
    private String alertMessage;

    /** 推送状态（0未推送 1已推送 2抑制未推） */
    private String pushStatus;

    /** 最近推送时间 */
    private Date pushTime;

    /** 累计推送次数 */
    private Integer pushCount;

    /** 最近一次未推送原因（抑制说明） */
    private String pushRemark;

    /** 恢复时间（值回到阈值内） */
    private Date recoverTime;

    /** 预警状态（0未恢复 1已恢复） */
    private String status;

    /** 查询辅助：指标名称（联字典展示用） */
    private String metricName;

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

    public String getAlertLevel()
    {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel)
    {
        this.alertLevel = alertLevel;
    }

    public BigDecimal getMetricValue()
    {
        return metricValue;
    }

    public void setMetricValue(BigDecimal metricValue)
    {
        this.metricValue = metricValue;
    }

    public BigDecimal getThresholdValue()
    {
        return thresholdValue;
    }

    public void setThresholdValue(BigDecimal thresholdValue)
    {
        this.thresholdValue = thresholdValue;
    }

    public String getAlertMessage()
    {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage)
    {
        this.alertMessage = alertMessage;
    }

    public String getPushStatus()
    {
        return pushStatus;
    }

    public void setPushStatus(String pushStatus)
    {
        this.pushStatus = pushStatus;
    }

    public Date getPushTime()
    {
        return pushTime;
    }

    public void setPushTime(Date pushTime)
    {
        this.pushTime = pushTime;
    }

    public Integer getPushCount()
    {
        return pushCount;
    }

    public void setPushCount(Integer pushCount)
    {
        this.pushCount = pushCount;
    }

    public String getPushRemark()
    {
        return pushRemark;
    }

    public void setPushRemark(String pushRemark)
    {
        this.pushRemark = pushRemark;
    }

    public Date getRecoverTime()
    {
        return recoverTime;
    }

    public void setRecoverTime(Date recoverTime)
    {
        this.recoverTime = recoverTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getMetricName()
    {
        return metricName;
    }

    public void setMetricName(String metricName)
    {
        this.metricName = metricName;
    }
}
