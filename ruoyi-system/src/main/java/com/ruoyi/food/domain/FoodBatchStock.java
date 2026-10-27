package com.ruoyi.food.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品批次库存对象 food_batch_stock
 *
 * @author ruoyi
 */
public class FoodBatchStock extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long bsId;

    /** 物料ID */
    private Long materialId;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次ID */
    private Long batchId;

    /** 批次库存量 */
    private BigDecimal qty;

    /** 锁定/预留量 */
    private BigDecimal qtyLocked;

    /** 乐观锁版本 */
    private Integer version;

    /** 批次号（关联查询） */
    private String batchCode;

    /** 仓库名称（关联查询） */
    private String warehouseName;

    /** 库位名称（关联查询） */
    private String locationName;

    /** 物料编码（关联查询） */
    private String materialCode;

    /** 到期日期（FEFO关联查询） */
    private java.util.Date expireDate;

    /** 批次状态（FEFO关联查询） */
    private String batchStatus;

    /** 效期状态（FEFO关联查询） */
    private String expireStatus;

    /** 剩余天数（FEFO计算字段） */
    private Long daysRemain;

    public Long getBsId()
    {
        return bsId;
    }

    public void setBsId(Long bsId)
    {
        this.bsId = bsId;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public Long getLocationId()
    {
        return locationId;
    }

    public void setLocationId(Long locationId)
    {
        this.locationId = locationId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public BigDecimal getQty()
    {
        return qty;
    }

    public void setQty(BigDecimal qty)
    {
        this.qty = qty;
    }

    public BigDecimal getQtyLocked()
    {
        return qtyLocked;
    }

    public void setQtyLocked(BigDecimal qtyLocked)
    {
        this.qtyLocked = qtyLocked;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getBatchCode()
    {
        return batchCode;
    }

    public void setBatchCode(String batchCode)
    {
        this.batchCode = batchCode;
    }

    public String getWarehouseName()
    {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName)
    {
        this.warehouseName = warehouseName;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public String getMaterialCode()
    {
        return materialCode;
    }

    public void setMaterialCode(String materialCode)
    {
        this.materialCode = materialCode;
    }

    public java.util.Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(java.util.Date expireDate)
    {
        this.expireDate = expireDate;
    }

    public String getBatchStatus()
    {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus)
    {
        this.batchStatus = batchStatus;
    }

    public String getExpireStatus()
    {
        return expireStatus;
    }

    public void setExpireStatus(String expireStatus)
    {
        this.expireStatus = expireStatus;
    }

    public Long getDaysRemain()
    {
        return daysRemain;
    }

    public void setDaysRemain(Long daysRemain)
    {
        this.daysRemain = daysRemain;
    }
}
