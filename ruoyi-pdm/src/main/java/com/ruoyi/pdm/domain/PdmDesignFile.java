package com.ruoyi.pdm.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 图纸技术文件对象 pdm_design_file
 *
 * @author ruoyi
 */
public class PdmDesignFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 资料ID */
    private Long fileId;

    /** 资料编号（pdm_design_file 规则自动生成） */
    @Excel(name = "资料编号")
    private String fileNo;

    /** 资料名称 */
    @Excel(name = "资料名称")
    private String fileName;

    /** 资料类别（字典 pdm_file_category） */
    @Excel(name = "资料类别", dictType = "pdm_file_category")
    private String fileCategory;

    /** 产品档案ID */
    private Long productId;

    /** 产品编码（冗余） */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称（冗余） */
    @Excel(name = "产品名称")
    private String productName;

    /** 关联设计方案ID（可选） */
    private Long schemeId;

    /** 关联设计方案编号（冗余） */
    private String schemeNo;

    /** 文件相对路径（最新版本） */
    private String fileUrl;

    /** 受控状态（字典 pdm_file_status：0=未生效,1=受控生效） */
    @Excel(name = "受控状态", dictType = "pdm_file_status")
    private String fileStatus;

    /** 部门ID（数据权限） */
    private Long deptId;

    /** 删除标志 */
    private String delFlag;

    /** 生效指定版本ID（瞬态） */
    private Long versionId;

    /** 是否大版本升版（瞬态） */
    private Boolean majorUpgrade;

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public String getFileNo() { return fileNo; }
    public void setFileNo(String fileNo) { this.fileNo = fileNo; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileCategory() { return fileCategory; }
    public void setFileCategory(String fileCategory) { this.fileCategory = fileCategory; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }
    public String getSchemeNo() { return schemeNo; }
    public void setSchemeNo(String schemeNo) { this.schemeNo = schemeNo; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getFileStatus() { return fileStatus; }
    public void setFileStatus(String fileStatus) { this.fileStatus = fileStatus; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Boolean getMajorUpgrade() { return majorUpgrade; }
    public void setMajorUpgrade(Boolean majorUpgrade) { this.majorUpgrade = majorUpgrade; }
}
