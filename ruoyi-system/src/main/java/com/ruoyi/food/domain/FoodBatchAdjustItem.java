package com.ruoyi.food.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 食品批次调整明细对象 food_batch_adjust_item
 *
 * @author ruoyi
 */
public class FoodBatchAdjustItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 调整单ID */
    private Long adjustId;

    /** 源批次ID */
    private Long sourceBatchId;

    /** 源批次号 */
    private String sourceBatchCode;

    /** 目标批次ID（拆批/合批生成后回填） */
    private Long targetBatchId;

    /** 目标批次号 */
    private String targetBatchCode;

    /** 调整数量 */
    private BigDecimal qty;

    /** 数量单位 */
    private String qtyUnit;

    /** 效期处理（1沿用源批次 2重新计算 3顺延） */
    private String expireAction;

    /** 新到期日期（效期调整时填写） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date newExpireDate;

    /** 备注 */
    private String remark;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getAdjustId()
    {
        return adjustId;
    }

    public void setAdjustId(Long adjustId)
    {
        this.adjustId = adjustId;
    }

    public Long getSourceBatchId()
    {
        return sourceBatchId;
    }

    public void setSourceBatchId(Long sourceBatchId)
    {
        this.sourceBatchId = sourceBatchId;
    }

    public String getSourceBatchCode()
    {
        return sourceBatchCode;
    }

    public void setSourceBatchCode(String sourceBatchCode)
    {
        this.sourceBatchCode = sourceBatchCode;
    }

    public Long getTargetBatchId()
    {
        return targetBatchId;
    }

    public void setTargetBatchId(Long targetBatchId)
    {
        this.targetBatchId = targetBatchId;
    }

    public String getTargetBatchCode()
    {
        return targetBatchCode;
    }

    public void setTargetBatchCode(String targetBatchCode)
    {
        this.targetBatchCode = targetBatchCode;
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

    public String getExpireAction()
    {
        return expireAction;
    }

    public void setExpireAction(String expireAction)
    {
        this.expireAction = expireAction;
    }

    public Date getNewExpireDate()
    {
        return newExpireDate;
    }

    public void setNewExpireDate(Date newExpireDate)
    {
        this.newExpireDate = newExpireDate;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
