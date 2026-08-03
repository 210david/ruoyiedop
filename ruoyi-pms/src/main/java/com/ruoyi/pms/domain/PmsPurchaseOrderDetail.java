package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购订单明细表 pms_purchase_order_detail
 *
 * @author ruoyi
 */
public class PmsPurchaseOrderDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 采购订单ID */
    private Long orderId;

    /** 关联申请明细ID */
    private Long requestDetailId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码（冗余） */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称（冗余） */
    @Excel(name = "物料名称")
    private String materialName;

    /** 规格型号（冗余） */
    @Excel(name = "规格型号")
    private String specModel;

    /** 单位（冗余） */
    @Excel(name = "单位")
    private String unit;

    /** 采购数量 */
    @Excel(name = "采购数量")
    private BigDecimal qty;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    /** 金额 */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 已收数量 */
    @Excel(name = "已收数量")
    private BigDecimal receivedQty;

    /** 删除标志 */
    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getRequestDetailId() { return requestDetailId; }
    public void setRequestDetailId(Long requestDetailId) { this.requestDetailId = requestDetailId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public BigDecimal getReceivedQty() { return receivedQty; }
    public void setReceivedQty(BigDecimal receivedQty) { this.receivedQty = receivedQty; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
