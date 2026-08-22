package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * BOM物料清单 mms_bom
 *
 * @author ruoyi
 */
public class MmsBom extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** BOM ID */
    private Long bomId;

    /** BOM编号 */
    @Excel(name = "BOM编号")
    private String bomNo;

    /** BOM名称 */
    @Excel(name = "BOM名称")
    private String bomName;

    /** 成品/主产品物料ID */
    private Long productId;

    /** 产品编码 */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;

    /** 清单类型(0标准BOM 1替代BOM 2配方 3临时BOM) */
    @Excel(name = "BOM类型", readConverterExp = "0=标准BOM,1=替代BOM,2=配方,3=临时BOM")
    private String bomType;

    /** 版本号 */
    @Excel(name = "版本号")
    private String version;

    /** 基准数量(生产多少个产品的用料) */
    @Excel(name = "基准数量")
    private BigDecimal baseQty;

    /** 基准单位 */
    @Excel(name = "基准单位")
    private String baseUnit;

    /** 状态(0草稿 1已发布 2停用) */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=已发布,2=停用")
    private String status;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "失效日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 删除标志 */
    private String delFlag;

    /** 产品物料类型（仅用于查询过滤，不持久化） */
    private transient String productMaterialType;

    /** BOM明细列表 */
    private List<MmsBomDetail> detailList;

    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public String getBomNo() { return bomNo; }
    public void setBomNo(String bomNo) { this.bomNo = bomNo; }
    public String getBomName() { return bomName; }
    public void setBomName(String bomName) { this.bomName = bomName; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProductMaterialType() { return productMaterialType; }
    public void setProductMaterialType(String productMaterialType) { this.productMaterialType = productMaterialType; }
    public List<MmsBomDetail> getDetailList() { return detailList; }
    public void setDetailList(List<MmsBomDetail> detailList) { this.detailList = detailList; }
}
