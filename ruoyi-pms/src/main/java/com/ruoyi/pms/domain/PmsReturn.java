package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 退货表 pms_return
 *
 * @author ruoyi
 */
public class PmsReturn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 退货ID */
    private Long returnId;

    /** 退货单号 */
    @Excel(name = "退货单号")
    private String returnNo;

    /** 采购订单ID */
    private Long orderId;

    /** 采购单号（冗余） */
    @Excel(name = "采购单号")
    private String orderNo;

    /** 关联收货ID */
    private Long receiveId;

    /** 收货单号（冗余） */
    @Excel(name = "收货单号")
    private String receiveNo;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 状态（字典 pms_return_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=待审批,2=已审批,3=已退货,4=已作废,5=已驳回")
    private String status;

    /** 退货类型（字典 pms_return_type） */
    @Excel(name = "退货类型", readConverterExp = "0=质量问题,1=规格不符,2=多收,3=其他")
    private String returnType;

    /** 退货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "退货日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date returnDate;

    /** 退货总数量 */
    @Excel(name = "退货总数量")
    private BigDecimal totalQty;

    /** 退货总金额 */
    @Excel(name = "退货总金额")
    private BigDecimal totalAmount;

    /** 退货原因 */
    @Excel(name = "退货原因")
    private String reason;

    /** 经办人ID */
    private Long handlerId;

    /** 经办人姓名（冗余） */
    @Excel(name = "经办人")
    private String handlerName;

    /** 删除标志 */
    private String delFlag;

    /** 退货明细列表 */
    private List<PmsReturnDetail> detailList;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditOpinion;

    /** 出库单ID */
    private Long outboundOrderId;

    /** 出库单号（非数据库字段，用于展示） */
    private String outboundOrderNo;

    /** 审核日志列表 */
    private List<PmsReturnAuditLog> auditLogList;

    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }
    public String getReturnNo() { return returnNo; }
    public void setReturnNo(String returnNo) { this.returnNo = returnNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getReceiveId() { return receiveId; }
    public void setReceiveId(Long receiveId) { this.receiveId = receiveId; }
    public String getReceiveNo() { return receiveNo; }
    public void setReceiveNo(String receiveNo) { this.receiveNo = receiveNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }
    public String getHandlerName() { return handlerName; }
    public void setHandlerName(String handlerName) { this.handlerName = handlerName; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PmsReturnDetail> getDetailList() { return detailList; }
    public void setDetailList(List<PmsReturnDetail> detailList) { this.detailList = detailList; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public Long getOutboundOrderId() { return outboundOrderId; }
    public void setOutboundOrderId(Long outboundOrderId) { this.outboundOrderId = outboundOrderId; }
    public String getOutboundOrderNo() { return outboundOrderNo; }
    public void setOutboundOrderNo(String outboundOrderNo) { this.outboundOrderNo = outboundOrderNo; }
    public List<PmsReturnAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsReturnAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
