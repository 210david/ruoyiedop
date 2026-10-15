package com.ruoyi.food.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品批次调整主单对象 food_batch_adjust
 *
 * @author ruoyi
 */
public class FoodBatchAdjust extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 调整单ID */
    private Long adjustId;

    /** 调整单号 */
    private String adjustCode;

    /** 调整类型（1冻结 2解冻 3报废 4拆批 5合批 6召回 7效期调整） */
    private String adjustType;

    /** 主批次ID */
    private Long sourceBatchId;

    /** 主批次号 */
    private String sourceBatchCode;

    /** 物料ID */
    private Long materialId;

    /** 单据状态（0待执行 1已执行 2已取消） */
    private String status;

    /** 调整原因 */
    private String reason;

    /** 执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date executeTime;

    /** 执行人 */
    private String executeBy;

    /** 删除标志 */
    private String delFlag;

    /** 调整明细（非表字段） */
    private List<FoodBatchAdjustItem> itemList;

    public Long getAdjustId()
    {
        return adjustId;
    }

    public void setAdjustId(Long adjustId)
    {
        this.adjustId = adjustId;
    }

    public String getAdjustCode()
    {
        return adjustCode;
    }

    public void setAdjustCode(String adjustCode)
    {
        this.adjustCode = adjustCode;
    }

    public String getAdjustType()
    {
        return adjustType;
    }

    public void setAdjustType(String adjustType)
    {
        this.adjustType = adjustType;
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

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public Date getExecuteTime()
    {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime)
    {
        this.executeTime = executeTime;
    }

    public String getExecuteBy()
    {
        return executeBy;
    }

    public void setExecuteBy(String executeBy)
    {
        this.executeBy = executeBy;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<FoodBatchAdjustItem> getItemList()
    {
        return itemList;
    }

    public void setItemList(List<FoodBatchAdjustItem> itemList)
    {
        this.itemList = itemList;
    }
}
