package com.ruoyi.food.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品批次号规则对象 food_batch_rule
 *
 * 占位符：{yyyyMMdd} {yyMM} {yyyy} {materialCode} {seq:n}（n为流水位数）
 *
 * @author ruoyi
 */
public class FoodBatchRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 规则名称 */
    private String ruleName;

    /** 适用物料ID（0=默认规则） */
    private Long materialId;

    /** 批次号模板 */
    private String rulePattern;

    /** 当前流水号 */
    private Long currentSeq;

    /** 流水日期（跨日自动重置） */
    private String seqDate;

    /** 状态（0启用 1停用） */
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 物料名称（关联查询，非表字段） */
    private String materialName;

    /** 试算预览结果（非表字段） */
    private String previewCode;

    public Long getRuleId()
    {
        return ruleId;
    }

    public void setRuleId(Long ruleId)
    {
        this.ruleId = ruleId;
    }

    @NotBlank(message = "规则名称不能为空")
    @Size(min = 0, max = 100, message = "规则名称长度不能超过100个字符")
    public String getRuleName()
    {
        return ruleName;
    }

    public void setRuleName(String ruleName)
    {
        this.ruleName = ruleName;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    @NotBlank(message = "批次号模板不能为空")
    @Size(min = 0, max = 100, message = "批次号模板长度不能超过100个字符")
    public String getRulePattern()
    {
        return rulePattern;
    }

    public void setRulePattern(String rulePattern)
    {
        this.rulePattern = rulePattern;
    }

    public Long getCurrentSeq()
    {
        return currentSeq;
    }

    public void setCurrentSeq(Long currentSeq)
    {
        this.currentSeq = currentSeq;
    }

    public String getSeqDate()
    {
        return seqDate;
    }

    public void setSeqDate(String seqDate)
    {
        this.seqDate = seqDate;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getPreviewCode()
    {
        return previewCode;
    }

    public void setPreviewCode(String previewCode)
    {
        this.previewCode = previewCode;
    }
}
