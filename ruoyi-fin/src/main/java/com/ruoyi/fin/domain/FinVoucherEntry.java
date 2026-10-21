package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 记账凭证分录 fin_voucher_entry
 *
 * <p>【借贷平衡】由 Service 层强制校验 Σ借 = Σ贷，不平不允许保存。
 * 本类只承载单行分录，不做平衡判断。</p>
 *
 * <p>【红字处理】金额恒为正数（DB 有 CHECK amount > 0），
 * 红字通过 direction 表达，不使用负数。</p>
 *
 * <p>【辅助核算 Q2 决策】四类辅助核算：客户/供应商/部门/项目。
 * 启用哪几类由科目的 aux_types 字段决定，本表提供字段承载。</p>
 *
 * @author ruoyi
 */
public class FinVoucherEntry extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 方向：借 */
    public static final String DIR_DEBIT = "DEBIT";

    /** 方向：贷 */
    public static final String DIR_CREDIT = "CREDIT";

    /** 分录ID */
    private Long entryId;

    /** 凭证ID */
    private Long voucherId;

    /** 预留账套ID */
    private Long companyId;

    /** 行号 */
    private Integer lineNo;

    /** 会计期间（冗余自凭证，加速账簿汇总） */
    private String period;

    /** 归属部门（随凭证继承） */
    private Long deptId;

    /** 会计科目编码 */
    @Excel(name = "科目编码")
    private String accountCode;

    /** 科目名称（冗余） */
    @Excel(name = "科目名称")
    private String accountName;

    /** 方向 DEBIT/CREDIT */
    @Excel(name = "方向", readConverterExp = "DEBIT=借,CREDIT=贷")
    private String direction;

    /** 金额（恒为正） */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 币种（V1 固定 CNY） */
    private String currency;

    /** 汇率（V1 固定 1） */
    private BigDecimal exchangeRate;

    /** 税额 */
    @Excel(name = "税额")
    private BigDecimal taxAmount;

    /** 辅助核算-客户 */
    private Long auxCustomerId;

    /** 辅助核算-供应商 */
    private Long auxSupplierId;

    /** 辅助核算-部门 */
    private Long auxDeptId;

    /** 辅助核算-项目 */
    private Long auxProjectId;

    /** 摘要 */
    @Excel(name = "摘要")
    private String summary;
    /**
     * 来源规则键（运行时字段，不入库）。
     *
     * <p>凭证规则引擎组装配分录时标记「本行来自哪条规则」，
     * 便于借贷不平时定位问题规则。落库前会被清空。</p>
     */
    private String sourceKey;

    public Long getEntryId() { return entryId; }
    public void setEntryId(Long entryId) { this.entryId = entryId; }

    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public Long getAuxCustomerId() { return auxCustomerId; }
    public void setAuxCustomerId(Long auxCustomerId) { this.auxCustomerId = auxCustomerId; }

    public Long getAuxSupplierId() { return auxSupplierId; }
    public void setAuxSupplierId(Long auxSupplierId) { this.auxSupplierId = auxSupplierId; }

    public Long getAuxDeptId() { return auxDeptId; }
    public void setAuxDeptId(Long auxDeptId) { this.auxDeptId = auxDeptId; }

    public Long getAuxProjectId() { return auxProjectId; }
    public void setAuxProjectId(Long auxProjectId) { this.auxProjectId = auxProjectId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    /**
     * 来源规则键（运行时字段，不入库）。
     *
     * <p>凭证规则引擎组装配分录时标记「本行来自哪条规则」，
     * 便于借贷不平时定位问题规则。落库前会被清空。</p>
     */
    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }

    /**
     * 判断本行是否借方。
     *
     * @return true=借方
     */
    public boolean isDebit()
    {
        return DIR_DEBIT.equals(direction);
    }
}