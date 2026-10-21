package com.ruoyi.mk.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 编号规则动态前缀映射 mk_number_rule_prefix
 *
 * @author ruoyi
 */
public class MkNumberRulePrefix extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long prefixId;

    /** 规则ID */
    private Long ruleId;

    /** 字段值（字典值，如 0/1/2/3） */
    private String fieldValue;

    /** 字段标签（字典标签，如 原材料/半成品） */
    private String fieldLabel;

    /** 对应前缀（如 YL/BC/CP） */
    private String prefix;

    /** 是否启用（0=否,1=是） */
    private String enabled;

    /** 当前序列号（按动态前缀重置时使用） */
    private Long currentSeq;

    /** 当前日期串（按动态前缀重置时使用） */
    private String currentDateStr;

    public Long getPrefixId() { return prefixId; }
    public void setPrefixId(Long prefixId) { this.prefixId = prefixId; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getFieldValue() { return fieldValue; }
    public void setFieldValue(String fieldValue) { this.fieldValue = fieldValue; }
    public String getFieldLabel() { return fieldLabel; }
    public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
    public Long getCurrentSeq() { return currentSeq; }
    public void setCurrentSeq(Long currentSeq) { this.currentSeq = currentSeq; }
    public String getCurrentDateStr() { return currentDateStr; }
    public void setCurrentDateStr(String currentDateStr) { this.currentDateStr = currentDateStr; }
}
