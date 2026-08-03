package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购申请明细表 pms_purchase_request_detail
 *
 * @author ruoyi
 */
public class PmsPurchaseRequestDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 采购申请ID */
    private Long requestId;

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

    /** 申请数量 */
    @Excel(name = "申请数量")
    private BigDecimal qty;

    /** 预估单价 */
    @Excel(name = "预估单价")
    private BigDecimal estimatedPrice;

    /** 预估金额 */
    @Excel(name = "预估金额")
    private BigDecimal estimatedAmount;

    /** 删除标志 */
    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
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
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
