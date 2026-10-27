package com.ruoyi.qms.domain;

import com.ruoyi.common.core.domain.BaseEntity;
/** 内审检查表模板检查项 */
public class QmsAuditCheckTemplateItem extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long tmplItemId;
    private Long templateId;
    private String clauseRef;
    private String checkContent;
    private Integer sortNo;
    private String delFlag;

    public Long getTmplItemId() { return tmplItemId; }
    public void setTmplItemId(Long tmplItemId) { this.tmplItemId = tmplItemId; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getClauseRef() { return clauseRef; }
    public void setClauseRef(String clauseRef) { this.clauseRef = clauseRef; }
    public String getCheckContent() { return checkContent; }
    public void setCheckContent(String checkContent) { this.checkContent = checkContent; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
