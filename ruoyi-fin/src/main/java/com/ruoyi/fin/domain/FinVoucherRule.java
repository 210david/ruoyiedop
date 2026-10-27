package com.ruoyi.fin.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 凭证规则 fin_voucher_rule（只读视图对象）
 *
 * <p>一条规则 = 一行分录。多条规则按 {@code priority} 排序组成一张凭证的各行。</p>
 *
 * <p><b>金额表达式</b>（amount_expr）取值：
 * <ul>
 *   <li>{@code total_amount} —— 来源单据价税合计</li>
 *   <li>{@code amount_excl_tax} —— 来源单据不含税金额</li>
 *   <li>{@code tax_amount} —— 来源单据税额</li>
 * </ul>
 * 引擎按此表达式从来源单据取数后组成分录。</p>
 *
 * @author ruoyi
 */
public class FinVoucherRule
{
    /** 规则ID */
    private Long ruleId;

    /** 规则键（幂等键，五源追溯用） */
    private String ruleKey;

    /** 规则名称 */
    private String ruleName;

    /** 来源模块 pms/mk/hr */
    private String sourceModule;

    /** 来源类型 invoice/shipment/payroll */
    private String sourceType;

    /** 触发状态：单据达到此状态时触发（对应各模块的 status） */
    private String triggerStatus;

    /** 科目编码 */
    private String accountCode;

    /** 借贷方向 DEBIT/CREDIT */
    private String direction;

    /** 金额表达式 */
    private String amountExpr;

    /** 辅助核算类型 CUSTOMER/SUPPLIER/DEPT */
    private String auxType;

    /** 摘要模板 */
    private String summaryTpl;

    /** 优先级（小者在前） */
    private Integer priority;

    /** 状态 0启用 1停用 */
    private String status;

    /** 来源单据ID（运行时填充，非表字段） */
    private Long sourceId;

    /** 金额（运行时按表达式计算，非表字段） */
    private BigDecimal amount;

    /** 来源单据号（运行时填充） */
    private String sourceKey;

    /** 来源日期（运行时填充） */
    private Date sourceDate;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public String getRuleKey() { return ruleKey; }
    public void setRuleKey(String ruleKey) { this.ruleKey = ruleKey; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getTriggerStatus() { return triggerStatus; }
    public void setTriggerStatus(String triggerStatus) { this.triggerStatus = triggerStatus; }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getAmountExpr() { return amountExpr; }
    public void setAmountExpr(String amountExpr) { this.amountExpr = amountExpr; }

    public String getAuxType() { return auxType; }
    public void setAuxType(String auxType) { this.auxType = auxType; }

    public String getSummaryTpl() { return summaryTpl; }
    public void setSummaryTpl(String summaryTpl) { this.summaryTpl = summaryTpl; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }

    public Date getSourceDate() { return sourceDate; }
    public void setSourceDate(Date sourceDate) { this.sourceDate = sourceDate; }

    /** 更新人（运行时填充） */
    private String updateBy;

    /** 更新时间（运行时填充） */
    private Date updateTime;

    /** 备注 */
    private String remark;

    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}