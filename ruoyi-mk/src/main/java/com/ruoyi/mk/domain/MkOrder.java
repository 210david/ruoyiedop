package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.List;

/**
 * 订单 mk_order
 *
 * @author ruoyi
 */
public class MkOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    @Excel(name = "订单编号")
    private String orderNo;

    /** 关联合同ID */
    private Long contractId;

    /** 合同编号 */
    @Excel(name = "合同编号")
    private String contractNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 订单金额 */
    @Excel(name = "订单金额")
    private BigDecimal orderAmount;

    /** 订单状态 */
    @Excel(name = "订单状态", dictType = "marketing_order_status")
    private String orderStatus;

    /** 物流公司 */
    private String logisticsCompany;

    /** 物流单号 */
    private String trackingNo;

    /** 发货时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deliverTime;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /** 负责人ID */
    private Long userId;

    /** 负责人名称 */
    @Excel(name = "负责人")
    private String userName;

/** 部门ID */
private Long deptId;

/** 部门名称 */
@Excel(name = "部门")
private String deptName;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;

    /** 签收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;

    /** 签收人 */
    private String receivePerson;

    /** 取消原因 */
    private String cancelReason;

    /** 删除标志 */
    private String delFlag;

    /** 订单明细列表 */
    private List<MkOrderItem> itemList;

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
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public Date getDeliverTime() { return deliverTime; }
    public void setDeliverTime(Date deliverTime) { this.deliverTime = deliverTime; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
public Long getDeptId() { return deptId; }
public void setDeptId(Long deptId) { this.deptId = deptId; }
public String getDeptName() { return deptName; }
public void setDeptName(String deptName) { this.deptName = deptName; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
    public Date getReceiveTime() { return receiveTime; }
    public void setReceiveTime(Date receiveTime) { this.receiveTime = receiveTime; }
    public String getReceivePerson() { return receivePerson; }
    public void setReceivePerson(String receivePerson) { this.receivePerson = receivePerson; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<MkOrderItem> getItemList() { return itemList; }
    public void setItemList(List<MkOrderItem> itemList) { this.itemList = itemList; }
}
