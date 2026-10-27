package com.ruoyi.pdm.domain;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 设计BOM对象 pdm_design_bom
 *
 * @author ruoyi
 */
public class PdmDesignBom extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** BOM ID */
    private Long bomId;

    /** BOM编号（pdm_design_bom 规则自动生成） */
    @Excel(name = "BOM编号")
    private String bomNo;

    /** BOM名称 */
    @Excel(name = "BOM名称")
    private String bomName;

    /** 产品档案ID */
    private Long productId;

    /** 产品编码（冗余） */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称（冗余） */
    @Excel(name = "产品名称")
    private String productName;

    /** 产品单台用量 */
    @Excel(name = "单台用量")
    private BigDecimal baseQty;

    /** 产品单位 */
    @Excel(name = "产品单位")
    private String baseUnit;

    /** BOM状态（字典 pdm_bom_status） */
    @Excel(name = "BOM状态", dictType = "pdm_bom_status")
    private String bomStatus;

    /** 部门ID（数据权限） */
    private Long deptId;

    /** 删除标志 */
    private String delFlag;

    /** 明细数量（列表展示） */
    private Integer itemCount;

    /** 当前生效版本号（列表瞬态，用于下发按钮状态） */
    private String currentVersionNo;

    /** 当前已下发的EBOM版本号（列表瞬态，无则未下发） */
    private String releasedVersionNo;

    /** BOM明细 */
    private List<PdmDesignBomItem> itemList;

    /** 生效指定版本ID（瞬态） */
    private Long versionId;

    /** 是否大版本升版（瞬态） */
    private Boolean majorUpgrade;

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
    public BigDecimal getBaseQty() { return baseQty; }
    public void setBaseQty(BigDecimal baseQty) { this.baseQty = baseQty; }
    public String getBaseUnit() { return baseUnit; }
    public void setBaseUnit(String baseUnit) { this.baseUnit = baseUnit; }
    public String getBomStatus() { return bomStatus; }
    public void setBomStatus(String bomStatus) { this.bomStatus = bomStatus; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public String getCurrentVersionNo() { return currentVersionNo; }
    public void setCurrentVersionNo(String currentVersionNo) { this.currentVersionNo = currentVersionNo; }
    public String getReleasedVersionNo() { return releasedVersionNo; }
    public void setReleasedVersionNo(String releasedVersionNo) { this.releasedVersionNo = releasedVersionNo; }
    public List<PdmDesignBomItem> getItemList() { return itemList; }
    public void setItemList(List<PdmDesignBomItem> itemList) { this.itemList = itemList; }
    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Boolean getMajorUpgrade() { return majorUpgrade; }
    public void setMajorUpgrade(Boolean majorUpgrade) { this.majorUpgrade = majorUpgrade; }
}
