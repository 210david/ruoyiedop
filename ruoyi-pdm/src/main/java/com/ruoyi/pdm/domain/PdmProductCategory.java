package com.ruoyi.pdm.domain;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产品分类对象 pdm_product_category
 *
 * @author ruoyi
 */
public class PdmProductCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long categoryId;

    /** 父分类ID（0=顶级） */
    private Long parentId;

    /** 祖级路径（逗号分隔） */
    private String ancestors;

    /** 分类编码（自动生成） */
    private String categoryCode;

    /** 分类名称 */
    private String categoryName;

    /** 显示顺序 */
    private Integer orderNum;

    /** 状态（0=正常,1=停用） */
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 子分类（树形构建用） */
    private List<PdmProductCategory> children = new ArrayList<>();

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getAncestors() { return ancestors; }
    public void setAncestors(String ancestors) { this.ancestors = ancestors; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PdmProductCategory> getChildren() { return children; }
    public void setChildren(List<PdmProductCategory> children) { this.children = children; }
}
