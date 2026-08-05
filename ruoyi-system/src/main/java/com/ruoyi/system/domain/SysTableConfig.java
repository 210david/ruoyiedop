package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 表格列配置表 sys_table_config（列宽 + 列显隐）
 *
 * 支持两种配置类型：
 * - G（全局）：管理员设置后保存，对所有用户生效
 * - U（个人）：普通用户设置后保存，仅对当前用户生效
 *
 * 优先级：个人配置 > 全局配置 > 默认值
 *
 * @author ruoyi
 */
public class SysTableConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long configId;

    /** 页面标识（如 pms_eval_index） */
    private String tableKey;

    /** 列属性名（如 supplierName） */
    private String colProp;

    /** 列宽（像素），NULL表示未设置 */
    private Integer colWidth;

    /** 列是否可见：true=可见，false=隐藏，null=未设置 */
    private Boolean colVisible;

    /** 配置类型：G=全局（管理员），U=个人（用户） */
    private String configType;

    /** 用户ID（config_type=U 时有效，G 时为NULL） */
    private Long userId;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }

    public String getTableKey()
    {
        return tableKey;
    }

    public void setTableKey(String tableKey)
    {
        this.tableKey = tableKey;
    }

    public String getColProp()
    {
        return colProp;
    }

    public void setColProp(String colProp)
    {
        this.colProp = colProp;
    }

    public Integer getColWidth()
    {
        return colWidth;
    }

    public void setColWidth(Integer colWidth)
    {
        this.colWidth = colWidth;
    }

    public Boolean getColVisible()
    {
        return colVisible;
    }

    public void setColVisible(Boolean colVisible)
    {
        this.colVisible = colVisible;
    }

    public String getConfigType()
    {
        return configType;
    }

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("tableKey", getTableKey())
            .append("colProp", getColProp())
            .append("colWidth", getColWidth())
            .append("colVisible", getColVisible())
            .append("configType", getConfigType())
            .append("userId", getUserId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
