package com.ruoyi.safety.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试题库分类对象 biz_safety_exam_category
 *
 * 支持两级分类，用于题库归类管理。
 *
 * @author ruoyi
 */
public class SafetyExamCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private Long parentId;

    /** 分类名称 */
    private String categoryName;

    private Integer orderNum;
    private Long enterpriseId;
    private String status;
    private String delFlag;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
