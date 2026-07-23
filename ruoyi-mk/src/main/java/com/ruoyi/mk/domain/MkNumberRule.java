package com.ruoyi.mk.domain;

import java.util.List;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 编号规则 mk_number_rule
 *
 * @author ruoyi
 */
public class MkNumberRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 规则编码 */
    @Excel(name = "规则编码")
    private String ruleCode;

    /** 所属模块(mk=营销,dms=设备,wms=仓储) */
    @Excel(name = "所属模块", readConverterExp = "mk=营销管理,dms=设备管理,wms=仓储管理")
    private String module;

    /** 规则名称 */
    @Excel(name = "规则名称")
    private String ruleName;

    /** 编号前缀 */
    @Excel(name = "前缀")
    private String prefix;

    /** 日期格式 */
    @Excel(name = "日期格式")
    private String dateFormat;

    /** 序号重置类型（0=不重置,1=按天,2=按月,3=按年） */
    @Excel(name = "重置类型", dictType = "mk_number_reset_type")
    private String resetType;

    /** 序列号长度 */
    @Excel(name = "序列号长度")
    private Integer seqLength;

    /** 序列号起始值 */
    private Long seqStart;

    /** 每次增长步长 */
    private Integer step;

    /** 当前序列号 */
    @Excel(name = "当前序列号")
    private Long currentSeq;

    /** 当前日期串 */
    private String currentDateStr;

    /** 连接符 */
    private String connector;

    /** 状态（0=正常,1=停用） */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    /** 动态前缀关联字段名（如 materialType） */
    private String prefixField;

    /** 动态前缀关联字典类型（如 wms_material_type） */
    private String prefixFieldDictType;

    /** 是否启用动态前缀（0=否,1=是） */
    @Excel(name = "动态前缀", readConverterExp = "0=关闭,1=开启")
    private String prefixFieldEnabled;

    /** 预览编号（非数据库字段） */
    @Excel(name = "预览")
    private String preview;

    /** 动态前缀映射列表（非数据库字段，子表数据） */
    private List<MkNumberRulePrefix> prefixList;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    public String getResetType() { return resetType; }
    public void setResetType(String resetType) { this.resetType = resetType; }
    public Integer getSeqLength() { return seqLength; }
    public void setSeqLength(Integer seqLength) { this.seqLength = seqLength; }
    public Long getSeqStart() { return seqStart; }
    public void setSeqStart(Long seqStart) { this.seqStart = seqStart; }
    public Integer getStep() { return step; }
    public void setStep(Integer step) { this.step = step; }
    public Long getCurrentSeq() { return currentSeq; }
    public void setCurrentSeq(Long currentSeq) { this.currentSeq = currentSeq; }
    public String getCurrentDateStr() { return currentDateStr; }
    public void setCurrentDateStr(String currentDateStr) { this.currentDateStr = currentDateStr; }
    public String getConnector() { return connector; }
    public void setConnector(String connector) { this.connector = connector; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPreview() { return preview; }
    public void setPreview(String preview) { this.preview = preview; }
    public String getPrefixField() { return prefixField; }
    public void setPrefixField(String prefixField) { this.prefixField = prefixField; }
    public String getPrefixFieldDictType() { return prefixFieldDictType; }
    public void setPrefixFieldDictType(String prefixFieldDictType) { this.prefixFieldDictType = prefixFieldDictType; }
    public String getPrefixFieldEnabled() { return prefixFieldEnabled; }
    public void setPrefixFieldEnabled(String prefixFieldEnabled) { this.prefixFieldEnabled = prefixFieldEnabled; }
    public List<MkNumberRulePrefix> getPrefixList() { return prefixList; }
    public void setPrefixList(List<MkNumberRulePrefix> prefixList) { this.prefixList = prefixList; }
}
