package com.ruoyi.food.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品批次预留对象 food_batch_reserve
 *
 * @author ruoyi
 */
public class FoodBatchReserve extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预留ID */
    private Long reserveId;

    /** 物料ID */
    private Long materialId;

    /** 批次ID */
    private Long batchId;

    /** 客户名称 */
    private String customerName;

    /** 单据类型 */
    private String orderBillType;

    /** 单据ID */
    private Long orderBillId;

    /** 单据号 */
    private String orderBillCode;

    /** 预留数量 */
    private BigDecimal qty;

    /** 预留状态（0预留中 1已核销 2已释放） */
    private String status;

    /** 释放时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    /** 删除标志 */
    private String delFlag;

    /** 批次号（关联查询） */
    private String batchCode;

    public Long getReserveId()
    {
        return reserveId;
    }

    public void setReserveId(Long reserveId)
    {
        this.reserveId = reserveId;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getOrderBillType()
    {
        return orderBillType;
    }

    public void setOrderBillType(String orderBillType)
    {
        this.orderBillType = orderBillType;
    }

    public Long getOrderBillId()
    {
        return orderBillId;
    }

    public void setOrderBillId(Long orderBillId)
    {
        this.orderBillId = orderBillId;
    }

    public String getOrderBillCode()
    {
        return orderBillCode;
    }

    public void setOrderBillCode(String orderBillCode)
    {
        this.orderBillCode = orderBillCode;
    }

    public BigDecimal getQty()
    {
        return qty;
    }

    public void setQty(BigDecimal qty)
    {
        this.qty = qty;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getReleaseTime()
    {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime)
    {
        this.releaseTime = releaseTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getBatchCode()
    {
        return batchCode;
    }

    public void setBatchCode(String batchCode)
    {
        this.batchCode = batchCode;
    }
}
