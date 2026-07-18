package com.ruoyi.dms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/** 维修知识FAQ dms_repair_faq */
public class DmsRepairFaq extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long faqId;
    @Excel(name = "问题") private String question;
    private String answer;
    private Long categoryId;
    @Excel(name = "分类") private String categoryName;
    @Excel(name = "关键词") private String keywords;
    private Integer viewCount;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用") private String status;
    private String delFlag;

    public Long getFaqId() { return faqId; }
    public void setFaqId(Long faqId) { this.faqId = faqId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
