package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 入库单头表 wms_inbound_order
 *
 * @author ruoyi
 */
public class WmsInboundOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 入库单ID */
    private Long orderId;

    /** 入库单号 */
    @Excel(name = "入库单号")
    private String orderNo;

    /** 入库类型 */
    @Excel(name = "入库类型", readConverterExp = "0=采购入库,1=生产入库,2=退货入库,3=调拨入库")
    private String orderType;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    @Excel(name = "供应商")
    private String supplierName;

    /** 目标仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    @Excel(name = "目标仓库")
    private String warehouseName;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=待收货,2=待上架,3=已完成,4=已作废")
    private String status;

    /** 预计入库日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预计入库日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date inboundDate;

    /** 实际完成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeDate;

    /** 总数量 */
    @Excel(name = "总数量")
    private BigDecimal totalQty;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 删除标志 */
    private String delFlag;

    /** 入库单明细列表 */
    private List<WmsInboundOrderDetail> detailList;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getInboundDate() { return inboundDate; }
    public void setInboundDate(Date inboundDate) { this.inboundDate = inboundDate; }
    public Date getCompleteDate() { return completeDate; }
    public void setCompleteDate(Date completeDate) { this.completeDate = completeDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<WmsInboundOrderDetail> getDetailList() { return detailList; }
    public void setDetailList(List<WmsInboundOrderDetail> detailList) { this.detailList = detailList; }
}
