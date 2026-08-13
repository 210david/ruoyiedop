package com.ruoyi.qms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 检验标准库表 qms_inspect_std
 *
 * @author ruoyi
 */
public class QmsInspectStd extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 标准ID */
    private Long stdId;

    /** 标准编码 */
    @Excel(name = "标准编码")
    private String stdCode;

    /** 检验项目名称 */
    @Excel(name = "检验项目")
    private String stdName;

    /** 检验方法 */
    @Excel(name = "检验方法")
    private String inspectMethod;

    /** 计量单位 */
    @Excel(name = "单位")
    private String unit;

    /** 标准下限 */
    @Excel(name = "标准下限")
    private BigDecimal valMin;

    /** 标准上限 */
    @Excel(name = "标准上限")
    private BigDecimal valMax;

    /** 目标值 */
    @Excel(name = "目标值")
    private BigDecimal targetVal;

    /** 判定规则（1区间 2单值 3定性） */
    @Excel(name = "判定规则", readConverterExp = "1=区间判定,2=单值判定,3=定性判定")
    private String judgeRule;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getStdId() { return stdId; }
    public void setStdId(Long stdId) { this.stdId = stdId; }
    public String getStdCode() { return stdCode; }
    public void setStdCode(String stdCode) { this.stdCode = stdCode; }
    public String getStdName() { return stdName; }
    public void setStdName(String stdName) { this.stdName = stdName; }
    public String getInspectMethod() { return inspectMethod; }
    public void setInspectMethod(String inspectMethod) { this.inspectMethod = inspectMethod; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getValMin() { return valMin; }
    public void setValMin(BigDecimal valMin) { this.valMin = valMin; }
    public BigDecimal getValMax() { return valMax; }
    public void setValMax(BigDecimal valMax) { this.valMax = valMax; }
    public BigDecimal getTargetVal() { return targetVal; }
    public void setTargetVal(BigDecimal targetVal) { this.targetVal = targetVal; }
    public String getJudgeRule() { return judgeRule; }
    public void setJudgeRule(String judgeRule) { this.judgeRule = judgeRule; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
