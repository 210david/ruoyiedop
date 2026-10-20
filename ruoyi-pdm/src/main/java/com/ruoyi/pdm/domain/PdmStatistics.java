package com.ruoyi.pdm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产品设计统计分析 VO
 *
 * @author ruoyi
 */
public class PdmStatistics extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 产品ID */
    private Long productId;

    /** 产品编码 */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称 */
    @Excel(name = "产品名称")
    private String productName;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    @Excel(name = "产品分类")
    private String categoryName;

    /** 产品状态 */
    private String productStatus;

    /** 定型日期 */
    @Excel(name = "定型日期")
    private String finalDate;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 设计方案数 */
    @Excel(name = "设计方案数")
    private Long schemeCount;

    /** 图纸文件数 */
    @Excel(name = "图纸文件数")
    private Long fileCount;

    /** 设计BOM数 */
    @Excel(name = "设计BOM数")
    private Long bomCount;

    /** 变更单数 */
    @Excel(name = "变更单数")
    private Long changeCount;

    /** 最近变更时间 */
    @Excel(name = "最近变更时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastChangeTime;

    /** 汇总-产品总数 */
    private Long productCount;

    /** 汇总-BOM下发次数 */
    private Long releaseCount;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }

    public String getFinalDate() { return finalDate; }
    public void setFinalDate(String finalDate) { this.finalDate = finalDate; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Long getSchemeCount() { return schemeCount; }
    public void setSchemeCount(Long schemeCount) { this.schemeCount = schemeCount; }

    public Long getFileCount() { return fileCount; }
    public void setFileCount(Long fileCount) { this.fileCount = fileCount; }

    public Long getBomCount() { return bomCount; }
    public void setBomCount(Long bomCount) { this.bomCount = bomCount; }

    public Long getChangeCount() { return changeCount; }
    public void setChangeCount(Long changeCount) { this.changeCount = changeCount; }

    public Date getLastChangeTime() { return lastChangeTime; }
    public void setLastChangeTime(Date lastChangeTime) { this.lastChangeTime = lastChangeTime; }

    public Long getProductCount() { return productCount; }
    public void setProductCount(Long productCount) { this.productCount = productCount; }

    public Long getReleaseCount() { return releaseCount; }
    public void setReleaseCount(Long releaseCount) { this.releaseCount = releaseCount; }
}
