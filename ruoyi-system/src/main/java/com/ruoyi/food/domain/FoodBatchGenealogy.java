package com.ruoyi.food.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品批次谱系对象 food_batch_genealogy
 *
 * @author ruoyi
 */
public class FoodBatchGenealogy extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 谱系ID */
    private Long genealogyId;

    /** 父批次ID */
    private Long parentBatchId;

    /** 子批次ID */
    private Long childBatchId;

    /** 关系类型（1投入 2派生 3拆分 4合并 5退回） */
    private String relationType;

    /** 流转数量 */
    private BigDecimal qty;

    /** 数量单位 */
    private String qtyUnit;

    /** 关联单据类型 */
    private String refBillType;

    /** 关联单据ID */
    private Long refBillId;

    /** 关联单据号 */
    private String refBillCode;

    /** 父批次号（关联查询） */
    private String parentBatchCode;

    /** 子批次号（关联查询） */
    private String childBatchCode;

    /** 谱系方向（非表字段：up向上追溯 down向下追踪） */
    private String direction;

    public Long getGenealogyId()
    {
        return genealogyId;
    }

    public void setGenealogyId(Long genealogyId)
    {
        this.genealogyId = genealogyId;
    }

    public Long getParentBatchId()
    {
        return parentBatchId;
    }

    public void setParentBatchId(Long parentBatchId)
    {
        this.parentBatchId = parentBatchId;
    }

    public Long getChildBatchId()
    {
        return childBatchId;
    }

    public void setChildBatchId(Long childBatchId)
    {
        this.childBatchId = childBatchId;
    }

    public String getRelationType()
    {
        return relationType;
    }

    public void setRelationType(String relationType)
    {
        this.relationType = relationType;
    }

    public BigDecimal getQty()
    {
        return qty;
    }

    public void setQty(BigDecimal qty)
    {
        this.qty = qty;
    }

    public String getQtyUnit()
    {
        return qtyUnit;
    }

    public void setQtyUnit(String qtyUnit)
    {
        this.qtyUnit = qtyUnit;
    }

    public String getRefBillType()
    {
        return refBillType;
    }

    public void setRefBillType(String refBillType)
    {
        this.refBillType = refBillType;
    }

    public Long getRefBillId()
    {
        return refBillId;
    }

    public void setRefBillId(Long refBillId)
    {
        this.refBillId = refBillId;
    }

    public String getRefBillCode()
    {
        return refBillCode;
    }

    public void setRefBillCode(String refBillCode)
    {
        this.refBillCode = refBillCode;
    }

    public String getParentBatchCode()
    {
        return parentBatchCode;
    }

    public void setParentBatchCode(String parentBatchCode)
    {
        this.parentBatchCode = parentBatchCode;
    }

    public String getChildBatchCode()
    {
        return childBatchCode;
    }

    public void setChildBatchCode(String childBatchCode)
    {
        this.childBatchCode = childBatchCode;
    }

    public String getDirection()
    {
        return direction;
    }

    public void setDirection(String direction)
    {
        this.direction = direction;
    }
}
