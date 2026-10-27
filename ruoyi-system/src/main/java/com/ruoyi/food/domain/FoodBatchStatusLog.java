package com.ruoyi.food.domain;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 食品批次状态日志对象 food_batch_status_log（只增不删）
 *
 * @author ruoyi
 */
public class FoodBatchStatusLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 批次ID */
    private Long batchId;

    /** 批次号 */
    private String batchCode;

    /** 迁移前状态 */
    private String fromStatus;

    /** 迁移后状态 */
    private String toStatus;

    /** 操作类型 */
    private String operateType;

    /** 原因 */
    private String reason;

    /** 关联单据类型 */
    private String refBillType;

    /** 关联单据ID */
    private Long refBillId;

    /** 操作人 */
    private String operateBy;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
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

    public String getFromStatus()
    {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus)
    {
        this.fromStatus = fromStatus;
    }

    public String getToStatus()
    {
        return toStatus;
    }

    public void setToStatus(String toStatus)
    {
        this.toStatus = toStatus;
    }

    public String getOperateType()
    {
        return operateType;
    }

    public void setOperateType(String operateType)
    {
        this.operateType = operateType;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
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

    public String getOperateBy()
    {
        return operateBy;
    }

    public void setOperateBy(String operateBy)
    {
        this.operateBy = operateBy;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }
}
