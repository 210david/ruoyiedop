package com.ruoyi.dms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/** 设备数据采集配置 dms_data_collection_config */
public class DmsDataCollectionConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long configId;
    private Long equipmentId;
    @Excel(name = "设备名称") private String equipmentName;
    @Excel(name = "采集模式", readConverterExp = "0=人工,1=自动") private String collectMode;
    private String collectStatus;
    private String collectHours;
    private String collectCount;
    private String param1Name; private String param1Unit; private BigDecimal param1AlarmMin; private BigDecimal param1AlarmMax;
    private String param2Name; private String param2Unit; private BigDecimal param2AlarmMin; private BigDecimal param2AlarmMax;
    private String param3Name; private String param3Unit; private BigDecimal param3AlarmMin; private BigDecimal param3AlarmMax;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用") private String status;
    private String delFlag;

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public String getCollectMode() { return collectMode; }
    public void setCollectMode(String collectMode) { this.collectMode = collectMode; }
    public String getCollectStatus() { return collectStatus; }
    public void setCollectStatus(String collectStatus) { this.collectStatus = collectStatus; }
    public String getCollectHours() { return collectHours; }
    public void setCollectHours(String collectHours) { this.collectHours = collectHours; }
    public String getCollectCount() { return collectCount; }
    public void setCollectCount(String collectCount) { this.collectCount = collectCount; }
    public String getParam1Name() { return param1Name; }
    public void setParam1Name(String param1Name) { this.param1Name = param1Name; }
    public String getParam1Unit() { return param1Unit; }
    public void setParam1Unit(String param1Unit) { this.param1Unit = param1Unit; }
    public BigDecimal getParam1AlarmMin() { return param1AlarmMin; }
    public void setParam1AlarmMin(BigDecimal param1AlarmMin) { this.param1AlarmMin = param1AlarmMin; }
    public BigDecimal getParam1AlarmMax() { return param1AlarmMax; }
    public void setParam1AlarmMax(BigDecimal param1AlarmMax) { this.param1AlarmMax = param1AlarmMax; }
    public String getParam2Name() { return param2Name; }
    public void setParam2Name(String param2Name) { this.param2Name = param2Name; }
    public String getParam2Unit() { return param2Unit; }
    public void setParam2Unit(String param2Unit) { this.param2Unit = param2Unit; }
    public BigDecimal getParam2AlarmMin() { return param2AlarmMin; }
    public void setParam2AlarmMin(BigDecimal param2AlarmMin) { this.param2AlarmMin = param2AlarmMin; }
    public BigDecimal getParam2AlarmMax() { return param2AlarmMax; }
    public void setParam2AlarmMax(BigDecimal param2AlarmMax) { this.param2AlarmMax = param2AlarmMax; }
    public String getParam3Name() { return param3Name; }
    public void setParam3Name(String param3Name) { this.param3Name = param3Name; }
    public String getParam3Unit() { return param3Unit; }
    public void setParam3Unit(String param3Unit) { this.param3Unit = param3Unit; }
    public BigDecimal getParam3AlarmMin() { return param3AlarmMin; }
    public void setParam3AlarmMin(BigDecimal param3AlarmMin) { this.param3AlarmMin = param3AlarmMin; }
    public BigDecimal getParam3AlarmMax() { return param3AlarmMax; }
    public void setParam3AlarmMax(BigDecimal param3AlarmMax) { this.param3AlarmMax = param3AlarmMax; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
