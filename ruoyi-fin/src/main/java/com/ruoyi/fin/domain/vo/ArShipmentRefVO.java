package com.ruoyi.fin.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 跨模块只读视图对象 —— 发货单（应收单数据源）
 *
 * <p>来自 mk_shipment。</p>
 *
 * <p><b>重要</b>：实测 mk_shipment 只有 total_amount（价税合计），
 * <b>没有税额与不含税金额字段</b>，故不含税额需在财务侧按税率拆分。</p>
 *
 * @author ruoyi
 */
public class ArShipmentRefVO
{
    /** 发货单ID */
    private Long shipmentId;

    /** 发货单号 */
    private String shipmentNo;

    /** 发货日期 */
    private Date shipmentDate;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 价税合计 */
    private BigDecimal totalAmount;

    /** 账期天数（mk_customer 或 fin_partner） */
    private Integer paymentDays;

    /** 往来单位ID（fin_partner 映射） */
    private Long partnerId;

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getShipmentNo() { return shipmentNo; }
    public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }

    public Date getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(Date shipmentDate) { this.shipmentDate = shipmentDate; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getPaymentDays() { return paymentDays; }
    public void setPaymentDays(Integer paymentDays) { this.paymentDays = paymentDays; }

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
}