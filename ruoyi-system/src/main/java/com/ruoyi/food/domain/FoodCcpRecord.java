package com.ruoyi.food.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * HACCP CCP监控记录表 food_ccp_record
 *
 * @author ruoyi
 */
public class FoodCcpRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 记录编号 */
    @Excel(name = "记录编号", width = 20)
    private String recordNo;

    /** 模板ID */
    private Long templateId;

    /** 控制点名称(快照) */
    @Excel(name = "控制点", width = 20)
    private String ccpName;

    /** 监控项类型(字典food_ccp_monitor_type) */
    private String monitorType;

    /** 监控项类型标签(非数据库字段) */
    @Excel(name = "监控项")
    private String monitorTypeLabel;

    /** 工艺路线ID */
    private Long routeId;

    /** 工序名称 */
    @Excel(name = "工序", width = 15)
    private String processName;

    /** 工单号 */
    @Excel(name = "工单号", width = 18)
    private String workOrderNo;

    /** 垛ID(food_wip) */
    private Long wipId;

    /** 垛号 */
    @Excel(name = "垛号", width = 18)
    private String wipNo;

    /** 批次ID(food_batch) */
    private Long batchId;

    /** 批次号 */
    @Excel(name = "批次号", width = 20)
    private String batchCode;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称", width = 20)
    private String materialName;

    /** 记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "记录时间", width = 22, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date recordTime;

    /** 温度(℃) */
    @Excel(name = "温度(℃)")
    private BigDecimal tempValue;

    /** 湿度(%) */
    @Excel(name = "湿度(%)")
    private BigDecimal humidityValue;

    /** 盐量比例(%) */
    @Excel(name = "盐量比例(%)")
    private BigDecimal saltRate;

    /** 上霉状态(字典food_mold_status) */
    private String moldStatus;

    /** 上霉状态标签(非数据库字段) */
    @Excel(name = "上霉状态")
    private String moldStatusLabel;

    /** 其他监控值 */
    @Excel(name = "其他监控值")
    private BigDecimal monitorValue;

    /** 关键限值快照 */
    @Excel(name = "关键限值", width = 15)
    private String limitSnapshot;

    /** 判定结果(0合格1超限) */
    private String judgeResult;

    /** 判定结果标签(非数据库字段) */
    @Excel(name = "判定结果", width = 10)
    private String judgeResultLabel;

    /** 偏差/超限说明 */
    @Excel(name = "偏差说明", width = 25)
    private String overNote;

    /** 纠正措施 */
    @Excel(name = "纠正措施", width = 25)
    private String corrective;

    /** 联动NCR ID(qms_ncr) */
    private Long ncrId;

    /** 联动NCR编号 */
    @Excel(name = "联动NCR", width = 18)
    private String ncrNo;

    /** 记录人 */
    @Excel(name = "记录人", width = 10)
    private String recorder;

    /** 删除标志 */
    private String delFlag;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getCcpName() { return ccpName; }
    public void setCcpName(String ccpName) { this.ccpName = ccpName; }
    public String getMonitorType() { return monitorType; }
    public void setMonitorType(String monitorType) { this.monitorType = monitorType; }
    public String getMonitorTypeLabel() { return monitorTypeLabel; }
    public void setMonitorTypeLabel(String monitorTypeLabel) { this.monitorTypeLabel = monitorTypeLabel; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getWipId() { return wipId; }
    public void setWipId(Long wipId) { this.wipId = wipId; }
    public String getWipNo() { return wipNo; }
    public void setWipNo(String wipNo) { this.wipNo = wipNo; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public Date getRecordTime() { return recordTime; }
    public void setRecordTime(Date recordTime) { this.recordTime = recordTime; }
    public BigDecimal getTempValue() { return tempValue; }
    public void setTempValue(BigDecimal tempValue) { this.tempValue = tempValue; }
    public BigDecimal getHumidityValue() { return humidityValue; }
    public void setHumidityValue(BigDecimal humidityValue) { this.humidityValue = humidityValue; }
    public BigDecimal getSaltRate() { return saltRate; }
    public void setSaltRate(BigDecimal saltRate) { this.saltRate = saltRate; }
    public String getMoldStatus() { return moldStatus; }
    public void setMoldStatus(String moldStatus) { this.moldStatus = moldStatus; }
    public String getMoldStatusLabel() { return moldStatusLabel; }
    public void setMoldStatusLabel(String moldStatusLabel) { this.moldStatusLabel = moldStatusLabel; }
    public BigDecimal getMonitorValue() { return monitorValue; }
    public void setMonitorValue(BigDecimal monitorValue) { this.monitorValue = monitorValue; }
    public String getLimitSnapshot() { return limitSnapshot; }
    public void setLimitSnapshot(String limitSnapshot) { this.limitSnapshot = limitSnapshot; }
    public String getJudgeResult() { return judgeResult; }
    public void setJudgeResult(String judgeResult) { this.judgeResult = judgeResult; }
    public String getJudgeResultLabel() { return judgeResultLabel; }
    public void setJudgeResultLabel(String judgeResultLabel) { this.judgeResultLabel = judgeResultLabel; }
    public String getOverNote() { return overNote; }
    public void setOverNote(String overNote) { this.overNote = overNote; }
    public String getCorrective() { return corrective; }
    public void setCorrective(String corrective) { this.corrective = corrective; }
    public Long getNcrId() { return ncrId; }
    public void setNcrId(Long ncrId) { this.ncrId = ncrId; }
    public String getNcrNo() { return ncrNo; }
    public void setNcrNo(String ncrNo) { this.ncrNo = ncrNo; }
    public String getRecorder() { return recorder; }
    public void setRecorder(String recorder) { this.recorder = recorder; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
