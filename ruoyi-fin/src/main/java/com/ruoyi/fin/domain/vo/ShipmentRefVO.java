package com.ruoyi.fin.domain.vo;

import java.math.BigDecimal;

/**
 * 跨模块只读视图对象 —— 发货单（应收来源）
 *
 * <p>对应 mk_shipment.total_amount，是应收单生成的金额依据。</p>
 *
 * @author ruoyi
 */
public class ShipmentRefVO
{
    /** 发货单ID */
    private Long shipmentId;

    /** 发货单号 */
    private String shipmentNo;

    /** 订单ID */
    private Long orderId;

    /** 合同ID */
    private Long contractId;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    /** 发货总金额（应收金额依据） */
    private BigDecimal totalAmount;

    /** 发货总数量 */
    private BigDecimal totalQty;

    /** 发货日期 */
    private java.util.Date shipmentDate;

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getShipmentNo() { return shipmentNo; }
    public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }

    public java.util.Date getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(java.util.Date shipmentDate) { this.shipmentDate = shipmentDate; }
}
