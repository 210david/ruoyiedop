package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 收货验收表 pms_receive
 *
 * @author ruoyi
 */
public class PmsReceive extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 收货ID */
    private Long receiveId;

    /** 收货单号 */
    @Excel(name = "收货单号")
    private String receiveNo;

    /** 采购订单ID */
    private Long orderId;

    /** 采购单号（冗余） */
    @Excel(name = "采购单号")
    private String orderNo;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称（冗余） */
    @Excel(name = "供应商")
    private String supplierName;

    /** 入库仓库ID */
    private Long warehouseId;

    /** 状态（字典 pms_receive_status） */
    @Excel(name = "状态", readConverterExp = "0=待验收,1=部分验收,2=已验收,3=已入库,4=已作废")
    private String status;

    /** 收货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "收货日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date receiveDate;

    /** 应收总数量 */
    @Excel(name = "应收总数量")
    private BigDecimal totalQty;

    /** 合格数量 */
    @Excel(name = "合格数量")
    private BigDecimal qualifiedQty;

    /** 不合格数量 */
    @Excel(name = "不合格数量")
    private BigDecimal unqualifiedQty;

    /** 验收人ID */
    private Long inspectorId;

    /** 验收人姓名（冗余） */
    @Excel(name = "验收人")
    private String inspectorName;

    /** 关联入库单ID */
    private Long inboundOrderId;

    /** 删除标志 */
    private String delFlag;

    /** 收货明细列表 */
    private List<PmsReceiveDetail> detailList;

    /** 审核人 */
    @Excel(name = "审核人")
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditOpinion;

    /** 审核日志列表 */
    private List<PmsReceiveAuditLog> auditLogList;

    /** 入库单号（非数据库字段，用于展示） */
    private String inboundOrderNo;

    public Long getReceiveId() { return receiveId; }
    public void setReceiveId(Long receiveId) { this.receiveId = receiveId; }
    public String getReceiveNo() { return receiveNo; }
    public void setReceiveNo(String receiveNo) { this.receiveNo = receiveNo; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getReceiveDate() { return receiveDate; }
    public void setReceiveDate(Date receiveDate) { this.receiveDate = receiveDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getQualifiedQty() { return qualifiedQty; }
    public void setQualifiedQty(BigDecimal qualifiedQty) { this.qualifiedQty = qualifiedQty; }
    public BigDecimal getUnqualifiedQty() { return unqualifiedQty; }
    public void setUnqualifiedQty(BigDecimal unqualifiedQty) { this.unqualifiedQty = unqualifiedQty; }
    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }
    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
    public Long getInboundOrderId() { return inboundOrderId; }
    public void setInboundOrderId(Long inboundOrderId) { this.inboundOrderId = inboundOrderId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PmsReceiveDetail> getDetailList() { return detailList; }
    public void setDetailList(List<PmsReceiveDetail> detailList) { this.detailList = detailList; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public List<PmsReceiveAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsReceiveAuditLog> auditLogList) { this.auditLogList = auditLogList; }
    public String getInboundOrderNo() { return inboundOrderNo; }
    public void setInboundOrderNo(String inboundOrderNo) { this.inboundOrderNo = inboundOrderNo; }
}
