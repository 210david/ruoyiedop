package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 发货管理明细 mk_shipment_detail
 *
 * @author ruoyi
 */
public class MkShipmentDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 发货单ID */
    private Long shipmentId;

    /** 订单明细ID */
    private Long orderItemId;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String productName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String productSpec;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 发货数量 */
    @Excel(name = "发货数量")
    private BigDecimal shipQty;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    /** 金额 */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 删除标志 */
    private String delFlag;

    /** 订单数量（非数据库字段，用于前端展示） */
    private BigDecimal orderQty;

    /** 已发货数量（非数据库字段，用于前端展示） */
    private BigDecimal shippedQty;

    /** 未发货数量（非数据库字段，用于前端展示） */
    private BigDecimal unshippedQty;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getShipQty() { return shipQty; }
    public void setShipQty(BigDecimal shipQty) { this.shipQty = shipQty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public BigDecimal getOrderQty() { return orderQty; }
    public void setOrderQty(BigDecimal orderQty) { this.orderQty = orderQty; }
    public BigDecimal getShippedQty() { return shippedQty; }
    public void setShippedQty(BigDecimal shippedQty) { this.shippedQty = shippedQty; }
    public BigDecimal getUnshippedQty() { return unshippedQty; }
    public void setUnshippedQty(BigDecimal unshippedQty) { this.unshippedQty = unshippedQty; }
}
