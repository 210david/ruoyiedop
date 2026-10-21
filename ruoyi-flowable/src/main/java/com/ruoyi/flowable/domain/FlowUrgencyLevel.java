package com.ruoyi.flowable.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 紧急程度配置对象
 *
 * 驱动系统内置字段 urgency 的可选档位（参考泛微 E9 workflow_customlevel）。
 * 由本表替代原先 FlowSystemFields 中的硬编码档位，支持自定义名称与排序。
 *
 * @author ruoyi
 */
public class FlowUrgencyLevel implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long levelId;

    /** 档位值（存业务表 urgency 列） */
    private String levelValue;

    /** 系统默认名（普通/紧急/特急） */
    private String defaultName;

    /** 用户自定义名（为空则用 defaultName） */
    private String customName;

    /** 排序（越小越靠前） */
    private Integer showOrder;

    /** 是否封存：0否 1是（封存后新发起不可选） */
    private String isSealed;

    /** 是否触发提醒分级：0否 1是 */
    private String isRemind;

    /** 状态：0正常 1停用 */
    private String status;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    private Date updateTime;

    /** 展示名（customName 优先，为空回退 defaultName） */
    public String getDisplayName()
    {
        return (customName != null && !customName.isEmpty()) ? customName : defaultName;
    }

    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    public String getLevelValue() { return levelValue; }
    public void setLevelValue(String levelValue) { this.levelValue = levelValue; }
    public String getDefaultName() { return defaultName; }
    public void setDefaultName(String defaultName) { this.defaultName = defaultName; }
    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName; }
    public Integer getShowOrder() { return showOrder; }
    public void setShowOrder(Integer showOrder) { this.showOrder = showOrder; }
    public String getIsSealed() { return isSealed; }
    public void setIsSealed(String isSealed) { this.isSealed = isSealed; }
    public String getIsRemind() { return isRemind; }
    public void setIsRemind(String isRemind) { this.isRemind = isRemind; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
