package com.ruoyi.qms.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.annotation.Excel;

/** 内审检查表模板 */
public class QmsAuditCheckTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long templateId;
    @Excel(name = "模板名称") private String templateName;
    @Excel(name = "适用标准") private String standard;
    private String delFlag;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
