package com.ruoyi.qms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 检验任务表 qms_insp_task
 *
 * @author ruoyi
 */
public class QmsInspTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务编号 */
    @Excel(name = "任务编号")
    private String taskNo;

    /** 检验类型（IQC/IPQC/FQC/OQC） */
    @Excel(name = "检验类型", readConverterExp = "IQC=来料检验,IPQC=过程检验,FQC=成品检验,OQC=出货检验")
    private String taskType;

    /** 来源类型 */
    @Excel(name = "来源类型")
    private String sourceType;

    /** 来源单号 */
    @Excel(name = "来源单号")
    private String sourceNo;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    @Excel(name = "供应商")
    private String supplierName;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 送检数量 */
    @Excel(name = "送检数量")
    private BigDecimal inspectQty;

    /** AQL等级 */
    @Excel(name = "AQL等级")
    private String aqlLevel;

    /** 检验严格度（1正常 2加严 3放宽） */
    private String inspectLevel;

    /** 样本量(n) */
    @Excel(name = "样本量")
    private Integer sampleSize;

    /** 接收数(Ac) */
    @Excel(name = "Ac")
    private Integer acVal;

    /** 拒收数(Re) */
    @Excel(name = "Re")
    private Integer reVal;

    /** 任务状态（0待检 1检验中 2已判定 3已作废） */
    @Excel(name = "任务状态", readConverterExp = "0=待检,1=检验中,2=已判定,3=已作废")
    private String taskStatus;

    /** 检验结果（1合格 2不合格） */
    @Excel(name = "检验结果", readConverterExp = "1=合格,2=不合格")
    private String inspectResult;

    /** 最高缺陷等级 */
    @Excel(name = "缺陷等级")
    private String defectLevel;

    /** 检验员ID */
    private Long inspectorId;

    /** 检验员名称 */
    @Excel(name = "检验员")
    private String inspectorName;

    /** 检验时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "检验时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date inspectTime;

    /** 判定时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date judgeTime;

    /** 使用量检具ID */
    private Long gaugeId;

    /** 量检具名称 */
    @Excel(name = "量检具")
    private String gaugeName;

    /** 是否复检 */
    @Excel(name = "复检", readConverterExp = "0=否,1=是")
    private String isRecheck;

    /** 原任务ID */
    private Long originTaskId;

    /** 状态（0正常 1停用） */
    private String status;

    /** 删除标志 */
    private String delFlag;

    /** 检验明细列表（非数据库字段） */
    private List<QmsInspItem> itemList;

    /** 电子签名记录列表（非数据库字段） */
    private List<QmsEsigRecord> esigList;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getInspectQty() { return inspectQty; }
    public void setInspectQty(BigDecimal inspectQty) { this.inspectQty = inspectQty; }
    public String getAqlLevel() { return aqlLevel; }
    public void setAqlLevel(String aqlLevel) { this.aqlLevel = aqlLevel; }
    public String getInspectLevel() { return inspectLevel; }
    public void setInspectLevel(String inspectLevel) { this.inspectLevel = inspectLevel; }
    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }
    public Integer getAcVal() { return acVal; }
    public void setAcVal(Integer acVal) { this.acVal = acVal; }
    public Integer getReVal() { return reVal; }
    public void setReVal(Integer reVal) { this.reVal = reVal; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public String getInspectResult() { return inspectResult; }
    public void setInspectResult(String inspectResult) { this.inspectResult = inspectResult; }
    public String getDefectLevel() { return defectLevel; }
    public void setDefectLevel(String defectLevel) { this.defectLevel = defectLevel; }
    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }
    public String getInspectorName() { return inspectorName; }
    public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
    public Date getInspectTime() { return inspectTime; }
    public void setInspectTime(Date inspectTime) { this.inspectTime = inspectTime; }
    public Date getJudgeTime() { return judgeTime; }
    public void setJudgeTime(Date judgeTime) { this.judgeTime = judgeTime; }
    public Long getGaugeId() { return gaugeId; }
    public void setGaugeId(Long gaugeId) { this.gaugeId = gaugeId; }
    public String getGaugeName() { return gaugeName; }
    public void setGaugeName(String gaugeName) { this.gaugeName = gaugeName; }
    public String getIsRecheck() { return isRecheck; }
    public void setIsRecheck(String isRecheck) { this.isRecheck = isRecheck; }
    public Long getOriginTaskId() { return originTaskId; }
    public void setOriginTaskId(Long originTaskId) { this.originTaskId = originTaskId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<QmsInspItem> getItemList() { return itemList; }
    public void setItemList(List<QmsInspItem> itemList) { this.itemList = itemList; }
    public List<QmsEsigRecord> getEsigList() { return esigList; }
    public void setEsigList(List<QmsEsigRecord> esigList) { this.esigList = esigList; }
}
