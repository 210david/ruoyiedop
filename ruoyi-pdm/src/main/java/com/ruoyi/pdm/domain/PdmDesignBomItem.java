package com.ruoyi.pdm.domain;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * 设计BOM明细对象 pdm_design_bom_item
 *
 * @author ruoyi
 */
public class PdmDesignBomItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** BOM ID */
    private Long bomId;

    /** 父明细ID（0=顶级） */
    private Long parentId;

    /** 层级（1=顶级） */
    private Integer itemLevel;

    /** 物料ID（wms_material，物料对照） */
    private Long materialId;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 规格型号 */
    private String specModel;

    /** 计量单位 */
    private String unit;

    /** 用量 */
    private BigDecimal usageQty;

    /** 损耗率(%) */
    private BigDecimal lossRate;

    /** 显示顺序 */
    private Integer orderNum;

    /** 备注 */
    private String remark;

    /** 父行序号（前端编辑提交用临时字段：-1=顶级，否则为父行在列表中的下标，不入库） */
    private Integer parentIndex;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getItemLevel() { return itemLevel; }
    public void setItemLevel(Integer itemLevel) { this.itemLevel = itemLevel; }
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
    public BigDecimal getUsageQty() { return usageQty; }
    public void setUsageQty(BigDecimal usageQty) { this.usageQty = usageQty; }
    public BigDecimal getLossRate() { return lossRate; }
    public void setLossRate(BigDecimal lossRate) { this.lossRate = lossRate; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getParentIndex() { return parentIndex; }
    public void setParentIndex(Integer parentIndex) { this.parentIndex = parentIndex; }
}
