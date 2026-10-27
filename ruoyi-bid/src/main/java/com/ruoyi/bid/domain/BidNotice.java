package com.ruoyi.bid.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 招标公告对象 bid_notice
 */
public class BidNotice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 公告ID */
    private Long noticeId;

    /** 项目关联键 */
    @Excel(name = "项目关联键")
    private String projectKey;

    /** 公告标题 */
    @Excel(name = "公告标题")
    private String title;

    /** 信息类型（字典 bid_notice_type） */
    @Excel(name = "信息类型", dictType = "bid_notice_type")
    private String noticeType;

    /** 采集源ID */
    private Long sourceId;

    /** 来源站点名称（冗余） */
    @Excel(name = "来源站点")
    private String sourceName;

    /** 招标人/采购人 */
    @Excel(name = "招标人")
    private String tenderee;

    /** 代理机构 */
    @Excel(name = "代理机构")
    private String agent;

    /** 地区（行政区划码） */
    @Excel(name = "地区")
    private String regionCode;

    /** 行业分类（字典 bid_industry） */
    @Excel(name = "行业分类", dictType = "bid_industry")
    private String industry;

    /** 预算/估算金额（元） */
    @Excel(name = "预算金额")
    private BigDecimal budgetAmount;

    /** 公告发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date deadlineTime;

    /** 开标时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开标时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date openTime;

    /** 联系人 */
    @Excel(name = "联系人")
    private String contactPerson;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String contactPhone;

    /** 原文链接 */
    @Excel(name = "原文链接")
    private String sourceUrl;

    /** 公告正文 */
    private String content;

    /** 招标附件（逗号分隔文件URL） */
    private String attachmentUrl;

    /** 命中关键词（逗号分隔） */
    @Excel(name = "命中关键词")
    private String matchedKeywords;

    /** 是否已AI抽取（0否 1是） */
    private String aiExtracted;

    /** 录入方式（0手动 1采集） */
    @Excel(name = "录入方式", readConverterExp = "0=手动,1=采集")
    private String inputType;

    /** 删除标志 */
    private String delFlag;

    public Long getNoticeId() { return noticeId; }
    public void setNoticeId(Long noticeId) { this.noticeId = noticeId; }
    public String getProjectKey() { return projectKey; }
    public void setProjectKey(String projectKey) { this.projectKey = projectKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getNoticeType() { return noticeType; }
    public void setNoticeType(String noticeType) { this.noticeType = noticeType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getTenderee() { return tenderee; }
    public void setTenderee(String tenderee) { this.tenderee = tenderee; }
    public String getAgent() { return agent; }
    public void setAgent(String agent) { this.agent = agent; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public Date getDeadlineTime() { return deadlineTime; }
    public void setDeadlineTime(Date deadlineTime) { this.deadlineTime = deadlineTime; }
    public Date getOpenTime() { return openTime; }
    public void setOpenTime(Date openTime) { this.openTime = openTime; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    public String getMatchedKeywords() { return matchedKeywords; }
    public void setMatchedKeywords(String matchedKeywords) { this.matchedKeywords = matchedKeywords; }
    public String getAiExtracted() { return aiExtracted; }
    public void setAiExtracted(String aiExtracted) { this.aiExtracted = aiExtracted; }
    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
