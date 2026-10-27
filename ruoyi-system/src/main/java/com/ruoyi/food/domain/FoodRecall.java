package com.ruoyi.food.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品批次召回单对象 food_recall
 *
 * 一键按谱系圈批冻结：从触发批次出发按批次DAG正/反向圈出全部关联批次并隔离
 *
 * @author ruoyi
 */
public class FoodRecall extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 召回ID */
    private Long recallId;

    /** 召回单号 */
    @Excel(name = "召回单号")
    private String recallCode;

    /** 触发批次ID */
    private Long triggerBatchId;

    /** 触发批次号 */
    @Excel(name = "触发批次号")
    private String triggerBatchCode;

    /** 召回范围（1正向 2反向 3双向） */
    @Excel(name = "召回范围", readConverterExp = "1=正向,2=反向,3=双向")
    private String recallScope;

    /** 召回原因 */
    @Excel(name = "召回原因")
    private String recallReason;

    /** 召回等级（1一般 2重要 3紧急） */
    @Excel(name = "召回等级", readConverterExp = "1=一般,2=重要,3=紧急")
    private String recallLevel;

    /** 圈批数量 */
    @Excel(name = "圈批数量")
    private Integer affectedCount;

    /** 状态（0待执行 1已执行 2已取消） */
    @Excel(name = "状态", readConverterExp = "0=待执行,1=已执行,2=已取消")
    private String status;

    /** 执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "执行时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date executeTime;

    /** 执行人 */
    @Excel(name = "执行人")
    private String executeBy;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 触发批次物料名称（关联展示） */
    @Excel(name = "触发物料")
    private String materialName;

    /** 圈批明细（圈批预览/执行结果） */
    private List<FoodRecallBatch> batchList;

    public Long getRecallId()
    {
        return recallId;
    }

    public void setRecallId(Long recallId)
    {
        this.recallId = recallId;
    }

    public String getRecallCode()
    {
        return recallCode;
    }

    public void setRecallCode(String recallCode)
    {
        this.recallCode = recallCode;
    }

    public Long getTriggerBatchId()
    {
        return triggerBatchId;
    }

    public void setTriggerBatchId(Long triggerBatchId)
    {
        this.triggerBatchId = triggerBatchId;
    }

    public String getTriggerBatchCode()
    {
        return triggerBatchCode;
    }

    public void setTriggerBatchCode(String triggerBatchCode)
    {
        this.triggerBatchCode = triggerBatchCode;
    }

    public String getRecallScope()
    {
        return recallScope;
    }

    public void setRecallScope(String recallScope)
    {
        this.recallScope = recallScope;
    }

    public String getRecallReason()
    {
        return recallReason;
    }

    public void setRecallReason(String recallReason)
    {
        this.recallReason = recallReason;
    }

    public String getRecallLevel()
    {
        return recallLevel;
    }

    public void setRecallLevel(String recallLevel)
    {
        this.recallLevel = recallLevel;
    }

    public Integer getAffectedCount()
    {
        return affectedCount;
    }

    public void setAffectedCount(Integer affectedCount)
    {
        this.affectedCount = affectedCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public List<FoodRecallBatch> getBatchList()
    {
        return batchList;
    }

    public void setBatchList(List<FoodRecallBatch> batchList)
    {
        this.batchList = batchList;
    }
}
