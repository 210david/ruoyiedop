package com.ruoyi.bid.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 关键词订阅范围对象 bid_keyword_scope
 */
public class BidKeywordScope extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long scopeId;
    private Long keywordId;
    /** 范围类型（1按部门 2按用户） */
    private String scopeType;
    private Long deptId;
    private Long userId;

    public Long getScopeId() { return scopeId; }
    public void setScopeId(Long scopeId) { this.scopeId = scopeId; }
    public Long getKeywordId() { return keywordId; }
    public void setKeywordId(Long keywordId) { this.keywordId = keywordId; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
