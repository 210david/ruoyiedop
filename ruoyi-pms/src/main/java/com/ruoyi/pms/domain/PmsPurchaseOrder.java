package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购订单表 pms_purchase_order
 *
 * @author ruoyi
 */
public class PmsPurchaseOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 采购单号 */
    @Excel(name = "采购单号")
    private String orderNo;

    /** 关联申请ID */
    private Long requestId;

    /** 关联申请单号（冗余） */
    @Excel(name = "关联申请单号")
    private String requestNo;

    /** 关联合同ID */
    private Long contractId;

    /** 关联合同编号（冗余） */
    @Excel(name = "关联合同")
    private String contractNo;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 目标仓库ID */
    private Long warehouseId;

    /** 状态（字典 pms_order_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=待审批,2=已审批,3=已下单,4=部分到货,5=已完成,6=已作废")
    private String status;

    /** 采购日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "采购日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date orderDate;

    /** 预计交货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预计交货日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expectDeliveryDate;

    /** 实际交货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "实际交货日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date actualDeliveryDate;

    /** 总数量 */
    @Excel(name = "总数量")
    private BigDecimal totalQty;

    /** 总金额 */
    @Excel(name = "总金额")
    private BigDecimal totalAmount;

    /** 已收金额 */
    @Excel(name = "已收金额")
    private BigDecimal receivedAmount;

    /** 已付金额 */
    @Excel(name = "已付金额")
    private BigDecimal paidAmount;

    /** 采购员ID */
    private Long buyerId;

    /** 采购员姓名（冗余） */
    @Excel(name = "采购员")
    private String buyerName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称（冗余） */
    @Excel(name = "部门")
    private String deptName;

    /** 付款方式（字典 wms_payment_method） */
    @Excel(name = "付款方式", readConverterExp = "0=现结,1=月结,2=预付")
    private String paymentMethod;

    /** 账期天数 */
    @Excel(name = "账期天数")
    private Integer paymentDays;

    /** 结算方式（字典 wms_settlement_type） */
    @Excel(name = "结算方式", readConverterExp = "0=银行转账,1=承兑汇票,2=现金")
    private String settlementType;

    /** 结算币种（字典 wms_currency） */
    @Excel(name = "结算币种")
    private String currency;

    /** 交货地址 */
    @Excel(name = "交货地址")
    private String deliveryAddress;

    /** 审批人 */
    @Excel(name = "审批人")
    private String auditBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审批意见 */
    private String auditOpinion;

    /** 删除标志 */
    private String delFlag;

    /** 采购订单明细列表 */
    private List<PmsPurchaseOrderDetail> detailList;

    /** 审核日志列表 */
    private List<PmsOrderAuditLog> auditLogList;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public String getContractNo() { return contractNo; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    public Date getExpectDeliveryDate() { return expectDeliveryDate; }
    public void setExpectDeliveryDate(Date expectDeliveryDate) { this.expectDeliveryDate = expectDeliveryDate; }
    public Date getActualDeliveryDate() { return actualDeliveryDate; }
    public void setActualDeliveryDate(Date actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Integer getPaymentDays() { return paymentDays; }
    public void setPaymentDays(Integer paymentDays) { this.paymentDays = paymentDays; }
    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PmsPurchaseOrderDetail> getDetailList() { return detailList; }
    public void setDetailList(List<PmsPurchaseOrderDetail> detailList) { this.detailList = detailList; }
    public List<PmsOrderAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsOrderAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
