package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 进项税明细 fin_input_tax（进项税额抵扣台账）
 *
 * <p>记录每张采购发票的可抵扣进项税额及其抵扣状态。
 * 依据增值税暂行条例，进项税须在认证抵扣期内使用，
 * 逾期未抵扣的进项税不得抵扣（这是 PENDING / EXPIRED 两种状态的由来）。</p>
 *
 * @author ruoyi
 */
public class FinInputTax extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 抵扣状态：待抵扣 */
    public static final String STATUS_PENDING = "PENDING";

    /** 抵扣状态：已抵扣 */
    public static final String STATUS_DEDUCTED = "DEDUCTED";

    /** 抵扣状态：已过期 */
    public static final String STATUS_EXPIRED = "EXPIRED";

    /** 抵扣状态：已作废 */
    public static final String STATUS_VOID = "VOID";

    /** 进项税ID */
    private Long taxId;

    /** 预留账套ID */
    private Long companyId;

    /** 关联应付单ID */
    private Long apId;

    /** 供应商ID */
    private Long partnerId;

    /** 供应商名称 */
    @Excel(name = "供应商")
    private String partnerName;

    /** 发票代码 */
    private String invoiceCode;

    /** 发票号码 */
    @Excel(name = "发票号码")
    private String invoiceNo;

    /** 开票日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开票日期", dateFormat = "yyyy-MM-dd")
    private Date invoiceDate;

    /** 税率 */
    private BigDecimal taxRate;

    /** 不含税金额 */
    @Excel(name = "不含税金额")
    private BigDecimal amountExclTax;

    /** 进项税额 */
    @Excel(name = "进项税额")
    private BigDecimal taxAmount;

    /** 抵扣状态 */
    @Excel(name = "抵扣状态", dictType = "fin_deduct_status")
    private String deductStatus;

    /** 抵扣所属期间 */
    private String deductPeriod;

    /**
     * 抵扣截止日（<b>运行时计算，不入库</b>）。
     *
     * <p>表结构无此列，按开票日 +12 个月（《增值税暂行条例》规定的
     * 认证抵扣期限）动态算出，供界面提示「还剩多少天可抵扣」。</p>
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deductDeadline;

    /** 关联凭证ID */
    private Long voucherId;

    /** 归属部门ID */
    private Long deptId;

    /** 经办人ID */
    private Long userId;

    public Long getTaxId() { return taxId; }
    public void setTaxId(Long taxId) { this.taxId = taxId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Long getApId() { return apId; }
    public void setApId(Long apId) { this.apId = apId; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    public Date getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(Date invoiceDate) { this.invoiceDate = invoiceDate; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public BigDecimal getAmountExclTax() { return amountExclTax; }
    public void setAmountExclTax(BigDecimal amountExclTax) { this.amountExclTax = amountExclTax; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public String getDeductStatus() { return deductStatus; }
    public void setDeductStatus(String deductStatus) { this.deductStatus = deductStatus; }

    public String getDeductPeriod() { return deductPeriod; }
    public void setDeductPeriod(String deductPeriod) { this.deductPeriod = deductPeriod; }

    public Date getDeductDeadline() { return deductDeadline; }
    public void setDeductDeadline(Date deductDeadline) { this.deductDeadline = deductDeadline; }

    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}