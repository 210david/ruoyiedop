package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 发货管理 mk_shipment
 *
 * @author ruoyi
 */
public class MkShipment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 发货单ID */
    private Long shipmentId;

    /** 发货单号 */
    @Excel(name = "发货单号")
    private String shipmentNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号（冗余） */
    @Excel(name = "订单编号")
    private String orderNo;

    /** 合同ID（可选，支持合同直接发货） */
    private Long contractId;

    /** 合同编号（冗余） */
    @Excel(name = "合同编号")
    private String contractNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称（冗余） */
    @Excel(name = "客户名称")
    private String customerName;

    /** 状态（字典 marketing_shipment_status） */
    @Excel(name = "状态", dictType = "marketing_shipment_status")
    private String status;

    /** 发货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发货日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date shipmentDate;

    /** 物流公司 */
    @Excel(name = "物流公司")
    private String logisticsCompany;

    /** 物流单号 */
    @Excel(name = "物流单号")
    private String trackingNo;

    /** 发货总数量 */
    @Excel(name = "发货总数量")
    private BigDecimal totalQty;

    /** 发货总金额 */
    @Excel(name = "发货总金额")
    private BigDecimal totalAmount;

    /** 收货人 */
    @Excel(name = "收货人")
    private String receiverName;

    /** 收货电话 */
    @Excel(name = "收货电话")
    private String receiverPhone;

    /** 收货地址 */
    @Excel(name = "收货地址")
    private String receiverAddress;

    /** 发货人ID */
    private Long shipperId;

    /** 发货人姓名 */
    @Excel(name = "发货人")
    private String shipperName;

    /** 出库单号（手动填写，关联仓库出库单） */
    @Excel(name = "出库单号")
    private String outboundOrderNo;

    /** 删除标志 */
    private String delFlag;

    /** 发货明细列表 */
    private List<MkShipmentDetail> detailList;

    /** 订单总数量（非数据库字段，用于前端展示） */
    private BigDecimal orderTotalQty;

    /** 订单已发货数量（非数据库字段，用于前端展示） */
    private BigDecimal orderShippedQty;

    /** 订单未发货数量（非数据库字段，用于前端展示） */
    private BigDecimal orderUnshippedQty;

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public String getShipmentNo() { return shipmentNo; }
    public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getShipmentDate() { return shipmentDate; }
    public void setShipmentDate(Date shipmentDate) { this.shipmentDate = shipmentDate; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverAddress() { return receiverAddress; }
    public void setReceiverAddress(String receiverAddress) { this.receiverAddress = receiverAddress; }
    public Long getShipperId() { return shipperId; }
    public void setShipperId(Long shipperId) { this.shipperId = shipperId; }
    public String getShipperName() { return shipperName; }
    public void setShipperName(String shipperName) { this.shipperName = shipperName; }
    public String getOutboundOrderNo() { return outboundOrderNo; }
    public void setOutboundOrderNo(String outboundOrderNo) { this.outboundOrderNo = outboundOrderNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<MkShipmentDetail> getDetailList() { return detailList; }
    public void setDetailList(List<MkShipmentDetail> detailList) { this.detailList = detailList; }
    public BigDecimal getOrderTotalQty() { return orderTotalQty; }
    public void setOrderTotalQty(BigDecimal orderTotalQty) { this.orderTotalQty = orderTotalQty; }
    public BigDecimal getOrderShippedQty() { return orderShippedQty; }
    public void setOrderShippedQty(BigDecimal orderShippedQty) { this.orderShippedQty = orderShippedQty; }
    public BigDecimal getOrderUnshippedQty() { return orderUnshippedQty; }
    public void setOrderUnshippedQty(BigDecimal orderUnshippedQty) { this.orderUnshippedQty = orderUnshippedQty; }
}
