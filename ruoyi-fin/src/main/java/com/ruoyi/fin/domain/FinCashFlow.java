package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 资金流水 fin_cash_flow
 *
 * <p>每笔流水记 {@code balance_after} 交易后余额，便于与银行对账单逐笔勾稽。
 * IN 增加余额、OUT 减少余额，转账拆成两条（转出 OUT + 转入 IN）。</p>
 *
 * @author ruoyi
 */
public class FinCashFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 方向：收 */
    public static final String DIR_IN = "IN";

    /** 方向：付 */
    public static final String DIR_OUT = "OUT";

    /** 流水ID */
    private Long flowId;

    /** 流水号 */
    @Excel(name = "流水号")
    private String flowNo;

    /** 预留账套ID */
    private Long companyId;

    /** 资金账户ID */
    private Long accountId;

    /** 账户名称（冗余） */
    @Excel(name = "账户")
    private String accountName;

    /** 流水日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "流水日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date flowDate;

    /** 会计期间 */
    @Excel(name = "会计期间")
    private String period;

    /** 归属部门ID */
    private Long deptId;

    /** 方向 IN收/OUT付 */
    @Excel(name = "方向", readConverterExp = "IN=收,OUT=付")
    private String direction;

    /** 业务类型（字典 fin_flow_biz_type） */
    @Excel(name = "业务类型", dictType = "fin_flow_biz_type")
    private String bizType;

    /** 金额（恒为正，方向由 direction 表达） */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 交易后余额 */
    @Excel(name = "交易后余额")
    private BigDecimal balanceAfter;

    /** 来源模块 pms/mk/manual */
    private String sourceModule;

    /** 来源类型 */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单号 */
    @Excel(name = "来源单号")
    private String sourceKey;

    /** 关联凭证ID */
    private Long voucherId;

    /** 对账状态 0未对账 1已对账 */
    @Excel(name = "对账状态", readConverterExp = "0=未对账,1=已对账")
    private String reconcileStatus;

    public Long getFlowId() { return flowId; }
    public void setFlowId(Long flowId) { this.flowId = flowId; }

    public String getFlowNo() { return flowNo; }
    public void setFlowNo(String flowNo) { this.flowNo = flowNo; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public Date getFlowDate() { return flowDate; }
    public void setFlowDate(Date flowDate) { this.flowDate = flowDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }

    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }

    public String getReconcileStatus() { return reconcileStatus; }
    public void setReconcileStatus(String reconcileStatus) { this.reconcileStatus = reconcileStatus; }
}