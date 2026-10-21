package com.ruoyi.bid.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 关键词订阅配置对象 bid_crawl_keyword
 */
public class BidCrawlKeyword extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long keywordId;
    @Excel(name = "关键词")
    private String keyword;
    private String synonymWords;
    private String excludeWords;
    /** 匹配方式（字典 bid_match_type） */
    @Excel(name = "匹配方式", dictType = "bid_match_type")
    private String matchType;
    private String regionCode;
    @Excel(name = "行业分类", dictType = "bid_industry")
    private String industry;
    private BigDecimal minAmount;
    private String noticeTypes;
    private String notifyEnabled;
    /** 启用状态（字典 sys_normal_disable） */
    @Excel(name = "启用状态", dictType = "sys_normal_disable")
    private String enabled;
    private String delFlag;

    /** 订阅范围列表（子表） */
    private List<BidKeywordScope> scopeList = new ArrayList<>();

    public Long getKeywordId() { return keywordId; }
    public void setKeywordId(Long keywordId) { this.keywordId = keywordId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getSynonymWords() { return synonymWords; }
    public void setSynonymWords(String synonymWords) { this.synonymWords = synonymWords; }
    public String getExcludeWords() { return excludeWords; }
    public void setExcludeWords(String excludeWords) { this.excludeWords = excludeWords; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public String getNoticeTypes() { return noticeTypes; }
    public void setNoticeTypes(String noticeTypes) { this.noticeTypes = noticeTypes; }
    public String getNotifyEnabled() { return notifyEnabled; }
    public void setNotifyEnabled(String notifyEnabled) { this.notifyEnabled = notifyEnabled; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<BidKeywordScope> getScopeList() { return scopeList; }
    public void setScopeList(List<BidKeywordScope> scopeList) { this.scopeList = scopeList; }
}
