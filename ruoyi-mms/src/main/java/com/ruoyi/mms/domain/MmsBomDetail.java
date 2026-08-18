package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * BOM明细 mms_bom_detail
 *
 * @author ruoyi
 */
public class MmsBomDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** BOM ID */
    private Long bomId;

    /** 行序号 */
    @Excel(name = "序号")
    private Integer seq;

    /** 子项物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 单件用量 */
    @Excel(name = "单件用量")
    private BigDecimal usageQty;

    /** 损耗率(%) */
    @Excel(name = "损耗率(%)")
    private BigDecimal lossRate;

    /** 是否关键料(0否 1是) */
    @Excel(name = "是否关键料", readConverterExp = "0=否,1=是")
    private String isKeyMaterial;

    /** 供应方式(1直接领料 2倒冲 3车间库存) */
    @Excel(name = "供应方式", readConverterExp = "1=直接领料,2=倒冲,3=车间库存")
    private String supplyType;

    /** 默认发料仓库ID */
    private Long pickStoreId;

    /** 默认发料仓库名称 */
    @Excel(name = "发料仓库")
    private String pickStoreName;

    /** 是否虚拟件(0否 1是) */
    @Excel(name = "是否虚拟件", readConverterExp = "0=否,1=是")
    private String isPhantom;

    /** 引用BOM ID(子项为半成品时关联其BOM,支持多层级展开) */
    private Long bomRefId;

    /** 引用BOM编号(用于显示) */
    private String bomRefNo;

    /** 删除标志 */
    private String delFlag;

    /** 层级(多层级展开时使用, 0=顶层) */
    private transient Integer treeLevel;

    public Long getDetailId() { return detailId; }
    public void setDetailId(Long detailId) { this.detailId = detailId; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public Integer getSeq() { return seq; }
    public void setSeq(Integer seq) { this.seq = seq; }
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
    public String getIsKeyMaterial() { return isKeyMaterial; }
    public void setIsKeyMaterial(String isKeyMaterial) { this.isKeyMaterial = isKeyMaterial; }
    public String getSupplyType() { return supplyType; }
    public void setSupplyType(String supplyType) { this.supplyType = supplyType; }
    public Long getPickStoreId() { return pickStoreId; }
    public void setPickStoreId(Long pickStoreId) { this.pickStoreId = pickStoreId; }
    public String getPickStoreName() { return pickStoreName; }
    public void setPickStoreName(String pickStoreName) { this.pickStoreName = pickStoreName; }
    public String getIsPhantom() { return isPhantom; }
    public void setIsPhantom(String isPhantom) { this.isPhantom = isPhantom; }
    public Long getBomRefId() { return bomRefId; }
    public void setBomRefId(Long bomRefId) { this.bomRefId = bomRefId; }
    public String getBomRefNo() { return bomRefNo; }
    public void setBomRefNo(String bomRefNo) { this.bomRefNo = bomRefNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Integer getTreeLevel() { return treeLevel; }
    public void setTreeLevel(Integer treeLevel) { this.treeLevel = treeLevel; }
}
