package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;

/**
 * BOM导入DTO（用于Excel导入）
 * Excel每行包含BOM主表字段和BOM明细字段。
 * 同一BOM的多行明细通过"产品编码+版本号"分组：
 * - 每行的BOM名称/产品编码/产品名称/BOM类型/版本号/基准数量/基准单位可以重复填写
 * - 系统按产品编码+版本号分组，第一行作为BOM主表，其余行作为明细
 *
 * @author ruoyi
 */
public class MmsBomImport
{
    @Excel(name = "BOM名称")
    private String bomName;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "BOM类型", readConverterExp = "0=标准BOM,1=替代BOM,2=配方,3=临时BOM")
    private String bomType;

    @Excel(name = "版本号")
    private String version;

    @Excel(name = "基准数量")
    private BigDecimal baseQty;

    @Excel(name = "基准单位")
    private String baseUnit;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    @Excel(name = "单件用量")
    private BigDecimal usageQty;

    @Excel(name = "损耗率(%)")
    private BigDecimal lossRate;

    @Excel(name = "是否关键料", readConverterExp = "0=否,1=是")
    private String isKeyMaterial;

    @Excel(name = "供应方式", readConverterExp = "1=直接领料,2=倒冲,3=车间库存")
    private String supplyType;

    @Excel(name = "是否虚拟件", readConverterExp = "0=否,1=是")
    private String isPhantom;

    @Excel(name = "备注")
    private String remark;

    public String getBomName() { return bomName; }
    public void setBomName(String bomName) { this.bomName = bomName; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getBomType() { return bomType; }
    public void setBomType(String bomType) { this.bomType = bomType; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public BigDecimal getBaseQty() { return baseQty; }
    public void setBaseQty(BigDecimal baseQty) { this.baseQty = baseQty; }
    public String getBaseUnit() { return baseUnit; }
    public void setBaseUnit(String baseUnit) { this.baseUnit = baseUnit; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public BigDecimal getUsageQty() { return usageQty; }
    public void setUsageQty(BigDecimal usageQty) { this.usageQty = usageQty; }
    public BigDecimal getLossRate() { return lossRate; }
    public void setLossRate(BigDecimal lossRate) { this.lossRate = lossRate; }
    public String getIsKeyMaterial() { return isKeyMaterial; }
    public void setIsKeyMaterial(String isKeyMaterial) { this.isKeyMaterial = isKeyMaterial; }
    public String getSupplyType() { return supplyType; }
    public void setSupplyType(String supplyType) { this.supplyType = supplyType; }
    public String getIsPhantom() { return isPhantom; }
    public void setIsPhantom(String isPhantom) { this.isPhantom = isPhantom; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
