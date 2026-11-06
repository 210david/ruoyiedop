package com.ruoyi.pjm.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目立项采购清单 pjm_project_procurement
 */
public class PjmProjectProcurement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 清单ID */
    private Long procurementId;

    /** 关联项目ID */
    private Long projectId;

    /** ★ 系统物料ID（关联 wms_material.material_id） */
    private Long materialId;

    /** 明细类型：1物料明细 2劳务外包 3机械设备 4其他 */
    private String detailType;

    /** 序号 */
    private Integer itemNo;

    /** 物料编码 */
    private String itemCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String itemName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 数量 */
    @Excel(name = "数量")
    private BigDecimal qty;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal unitPrice;

    /** 合计金额 */
    @Excel(name = "合计金额")
    private BigDecimal amount;

    public Long getProcurementId() { return procurementId; }
    public void setProcurementId(Long procurementId) { this.procurementId = procurementId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getDetailType() { return detailType; }
    public void setDetailType(String detailType) { this.detailType = detailType; }

    public Integer getItemNo() { return itemNo; }
    public void setItemNo(Integer itemNo) { this.itemNo = itemNo; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

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

    /** 计算金额 */
    public void computeAmount() {
        if (this.qty != null && this.unitPrice != null) {
            this.amount = this.qty.multiply(this.unitPrice);
        }
    }

    private String delFlag;
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
