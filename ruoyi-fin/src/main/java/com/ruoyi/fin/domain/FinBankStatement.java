package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 银行对账单 fin_bank_statement
 *
 * <p>账实核对的载体：导入银行流水后，与账面资金流水逐笔勾稽，
 * 差异即为未达账项（银行已记账面未记 / 账面已记银行未记）。</p>
 *
 * @author ruoyi
 */
public class FinBankStatement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 状态：已导入 */
    public static final String STATUS_IMPORTED = "IMPORTED";

    /** 状态：已对平 */
    public static final String STATUS_RECONCILED = "RECONCILED";

    /** 状态：有差异 */
    public static final String STATUS_DIFF = "DIFF";

    /** 对账单ID */
    private Long statementId;

    /** 预留账套ID */
    private Long companyId;

    /** 资金账户ID */
    private Long accountId;

    /** 账户名称（冗余） */
    @Excel(name = "账户名称")
    private String accountName;

    /** 对账日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "对账日期", dateFormat = "yyyy-MM-dd")
    private Date statementDate;

    /** 会计期间 */
    private String period;

    /** 账面期初余额 */
    private BigDecimal beginBalance;

    /** 银行期初余额 */
    private BigDecimal bankBeginBalance;

    /** 银行期末余额 */
    private BigDecimal bankEndBalance;

    /** 账面期末余额 */
    private BigDecimal bookEndBalance;

    /** 差异金额（银行 - 账面） */
    private BigDecimal diffAmount;

    /** 状态 IMPORTED/RECONCILED/DIFF */
    @Excel(name = "状态", readConverterExp = "IMPORTED=已导入,RECONCILED=已对平,DIFF=有差异")
    private String status;

    /** 导入文件名 */
    private String fileName;

    /** 导入人 */
    private String importBy;

    /** 导入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date importTime;

    /** 归属部门ID */
    private Long deptId;

    /** 上传人ID */
    private Long userId;

    public Long getStatementId() { return statementId; }
    public void setStatementId(Long statementId) { this.statementId = statementId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public Date getStatementDate() { return statementDate; }
    public void setStatementDate(Date statementDate) { this.statementDate = statementDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getBeginBalance() { return beginBalance; }
    public void setBeginBalance(BigDecimal beginBalance) { this.beginBalance = beginBalance; }

    public BigDecimal getBankBeginBalance() { return bankBeginBalance; }
    public void setBankBeginBalance(BigDecimal bankBeginBalance) { this.bankBeginBalance = bankBeginBalance; }

    public BigDecimal getBankEndBalance() { return bankEndBalance; }
    public void setBankEndBalance(BigDecimal bankEndBalance) { this.bankEndBalance = bankEndBalance; }

    public BigDecimal getBookEndBalance() { return bookEndBalance; }
    public void setBookEndBalance(BigDecimal bookEndBalance) { this.bookEndBalance = bookEndBalance; }

    public BigDecimal getDiffAmount() { return diffAmount; }
    public void setDiffAmount(BigDecimal diffAmount) { this.diffAmount = diffAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getImportBy() { return importBy; }
    public void setImportBy(String importBy) { this.importBy = importBy; }

    public Date getImportTime() { return importTime; }
    public void setImportTime(Date importTime) { this.importTime = importTime; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}