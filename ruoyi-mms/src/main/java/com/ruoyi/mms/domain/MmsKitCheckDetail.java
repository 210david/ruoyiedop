package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 齐套检查明细 mms_kit_check_detail
 *
 * @author ruoyi
 */
public class MmsKitCheckDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long detailId;
    private Long kitId;
    private Long materialId;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位", readConverterExp = "0=个,1=件,2=箱,3=kg,4=吨,5=米,6=把,7=千米,8=厘米,9=套,10=台,11=条")
    private String unit;

    @Excel(name = "需求数量")
    private BigDecimal requiredQty;

    @Excel(name = "可用数量")
    private BigDecimal availableQty;

    @Excel(name = "缺口量")
    private BigDecimal shortageQty;

    @Excel(name = "是否齐套", readConverterExp = "0=否,1=是")
    private String isComplete;

    private String delFlag;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getKitId() { return kitId; }
    public void setKitId(Long kitId) { this.kitId = kitId; }
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
    public BigDecimal getRequiredQty() { return requiredQty; }
    public void setRequiredQty(BigDecimal requiredQty) { this.requiredQty = requiredQty; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public void setAvailableQty(BigDecimal availableQty) { this.availableQty = availableQty; }
    public BigDecimal getShortageQty() { return shortageQty; }
    public void setShortageQty(BigDecimal shortageQty) { this.shortageQty = shortageQty; }
    public String getIsComplete() { return isComplete; }
    public void setIsComplete(String isComplete) { this.isComplete = isComplete; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
