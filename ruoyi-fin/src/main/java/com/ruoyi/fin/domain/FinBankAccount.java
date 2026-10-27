package com.ruoyi.fin.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 资金账户 fin_bank_account
 *
 * <p>【P0-5 加密】银行账号存密文 {@code account_number_encrypt}（AES-128-GCM），
 * 另存明文尾号 {@code account_tail} 用于展示与检索——
 * 密文无法 LIKE 查询，故尾号是必要冗余。</p>
 *
 * <p>【现金账户无账号】account_type='CASH' 时账号可为空，
 * 第三方支付同理（账号字段无意义）。</p>
 *
 * @author ruoyi
 */
public class FinBankAccount extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 类型：银行账户 */
    public static final String TYPE_BANK = "BANK";

    /** 类型：现金 */
    public static final String TYPE_CASH = "CASH";

    /** 类型：第三方支付 */
    public static final String TYPE_THIRD_PARTY = "THIRD_PARTY";

    /** 类型：票据保证金 */
    public static final String TYPE_NOTE = "NOTE";

    /** 账户ID */
    private Long accountId;

    /** 预留账套ID */
    private Long companyId;

    /** 账户编码 */
    @Excel(name = "账户编码")
    private String accountCode;

    /** 账户名称 */
    @Excel(name = "账户名称")
    private String accountName;

    /** 账户类型 */
    @Excel(name = "账户类型", dictType = "fin_account_type")
    private String accountType;

    /** 归属部门ID */
    private Long deptId;

    /** 开户行 */
    @Excel(name = "开户行")
    private String bankName;

    /** 开户网点 */
    private String bankBranch;

    /** 银行账号密文（不展示、不导出） */
    private String accountNumberEncrypt;

    /** 银行账号尾号（明文，用于展示与检索） */
    @Excel(name = "账号尾号")
    private String accountTail;

    /** 币种 */
    private String currency;

    /** 期初余额 */
    @Excel(name = "期初余额")
    private BigDecimal openingBalance;

    /** 当前余额 */
    @Excel(name = "当前余额")
    private BigDecimal currentBalance;

    /** 状态 0启用 1停用 */
    @Excel(name = "状态", dictType = "fin_account_status")
    private String status;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankBranch() { return bankBranch; }
    public void setBankBranch(String bankBranch) { this.bankBranch = bankBranch; }

    public String getAccountNumberEncrypt() { return accountNumberEncrypt; }
    public void setAccountNumberEncrypt(String accountNumberEncrypt) { this.accountNumberEncrypt = accountNumberEncrypt; }

    public String getAccountTail() { return accountTail; }
    public void setAccountTail(String accountTail) { this.accountTail = accountTail; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(BigDecimal openingBalance) { this.openingBalance = openingBalance; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * 该账户类型是否需要银行账号。
     *
     * @param type 账户类型
     * @return true=需要账号
     */
    public static boolean needAccountNo(String type)
    {
        return TYPE_BANK.equals(type) || TYPE_NOTE.equals(type);
    }
}