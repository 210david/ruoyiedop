package com.ruoyi.pdm.domain;

/**
 * 设计方案配方配比明细对象 pdm_scheme_formula
 *
 * 配方配比属于涉密等级最高的设计资料，查看与导出单独授权（pdm:scheme:formula）。
 *
 * @author ruoyi
 */
public class PdmSchemeFormula
{
    /** 明细ID */
    private Long formulaId;

    /** 方案ID */
    private Long schemeId;

    /** 挂接物料ID（wms_material，选择物料自动带出名称与单位） */
    private Long materialId;

    /** 原料名称 */
    private String itemName;

    /** 配比 */
    private String ratio;

    /** 单位 */
    private String unit;

    /** 允差范围 */
    private String tolerance;

    /** 显示顺序 */
    private Integer orderNum;

    /** 备注 */
    private String remark;

    public Long getFormulaId() { return formulaId; }
    public void setFormulaId(Long formulaId) { this.formulaId = formulaId; }
    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getRatio() { return ratio; }
    public void setRatio(String ratio) { this.ratio = ratio; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getTolerance() { return tolerance; }
    public void setTolerance(String tolerance) { this.tolerance = tolerance; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
