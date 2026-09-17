package com.ruoyi.flowable.domain;

import com.ruoyi.common.core.domain.entity.SysDictData;

/**
 * 流程分类 VO flowable_category 字典数据 + 引用统计
 *
 * @author ruoyi
 */
public class FlowCategory extends SysDictData
{
    private static final long serialVersionUID = 1L;

    /** 引用该分类的流程模型数 */
    private Long modelCount;

    /** 引用该分类的已部署流程定义数 */
    private Long definitionCount;

    /** 图标（存于字典 css_class 字段） */
    private String icon;

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Long getModelCount() { return modelCount; }
    public void setModelCount(Long modelCount) { this.modelCount = modelCount; }
    public Long getDefinitionCount() { return definitionCount; }
    public void setDefinitionCount(Long definitionCount) { this.definitionCount = definitionCount; }
}
