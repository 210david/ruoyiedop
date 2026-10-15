package com.ruoyi.food.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 食品召回圈批明细对象 food_recall_batch
 *
 * @author ruoyi
 */
public class FoodRecallBatch implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long rbId;

    /** 召回ID */
    private Long recallId;

    /** 批次ID */
    private Long batchId;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchCode;

    /** 物料ID */
    private Long materialId;

    /** 物料名称（关联展示） */
    @Excel(name = "物料")
    private String materialName;

    /** 谱系深度（0=触发批次本身） */
    @Excel(name = "谱系深度")
    private Integer depth;

    /** 关系路径说明 */
    @Excel(name = "关系路径")
    private String relationPath;

    /** 冻结前批次状态 */
    @Excel(name = "冻结前状态", readConverterExp = "0=待检,1=合格,2=冻结,3=报废")
    private String prevStatus;

    /** 是否本次冻结（1是 0执行前已是冻结） */
    @Excel(name = "是否本次冻结", readConverterExp = "1=是,0=否")
    private String frozenFlag;

    /** 批次剩余数量（关联展示） */
    private java.math.BigDecimal qty;

    /** 到期日期（关联展示） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    /** 当前批次状态（关联展示） */
    private String batchStatus;

    /** 创建时间 */
    private Date createTime;

    public Long getRbId()
    {
        return rbId;
    }

    public void setRbId(Long rbId)
    {
        this.rbId = rbId;
    }

    public Long getRecallId()
    {
        return recallId;
    }

    public void setRecallId(Long recallId)
    {
        this.recallId = recallId;
    }

    public Long getBatchId()
    {
        return batchId;
    }

    public void setBatchId(Long batchId)
    {
        this.batchId = batchId;
    }

    public String getBatchCode()
    {
        return batchCode;
    }

    public void setBatchCode(String batchCode)
    {
        this.batchCode = batchCode;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public Integer getDepth()
    {
        return depth;
    }

    public void setDepth(Integer depth)
    {
        this.depth = depth;
    }

    public String getRelationPath()
    {
        return relationPath;
    }

    public void setRelationPath(String relationPath)
    {
        this.relationPath = relationPath;
    }

    public String getPrevStatus()
    {
        return prevStatus;
    }

    public void setPrevStatus(String prevStatus)
    {
        this.prevStatus = prevStatus;
    }

    public String getFrozenFlag()
    {
        return frozenFlag;
    }

    public void setFrozenFlag(String frozenFlag)
    {
        this.frozenFlag = frozenFlag;
    }

    public java.math.BigDecimal getQty()
    {
        return qty;
    }

    public void setQty(java.math.BigDecimal qty)
    {
        this.qty = qty;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
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

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
