package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 银行对账单明细 fin_bank_statement_item
 *
 * <p>银行流水的一行。{@code match_status} 记录与账面资金流水的匹配情况：
 * 0未匹配（未达账项）、1已匹配、2人工确认强制匹配。</p>
 *
 * @author ruoyi
 */
public class FinBankStatementItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 匹配状态：未匹配 */
    public static final String MATCH_NONE = "0";

    /** 匹配状态：已匹配 */
    public static final String MATCH_OK = "1";

    /** 匹配状态：人工确认 */
    public static final String MATCH_MANUAL = "2";

    /** 明细ID */
    private Long itemId;

    /** 对账单ID */
    private Long statementId;

    /** 交易日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "交易日期", dateFormat = "yyyy-MM-dd")
    private Date transDate;

    /** 交易类型 收入/支出/转账/手续费/利息 */
    @Excel(name = "交易类型")
    private String transType;

    /**
     * 交易金额（<b>收入为正、支出为负</b>）。
     *
     * <p>与账面流水方向相反存储，便于直接求和得到期末余额。</p>
     */
    @Excel(name = "交易金额")
    private BigDecimal transAmount;

    /** 交易后余额 */
    private BigDecimal balanceAfter;

    /** 对方单位 */
    @Excel(name = "对方单位")
    private String counterparty;

    /** 对方账号尾号 */
    private String counterpartyAccount;

    /** 摘要 */
    @Excel(name = "摘要")
    private String summary;

    /** 银行流水号 */
    private String voucherNo;

    /** 匹配的账面资金流水ID */
    private Long flowId;

    /** 匹配状态 0/1/2 */
    @Excel(name = "匹配状态", readConverterExp = "0=未匹配,1=已匹配,2=人工确认")
    private String matchStatus;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getStatementId() { return statementId; }
    public void setStatementId(Long statementId) { this.statementId = statementId; }

    public Date getTransDate() { return transDate; }
    public void setTransDate(Date transDate) { this.transDate = transDate; }

    public String getTransType() { return transType; }
    public void setTransType(String transType) { this.transType = transType; }

    public BigDecimal getTransAmount() { return transAmount; }
    public void setTransAmount(BigDecimal transAmount) { this.transAmount = transAmount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getCounterparty() { return counterparty; }
    public void setCounterparty(String counterparty) { this.counterparty = counterparty; }

    public String getCounterpartyAccount() { return counterpartyAccount; }
    public void setCounterpartyAccount(String counterpartyAccount) { this.counterpartyAccount = counterpartyAccount; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getVoucherNo() { return voucherNo; }
    public void setVoucherNo(String voucherNo) { this.voucherNo = voucherNo; }

    public Long getFlowId() { return flowId; }
    public void setFlowId(Long flowId) { this.flowId = flowId; }

    public String getMatchStatus() { return matchStatus; }
    public void setMatchStatus(String matchStatus) { this.matchStatus = matchStatus; }
}