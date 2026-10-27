package com.ruoyi.fin.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 应收单 fin_ar_order（FM-2）
 *
 * <p><b>与 MK 的边界</b>：本表以 mk_shipment（发货单）为数据源生成应收单，
 * received_amount 由 mk_payment_record 同步，<b>不在财务侧重复登记收款</b>
 * （避免与 MK 的收款记录重复收款）。</p>
 *
 * <p><b>税额拆分说明</b>：mk_shipment 只有 total_amount（价税合计），
 * 无税额字段，故 amount_excl_tax 与 tax_amount 在本表按 tax_rate 拆分：
 * <pre>
 *   不含税额= 价税合计 / (1 + 税率)
 *   销项税额 = 价税合计 - 不含税额
 * </pre>
 *
 * @author ruoyi
 */
public class FinArOrder extends BaseEntity
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

    /** 应收单ID */
    private Long arId;

    /** 预留账套ID */
    private Long companyId;

    /** 应收单号 */
    @Excel(name = "应收单号")
    private String arNo;

    /** 来源模块 */
    private String sourceModule;

    /** 来源类型 */
    private String sourceType;

    /** 来源单据ID */
    private Long sourceId;

    /** 来源单据号 */
    @Excel(name = "来源发货单")
    private String sourceKey;

    /** 往来单位ID */
    private Long partnerId;

    /** 客户名称 */
    @Excel(name = "客户")
    private String partnerName;

    /** 客户ID（只读引用 mk_shipment.customer_id） */
    private Long customerId;

    /** 应收日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "应收日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date arDate;

    /** 会计期间 */
    @Excel(name = "会计期间")
    private String period;

    /** 不含税金额 */
    @Excel(name = "不含税金额")
    private BigDecimal amountExclTax;

    /** 销项税额 */
    @Excel(name = "销项税额")
    private BigDecimal taxAmount;

    /** 价税合计 */
    @Excel(name = "价税合计")
    private BigDecimal totalAmount;

    /** 已收金额（从 MK 同步） */
    @Excel(name = "已收金额")
    private BigDecimal receivedAmount;

    /** 已计提坏账 */
    private BigDecimal badDebtAmount;

    /** 税率 */
    private BigDecimal taxRate;

    /** 销项发票号码 */
    @Excel(name = "发票号码")
    private String invoiceNo;

    /** 销项发票代码 */
    private String invoiceCode;

    /** 开票日期 */
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

    public Long getArId() { return arId; }
    public void setArId(Long arId) { this.arId = arId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getArNo() { return arNo; }
    public void setArNo(String arNo) { this.arNo = arNo; }

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

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Date getArDate() { return arDate; }
    public void setArDate(Date arDate) { this.arDate = arDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getAmountExclTax() { return amountExclTax; }
    public void setAmountExclTax(BigDecimal amountExclTax) { this.amountExclTax = amountExclTax; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }

    public BigDecimal getBadDebtAmount() { return badDebtAmount; }
    public void setBadDebtAmount(BigDecimal badDebtAmount) { this.badDebtAmount = badDebtAmount; }

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
     * 未收余额。
     *
     * @return 价税合计 - 已收
     */
    public BigDecimal getUnreceivedAmount()
    {
        BigDecimal total = totalAmount == null ? BigDecimal.ZERO : totalAmount;
        BigDecimal received = receivedAmount == null ? BigDecimal.ZERO : receivedAmount;
        return total.subtract(received);
    }

    /**
     * 是否逾期未收。
     *
     * @param today 今日
     * @return true=逾期
     */
    public boolean isOverdue(Date today)
    {
        if (dueDate == null || status == null)
        {
            return false;
        }
        if (STATUS_SETTLED.equals(status) || STATUS_VOID.equals(status))
        {
            return false;
        }
        return getUnreceivedAmount().compareTo(BigDecimal.ZERO) > 0 && dueDate.before(today);
    }
}