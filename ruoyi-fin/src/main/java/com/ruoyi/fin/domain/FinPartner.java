package com.ruoyi.fin.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 财务往来单位 fin_partner
 *
 * <p>【Q7 决策 · 零侵入】本表通过 source_module + source_id 映射既有主数据
 * （wms_supplier / mk_customer），不改那两张表；财务专属字段（信用额度）
 * 落在此表。</p>
 *
 * <p>【数据权限】人员归属型，dept_id + user_id 供 BizDeptFillAspect 填充
 * 与 @DataScope 过滤。</p>
 *
 * @author ruoyi
 */
public class FinPartner extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 类型：供应商 */
    public static final String TYPE_SUPPLIER = "SUPPLIER";

    /** 类型：客户 */
    public static final String TYPE_CUSTOMER = "CUSTOMER";

    /** 类型：既是供应商又是客户 */
    public static final String TYPE_BOTH = "BOTH";

    /** 往来单位ID */
    private Long partnerId;

    /** 预留账套ID */
    private Long companyId;

    /** 单位编码 */
    @Excel(name = "单位编码")
    private String partnerCode;

    /** 单位名称 */
    @Excel(name = "单位名称")
    private String partnerName;

    /** 类型 SUPPLIER/CUSTOMER/BOTH */
    @Excel(name = "单位类型", readConverterExp = "SUPPLIER=供应商,CUSTOMER=客户,BOTH=既是供应商又是客户")
    private String partnerType;

    /** 来源模块 wms/mk */
    private String sourceModule;

    /** 来源主数据ID（wms_supplier.supplier_id 或 mk_customer.customer_id） */
    private Long sourceId;

    /** 税号 */
    @Excel(name = "税号")
    private String taxNumber;

    /** 发票抬头 */
    @Excel(name = "发票抬头")
    private String invoiceTitle;

    /** 开票地址 */
    @Excel(name = "开票地址")
    private String invoiceAddress;

    /** 开票电话 */
    @Excel(name = "开票电话")
    private String invoicePhone;

    /** 账期天数 */
    @Excel(name = "账期天数")
    private Integer paymentDays;

    /** 结算方式（字典 wms_settlement_type） */
    @Excel(name = "结算方式", dictType = "wms_settlement_type")
    private String settlementType;

    /** 结算币种 */
    private String currency;

    /** 信用额度 */
    @Excel(name = "信用额度")
    private BigDecimal creditLimit;

    /** 已用信用额度 */
    private BigDecimal creditUsed;

    /** 归属部门ID */
    private Long deptId;

    /** 负责人ID */
    private Long userId;

    /** 状态 0正常 1停用 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getPartnerCode() { return partnerCode; }
    public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getPartnerType() { return partnerType; }
    public void setPartnerType(String partnerType) { this.partnerType = partnerType; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }

    public String getInvoiceTitle() { return invoiceTitle; }
    public void setInvoiceTitle(String invoiceTitle) { this.invoiceTitle = invoiceTitle; }

    public String getInvoiceAddress() { return invoiceAddress; }
    public void setInvoiceAddress(String invoiceAddress) { this.invoiceAddress = invoiceAddress; }

    public String getInvoicePhone() { return invoicePhone; }
    public void setInvoicePhone(String invoicePhone) { this.invoicePhone = invoicePhone; }

    public Integer getPaymentDays() { return paymentDays; }
    public void setPaymentDays(Integer paymentDays) { this.paymentDays = paymentDays; }

    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public BigDecimal getCreditUsed() { return creditUsed; }
    public void setCreditUsed(BigDecimal creditUsed) { this.creditUsed = creditUsed; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * 剩余可用信用额度。
     *
     * @return 额度 - 已用
     */
    public BigDecimal getCreditAvailable()
    {
        BigDecimal limit = creditLimit == null ? BigDecimal.ZERO : creditLimit;
        BigDecimal used = creditUsed == null ? BigDecimal.ZERO : creditUsed;
        return limit.subtract(used);
    }
}