package com.ruoyi.food.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 物料食品行业参数 food_material_param
 *
 * 配方双模型：火腿按产出率模型（投入鲜腿→产出成品）、腊肉香肠按BOM配比配方；
 * 排产双模式：批次计划模式 / 离散日排产，按物料配置。
 *
 * @author ruoyi
 */
public class FoodMaterialParam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long paramId;

    private Long materialId;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    /** 配方模型(1产出率模型2配比配方) */
    @Excel(name = "配方模型", readConverterExp = "1=产出率模型,2=配比配方")
    private String yieldModel;

    /** 基准产出率(%)，如干腌火腿76% */
    @Excel(name = "基准产出率(%)")
    private java.math.BigDecimal stdYieldRate;

    /** 产出率告警下限(%) */
    @Excel(name = "产出率告警下限(%)")
    private java.math.BigDecimal yieldAlarmRate;

    /** 排产模式(1批次计划2离散日排产) */
    @Excel(name = "排产模式", readConverterExp = "1=批次计划模式,2=离散日排产")
    private String scheduleMode;

    private String delFlag;

    public Long getParamId() { return paramId; }
    public void setParamId(Long paramId) { this.paramId = paramId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getYieldModel() { return yieldModel; }
    public void setYieldModel(String yieldModel) { this.yieldModel = yieldModel; }
    public java.math.BigDecimal getStdYieldRate() { return stdYieldRate; }
    public void setStdYieldRate(java.math.BigDecimal stdYieldRate) { this.stdYieldRate = stdYieldRate; }
    public java.math.BigDecimal getYieldAlarmRate() { return yieldAlarmRate; }
    public void setYieldAlarmRate(java.math.BigDecimal yieldAlarmRate) { this.yieldAlarmRate = yieldAlarmRate; }
    public String getScheduleMode() { return scheduleMode; }
    public void setScheduleMode(String scheduleMode) { this.scheduleMode = scheduleMode; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
