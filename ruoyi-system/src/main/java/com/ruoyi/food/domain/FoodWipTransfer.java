package com.ruoyi.food.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品工序转移单 food_wip_transfer
 *
 * 工序间流转单据：转移留痕、损耗记录（修割损耗/发酵失重等分环节）、批次随转
 *
 * @author ruoyi
 */
public class FoodWipTransfer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long transferId;

    @Excel(name = "转移单号")
    private String transferNo;

    private Long workOrderId;

    @Excel(name = "工单号")
    private String workOrderNo;

    private Long wipId;

    @Excel(name = "垛号")
    private String wipNo;

    private Long batchId;

    @Excel(name = "批次号")
    private String batchCode;

    private Long materialId;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    @Excel(name = "转出工序序号")
    private Integer fromStepSeq;

    @Excel(name = "转出工序")
    private String fromProcessName;

    @Excel(name = "转入工序序号")
    private Integer toStepSeq;

    @Excel(name = "转入工序")
    private String toProcessName;

    @Excel(name = "转移数量")
    private BigDecimal transferQty;

    @Excel(name = "损耗数量")
    private BigDecimal lossQty;

    @Excel(name = "损耗率(%)")
    private BigDecimal lossRate;

    /** 损耗类型(字典food_loss_type) */
    @Excel(name = "损耗类型")
    private String lossType;

    /** 转入工序标准时长(天)快照 */
    private BigDecimal stdDays;

    /** 预计出柜时间（按标准时长推算） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planStepEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "转移时间")
    private Date transferTime;

    @Excel(name = "转移人")
    private String transferBy;

    /** 联动移库单号(wms_move_order) */
    @Excel(name = "联动移库单")
    private String moveNo;

    /** 状态(1已转移) */
    private String status;

    private String delFlag;

    public Long getTransferId() { return transferId; }
    public void setTransferId(Long transferId) { this.transferId = transferId; }
    public String getTransferNo() { return transferNo; }
    public void setTransferNo(String transferNo) { this.transferNo = transferNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
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
    public Integer getFromStepSeq() { return fromStepSeq; }
    public void setFromStepSeq(Integer fromStepSeq) { this.fromStepSeq = fromStepSeq; }
    public String getFromProcessName() { return fromProcessName; }
    public void setFromProcessName(String fromProcessName) { this.fromProcessName = fromProcessName; }
    public Integer getToStepSeq() { return toStepSeq; }
    public void setToStepSeq(Integer toStepSeq) { this.toStepSeq = toStepSeq; }
    public String getToProcessName() { return toProcessName; }
    public void setToProcessName(String toProcessName) { this.toProcessName = toProcessName; }
    public BigDecimal getTransferQty() { return transferQty; }
    public void setTransferQty(BigDecimal transferQty) { this.transferQty = transferQty; }
    public BigDecimal getLossQty() { return lossQty; }
    public void setLossQty(BigDecimal lossQty) { this.lossQty = lossQty; }
    public BigDecimal getLossRate() { return lossRate; }
    public void setLossRate(BigDecimal lossRate) { this.lossRate = lossRate; }
    public String getLossType() { return lossType; }
    public void setLossType(String lossType) { this.lossType = lossType; }
    public BigDecimal getStdDays() { return stdDays; }
    public void setStdDays(BigDecimal stdDays) { this.stdDays = stdDays; }
    public Date getPlanStepEnd() { return planStepEnd; }
    public void setPlanStepEnd(Date planStepEnd) { this.planStepEnd = planStepEnd; }
    public Date getTransferTime() { return transferTime; }
    public void setTransferTime(Date transferTime) { this.transferTime = transferTime; }
    public String getTransferBy() { return transferBy; }
    public void setTransferBy(String transferBy) { this.transferBy = transferBy; }
    public String getMoveNo() { return moveNo; }
    public void setMoveNo(String moveNo) { this.moveNo = moveNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
