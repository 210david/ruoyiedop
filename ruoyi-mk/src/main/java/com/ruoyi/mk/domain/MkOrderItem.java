package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单明细 mk_order_item
 *
 * @author ruoyi
 */
public class MkOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 订单ID */
    private Long orderId;

    /** 行号 */
    @Excel(name = "行号")
    private Integer lineNo;

    /** 商品名称 */
    @Excel(name = "商品名称")
    private String productName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String productSpec;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 数量 */
    @Excel(name = "数量")
    private Integer quantity;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    /** 小计 */
    @Excel(name = "小计")
    private BigDecimal subtotal;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductSpec() { return productSpec; }
    public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
