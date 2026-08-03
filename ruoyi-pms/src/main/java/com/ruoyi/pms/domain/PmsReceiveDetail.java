package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 收货验收明细表 pms_receive_detail
 *
 * @author ruoyi
 */
public class PmsReceiveDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 收货ID */
    private Long receiveId;

    /** 订单明细ID */
    private Long orderDetailId;

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

    /** 应收数量 */
    @Excel(name = "应收数量")
    private BigDecimal receiveQty;

    /** 合格数量 */
    @Excel(name = "合格数量")
    private BigDecimal qualifiedQty;

    /** 不合格数量 */
    @Excel(name = "不合格数量")
    private BigDecimal unqualifiedQty;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    /** 金额 */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生产日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date productionDate;

    /** 保质期至 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "保质期至", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expiryDate;

    /** 删除标志 */
    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getReceiveId() { return receiveId; }
    public void setReceiveId(Long receiveId) { this.receiveId = receiveId; }
    public Long getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(Long orderDetailId) { this.orderDetailId = orderDetailId; }
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
    public BigDecimal getReceiveQty() { return receiveQty; }
    public void setReceiveQty(BigDecimal receiveQty) { this.receiveQty = receiveQty; }
    public BigDecimal getQualifiedQty() { return qualifiedQty; }
    public void setQualifiedQty(BigDecimal qualifiedQty) { this.qualifiedQty = qualifiedQty; }
    public BigDecimal getUnqualifiedQty() { return unqualifiedQty; }
    public void setUnqualifiedQty(BigDecimal unqualifiedQty) { this.unqualifiedQty = unqualifiedQty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Date getProductionDate() { return productionDate; }
    public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
