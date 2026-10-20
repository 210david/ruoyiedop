package com.ruoyi.pdm.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 设计方案对象 pdm_scheme
 *
 * @author ruoyi
 */
public class PdmScheme extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 方案ID */
    private Long schemeId;

    /** 方案编号（pdm_scheme 规则自动生成） */
    @Excel(name = "方案编号")
    private String schemeNo;

    /** 方案名称 */
    @Excel(name = "方案名称")
    private String schemeName;

    /** 产品档案ID */
    private Long productId;

    /** 产品编码（冗余） */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称（冗余） */
    @Excel(name = "产品名称")
    private String productName;

    /** 设计目标 */
    private String designGoal;

    /** 主要参数 */
    private String mainParams;

    /** 方案正文附件（Word/PDF，最新版本文件） */
    private String fileUrl;

    /** 方案状态（字典 pdm_scheme_status） */
    @Excel(name = "方案状态", dictType = "pdm_scheme_status")
    private String schemeStatus;

    /** 定稿时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "定稿时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date finalTime;

    /** 定稿人 */
    @Excel(name = "定稿人")
    private String finalBy;

    /** 部门ID（数据权限） */
    private Long deptId;

    /** 删除标志 */
    private String delFlag;

    /** 配方配比明细（涉密，按权限返回） */
    private List<PdmSchemeFormula> formulaList;

    /** 定稿/生效指定版本ID（瞬态） */
    private Long versionId;

    /** 是否大版本升版（瞬态） */
    private Boolean majorUpgrade;

    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }
    public String getSchemeNo() { return schemeNo; }
    public void setSchemeNo(String schemeNo) { this.schemeNo = schemeNo; }
    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDesignGoal() { return designGoal; }
    public void setDesignGoal(String designGoal) { this.designGoal = designGoal; }
    public String getMainParams() { return mainParams; }
    public void setMainParams(String mainParams) { this.mainParams = mainParams; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getSchemeStatus() { return schemeStatus; }
    public void setSchemeStatus(String schemeStatus) { this.schemeStatus = schemeStatus; }
    public Date getFinalTime() { return finalTime; }
    public void setFinalTime(Date finalTime) { this.finalTime = finalTime; }
    public String getFinalBy() { return finalBy; }
    public void setFinalBy(String finalBy) { this.finalBy = finalBy; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PdmSchemeFormula> getFormulaList() { return formulaList; }
    public void setFormulaList(List<PdmSchemeFormula> formulaList) { this.formulaList = formulaList; }
    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Boolean getMajorUpgrade() { return majorUpgrade; }
    public void setMajorUpgrade(Boolean majorUpgrade) { this.majorUpgrade = majorUpgrade; }
}
