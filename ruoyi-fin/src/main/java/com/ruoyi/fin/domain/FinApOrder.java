package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 应付单 fin_ap_order（FM-3）
 *
 * <p><b>与 PMS 的边界</b>：本表以 pms_invoice 为数据源生成应付单，
 * paid_amount 由 pms_invoice.payment_amount 同步而来，
 * <b>不在财务侧重复登记付款</b>（避免与 pms_invoice_payment 重复付款）。
 * 出纳确需手工登记时走 fin_payment_record 且 source_module='manual'。</p>
 *
 * <p><b>核销</b>：财务经理确认「这笔应付可以销账」的动作，
 * 与出纳「实际付钱」分开，留痕于 fin_ap_writeoff。</p>
 *
 * @author ruoyi
 */
public class FinApOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 状态：未核销 */
    public static final String STATUS_OPEN = "OPEN";

    /** 状态：部分核销 */
    public static final String STATUS_PARTIAL = "PARTIAL";

    /** 状态：已核销 */
    public static final String STATUS_SETTLED = "SETTLED";

    /** 状态：已作废 */
    public static final String STATUS_VOID = "VOID";

    /** 应付单ID */
    private Long apId;

    /** 预留账套ID */
    private Long companyId;

    /** 应付单号 */
    @Excel(name = "应付单号")
    private String apNo;

    /** 来源模块 */
    private String sourceModule;

    /** 来源类型 */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据号 */
    @Excel(name = "来源单据")
    private String sourceKey;

    /** 往来单位ID */
    private Long partnerId;

    /** 往来单位名称 */
    @Excel(name = "供应商")
    private String partnerName;

    /** 供应商ID（只读引用 pms_invoice.supplier_id） */
    private Long supplierId;

    /** 应付日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "应付日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date apDate;

    /** 会计期间 */
    @Excel(name = "会计期间")
    private String period;

    /** 不含税金额 */
    @Excel(name = "不含税金额")
    private BigDecimal amountExclTax;

    /** 税额 */
    @Excel(name = "税额")
    private BigDecimal taxAmount;

    /** 价税合计 */
    @Excel(name = "价税合计")
    private BigDecimal totalAmount;

    /** 已付金额（从 PMS 同步） */
    @Excel(name = "已付金额")
    private BigDecimal paidAmount;

    /** 税率 */
    private BigDecimal taxRate;

    /** 发票号码 */
    @Excel(name = "发票号码")
    private String invoiceNo;

    /** 发票代码 */
    private String invoiceCode;

    /** 发票日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date invoiceDate;

    /** 到期日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "到期日", dateFormat = "yyyy-MM-dd")
    private Date dueDate;

    /** 账期天数 */
    private Integer paymentDays;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "OPEN=未核销,PARTIAL=部分核销,SETTLED=已核销,VOID=已作废")
    private String status;

    /** 归属部门ID */
    private Long deptId;

    /** 制单人ID */
    private Long userId;

    public Long getApId() { return apId; }
    public void setApId(Long apId) { this.apId = apId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getApNo() { return apNo; }
    public void setApNo(String apNo) { this.apNo = apNo; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getSourceKey() { return sourceKey; }
    public void setSourceKey(String sourceKey) { this.sourceKey = sourceKey; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public Date getApDate() { return apDate; }
    public void setApDate(Date apDate) { this.apDate = apDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getAmountExclTax() { return amountExclTax; }
    public void setAmountExclTax(BigDecimal amountExclTax) { this.amountExclTax = amountExclTax; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String invoiceCode) { this.invoiceCode = invoiceCode; }

    public Date getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(Date invoiceDate) { this.invoiceDate = invoiceDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Integer getPaymentDays() { return paymentDays; }
    public void setPaymentDays(Integer paymentDays) { this.paymentDays = paymentDays; }

public String getStatus() { return status; }

public void setStatus(String status) { this.status = status; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    /**
     * 未付余额。
     *
     * @return 价税合计 - 已付
     */
    public BigDecimal getUnpaidAmount()
    {
        BigDecimal total = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        BigDecimal paid = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        return total.subtract(paid);
    }

    /**
     * 是否已到期未付（逾期预警）。
     *
     * @param today 今日
     * @return true=逾期
     */
    public boolean isOverdue(Date today)
    {
        if (dueDate == null || getStatus() == null)
        {
            return false;
        }
        if (STATUS_SETTLED.equals(getStatus()) || STATUS_VOID.equals(getStatus()))
        {
            return false;
        }
        return getUnpaidAmount().compareTo(BigDecimal.ZERO) > 0 && dueDate.before(today);
    }
}