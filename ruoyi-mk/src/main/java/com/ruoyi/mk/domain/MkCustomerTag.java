package com.ruoyi.mk.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 客户标签 mk_customer_tag
 *
 * @author ruoyi
 */
public class MkCustomerTag extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 标签ID */
    private Long tagId;

    /** 标签名称 */
    @Excel(name = "标签名称")
    private String tagName;

    /** 标签类型 */
    @Excel(name = "标签类型", dictType = "marketing_tag_type")
    private String tagType;

    /** 标签颜色 */
    private String tagColor;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sort;

    /** 状态 */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    public Long getTagId() { return tagId; }
    public void setTagId(Long tagId) { this.tagId = tagId; }
    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }
    public String getTagType() { return tagType; }
    public void setTagType(String tagType) { this.tagType = tagType; }
    public String getTagColor() { return tagColor; }
    public void setTagColor(String tagColor) { this.tagColor = tagColor; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
