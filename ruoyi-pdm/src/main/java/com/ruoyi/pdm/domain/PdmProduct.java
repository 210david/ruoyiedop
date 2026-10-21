package com.ruoyi.pdm.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产品档案对象 pdm_product
 *
 * @author ruoyi
 */
public class PdmProduct extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 产品ID */
    private Long productId;

    /** 产品编码（关联仓库成品/半成品物料编码，启用后不可修改） */
    @Excel(name = "产品编码")
    private String productCode;

    /** 物料ID（wms_material，唯一挂接） */
    private Long materialId;

    /** 产品名称（冗余物料名称） */
    @Excel(name = "产品名称")
    private String materialName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 计量单位 */
    @Excel(name = "计量单位")
    private String unit;

    /** 产品分类ID */
    private Long categoryId;

    /** 产品分类名称（冗余） */
    @Excel(name = "产品分类")
    private String categoryName;

    /** 产品状态（字典 pdm_product_status；已定型仅可由设计方案定稿流程触发） */
    @Excel(name = "产品状态", dictType = "pdm_product_status")
    private String productStatus;

    /** 责任设计员ID */
    private Long designerId;

    /** 责任设计员姓名 */
    @Excel(name = "责任设计员")
    private String designerName;

    /** 定型日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "定型日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date finalDate;

    /** 附件（多个逗号分隔） */
    private String attach;

    /** 部门ID（数据权限） */
    private Long deptId;

    /** 删除标志 */
    private String delFlag;

    /** 版本记录（档案聚合展示） */
    private List<PdmDocVersion> versionList;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }
    public Long getDesignerId() { return designerId; }
    public void setDesignerId(Long designerId) { this.designerId = designerId; }
    public String getDesignerName() { return designerName; }
    public void setDesignerName(String designerName) { this.designerName = designerName; }
    public Date getFinalDate() { return finalDate; }
    public void setFinalDate(Date finalDate) { this.finalDate = finalDate; }
    public String getAttach() { return attach; }
    public void setAttach(String attach) { this.attach = attach; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PdmDocVersion> getVersionList() { return versionList; }
    public void setVersionList(List<PdmDocVersion> versionList) { this.versionList = versionList; }
}
