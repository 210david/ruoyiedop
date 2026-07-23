package com.ruoyi.dms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 设备状态变更日志 dms_equipment_log
 */
public class DmsEquipmentLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;

    private Long equipmentId;

    @Excel(name = "设备编号")
    private String equipmentCode;

    @Excel(name = "设备名称")
    private String equipmentName;

    @Excel(name = "变更类型", dictType = "dms_eq_change_type")
    private String changeType;

    private String oldValue;
    private String newValue;

    @Excel(name = "变更原因")
    private String changeReason;

    private String operatorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30)
    private Date createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public String getChangeType() { return changeType; }
    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    @Override
    public Date getCreateTime() { return createTime; }
    @Override
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
