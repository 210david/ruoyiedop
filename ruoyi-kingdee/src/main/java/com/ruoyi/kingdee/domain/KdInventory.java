package com.ruoyi.kingdee.domain;

import java.math.BigDecimal;
import java.util.Date;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 金蝶库存查询（一行 = 一个商品在一个仓库的库存）
 */
public class KdInventory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private String materialId;
    private String materialNumber;
    private String materialName;
    private String specModel;
    private String stockId;
    private String stockName;
    private String unitName;
    private BigDecimal qty;
    private BigDecimal validQty;
    private BigDecimal availableOutQty;
    private String categoryId;
    private String categoryName;
    /** 商品类别排序（金蝶商品类别树的展示顺序） */
    private Integer categorySort;
    private String imageUrl;
    private Date syncTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaterialId() { return materialId; }
    public void setMaterialId(String materialId) { this.materialId = materialId; }
    public String getMaterialNumber() { return materialNumber; }
    public void setMaterialNumber(String materialNumber) { this.materialNumber = materialNumber; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getStockId() { return stockId; }
    public void setStockId(String stockId) { this.stockId = stockId; }
    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public BigDecimal getValidQty() { return validQty; }
    public void setValidQty(BigDecimal validQty) { this.validQty = validQty; }
    public BigDecimal getAvailableOutQty() { return availableOutQty; }
    public void setAvailableOutQty(BigDecimal availableOutQty) { this.availableOutQty = availableOutQty; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Integer getCategorySort() { return categorySort; }
    public void setCategorySort(Integer categorySort) { this.categorySort = categorySort; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Date getSyncTime() { return syncTime; }
    public void setSyncTime(Date syncTime) { this.syncTime = syncTime; }
}
