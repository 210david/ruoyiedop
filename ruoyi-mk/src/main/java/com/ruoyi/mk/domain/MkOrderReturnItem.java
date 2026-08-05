package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单退货明细 mk_order_return_item
 *
 * @author ruoyi
 */
public class MkOrderReturnItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 退货ID */
    private Long returnId;

    /** 订单明细ID */
    private Long orderItemId;

    /** 商品名称（冗余） */
    @Excel(name = "商品名称")
    private String productName;

    /** 规格型号（冗余） */
    @Excel(name = "规格型号")
    private String productSpec;

    /** 单位（冗余） */
    @Excel(name = "单位")
    private String unit;

    /** 订单数量 */
    @Excel(name = "订单数量")
    private BigDecimal orderQty;

    /** 发货数量 */
    @Excel(name = "发货数量")
    private BigDecimal shipQty;

    /** 退货数量 */
    @Excel(name = "退货数量")
    private BigDecimal returnQty;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    /** 退货金额 */
    @Excel(name = "退货金额")
    private BigDecimal returnAmount;

    /** 退货原因 */
    @Excel(name = "退货原因")
    private String returnReason;

    /** 删除标志 */
    private String delFlag;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getOrderQty() { return orderQty; }
    public void setOrderQty(BigDecimal orderQty) { this.orderQty = orderQty; }
    public BigDecimal getShipQty() { return shipQty; }
    public void setShipQty(BigDecimal shipQty) { this.shipQty = shipQty; }
    public BigDecimal getReturnQty() { return returnQty; }
    public void setReturnQty(BigDecimal returnQty) { this.returnQty = returnQty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getReturnAmount() { return returnAmount; }
    public void setReturnAmount(BigDecimal returnAmount) { this.returnAmount = returnAmount; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
