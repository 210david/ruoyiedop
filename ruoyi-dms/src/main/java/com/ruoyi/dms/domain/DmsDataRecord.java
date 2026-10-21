package com.ruoyi.dms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/** 设备数据采集记录 dms_data_record */
public class DmsDataRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long equipmentId;
    @Excel(name = "设备编码") private String equipmentCode;
    @Excel(name = "设备名称") private String equipmentName;
    @Excel(name = "运行状态", dictType = "dms_run_status") private String runStatus;
    @Excel(name = "运行小时") private BigDecimal runHours;
    @Excel(name = "加工件数") private BigDecimal productCount;
    private BigDecimal param1Value;
    private BigDecimal param2Value;
    private BigDecimal param3Value;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "采集时间", width = 30) private Date collectTime;
    private String collectMode;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public String getRunStatus() { return runStatus; }
    public void setRunStatus(String runStatus) { this.runStatus = runStatus; }
    public BigDecimal getRunHours() { return runHours; }
    public void setRunHours(BigDecimal runHours) { this.runHours = runHours; }
    public BigDecimal getProductCount() { return productCount; }
    public void setProductCount(BigDecimal productCount) { this.productCount = productCount; }
    public BigDecimal getParam1Value() { return param1Value; }
    public void setParam1Value(BigDecimal param1Value) { this.param1Value = param1Value; }
    public BigDecimal getParam2Value() { return param2Value; }
    public void setParam2Value(BigDecimal param2Value) { this.param2Value = param2Value; }
    public BigDecimal getParam3Value() { return param3Value; }
    public void setParam3Value(BigDecimal param3Value) { this.param3Value = param3Value; }
    public Date getCollectTime() { return collectTime; }
    public void setCollectTime(Date collectTime) { this.collectTime = collectTime; }
    public String getCollectMode() { return collectMode; }
    public void setCollectMode(String collectMode) { this.collectMode = collectMode; }
}
