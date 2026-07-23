package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单退货 mk_order_return
 *
 * @author ruoyi
 */
public class MkOrderReturn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 退货ID */
    private Long returnId;

    /** 退货编号 */
    @Excel(name = "退货编号")
    private String returnNo;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    @Excel(name = "订单编号")
    private String orderNo;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 退货金额 */
    @Excel(name = "退货金额")
    private BigDecimal returnAmount;

    /** 退货原因 */
    @Excel(name = "退货原因")
    private String returnReason;

    /** 退货状态 */
    @Excel(name = "退货状态", dictType = "marketing_return_status")
    private String returnStatus;

    /** 退款金额 */
    @Excel(name = "退款金额")
    private BigDecimal refundAmount;

    /** 退款日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "退款日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date refundDate;

    /** 审批意见 */
    @Excel(name = "审批意见")
    private String approveOpinion;

    /** 审批人 */
    @Excel(name = "审批人")
    private String approveBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 删除标志 */
    private String delFlag;

    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }
    public String getReturnNo() { return returnNo; }
    public void setReturnNo(String returnNo) { this.returnNo = returnNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public BigDecimal getReturnAmount() { return returnAmount; }
    public void setReturnAmount(BigDecimal returnAmount) { this.returnAmount = returnAmount; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public Date getRefundDate() { return refundDate; }
    public void setRefundDate(Date refundDate) { this.refundDate = refundDate; }
    public String getApproveOpinion() { return approveOpinion; }
    public void setApproveOpinion(String approveOpinion) { this.approveOpinion = approveOpinion; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
