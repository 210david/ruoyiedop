package com.ruoyi.bi.domain;

import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * BI指标字典对象 bi_metric_dict
 *
 * 统一指标层的口径定义中心：同口径指标全系统仅一处计算实现，
 * 口径变更必须通过 bi_metric_dict_log 留痕。
 *
 * @author ruoyi
 */
public class BiMetricDict extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 指标编码（唯一） */
    @Excel(name = "指标编码")
    private String metricCode;

    /** 指标名称 */
    @Excel(name = "指标名称")
    private String metricName;

    /** 所属域（sales/bid/production/quality/purchase/inventory/equipment/safety/hr/synthesis） */
    @Excel(name = "所属域")
    private String domain;

    /** 口径定义（最终解释以本字段为准） */
    @Excel(name = "口径定义")
    private String caliberDesc;

    /** 计算实现引用（如 MkStatsProvider） */
    @Excel(name = "计算实现")
    private String calcRef;

    /** 刷新方式（snapshot快照/realtime实时） */
    @Excel(name = "刷新方式", readConverterExp = "snapshot=快照,realtime=实时")
    private String refreshType;

    /** 统计频率（day日/month月） */
    @Excel(name = "统计频率", readConverterExp = "day=日,month=月")
    private String freq;

    /** 红线阈值（P1预警启用） */
    @Excel(name = "红线阈值")
    private Double thresholdRed;

    /** 黄线阈值（P1预警启用） */
    @Excel(name = "黄线阈值")
    private Double thresholdYellow;

    /** 阈值方向（lte低于预警/gte高于预警） */
    @Excel(name = "阈值方向", readConverterExp = "lt=低于预警,gt=超限预警")
    private String thresholdDir;

    /** 指标责任人 */
    @Excel(name = "责任人")
    private String owner;

    /** 下钻跳转URL（F4：指标卡点击后跳转的模块列表页，支持占位符 {value}={当前值}，空=跳转本域主题看板） */
    private String drillUrl;

    /** 预警通知角色（F7：逗号分隔的 sys_role.role_key，空=默认推 admin） */
    private String notifyRoles;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

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

    public String getCaliberDesc()
    {
        return caliberDesc;
    }

    public void setCaliberDesc(String caliberDesc)
    {
        this.caliberDesc = caliberDesc;
    }

    public String getCalcRef()
    {
        return calcRef;
    }

    public void setCalcRef(String calcRef)
    {
        this.calcRef = calcRef;
    }

    public String getRefreshType()
    {
        return refreshType;
    }

    public void setRefreshType(String refreshType)
    {
        this.refreshType = refreshType;
    }

    public String getFreq()
    {
        return freq;
    }

    public void setFreq(String freq)
    {
        this.freq = freq;
    }

    public Double getThresholdRed()
    {
        return thresholdRed;
    }

    public void setThresholdRed(Double thresholdRed)
    {
        this.thresholdRed = thresholdRed;
    }

    public Double getThresholdYellow()
    {
        return thresholdYellow;
    }

    public void setThresholdYellow(Double thresholdYellow)
    {
        this.thresholdYellow = thresholdYellow;
    }

    public String getThresholdDir()
    {
        return thresholdDir;
    }

    public void setThresholdDir(String thresholdDir)
    {
        this.thresholdDir = thresholdDir;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public String getDrillUrl()
    {
        return drillUrl;
    }

    public void setDrillUrl(String drillUrl)
    {
        this.drillUrl = drillUrl;
    }

    public String getNotifyRoles()
    {
        return notifyRoles;
    }

    public void setNotifyRoles(String notifyRoles)
    {
        this.notifyRoles = notifyRoles;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public Date getCreateTime()
    {
        return super.getCreateTime();
    }
}
