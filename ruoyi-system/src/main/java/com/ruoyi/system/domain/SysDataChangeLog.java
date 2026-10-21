package com.ruoyi.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 数据变更日志表 sys_data_change_log
 * 
 * @author ruoyi
 */
public class SysDataChangeLog
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long id;

    /** 实体名称 */
    private String entityName;

    /** 实体ID */
    private String entityId;

    /** 字段名称 */
    private String fieldName;

    /** 旧值 */
    private String oldValue;

    /** 新值 */
    private String newValue;

    /** 变更类型 INSERT/UPDATE/DELETE */
    private String changeType;

    /** 操作人 */
    private String operateBy;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    /** 备注 */
    private String remark;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getEntityName()
    {
        return entityName;
    }

    public void setEntityName(String entityName)
    {
        this.entityName = entityName;
    }

    public String getEntityId()
    {
        return entityId;
    }

    public void setEntityId(String entityId)
    {
        this.entityId = entityId;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getOldValue()
    {
        return oldValue;
    }

    public void setOldValue(String oldValue)
    {
        this.oldValue = oldValue;
    }

    public String getNewValue()
    {
        return newValue;
    }

    public void setNewValue(String newValue)
    {
        this.newValue = newValue;
    }

    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
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

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
