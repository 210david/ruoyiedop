package com.ruoyi.food.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 食品在制品台账（垛/批维度） food_wip
 *
 * 长周期WIP跟踪：当前工序位置、入柜/出柜时间、柜位垛位、上霉状态、温湿度记录
 *
 * @author ruoyi
 */
public class FoodWip extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long wipId;

    @Excel(name = "垛号")
    private String wipNo;

    private Long workOrderId;

    @Excel(name = "工单号")
    private String workOrderNo;

    private Long routeId;

    private Long batchId;

    @Excel(name = "批次号")
    private String batchCode;

    private Long materialId;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "物料名称")
    private String materialName;

    @Excel(name = "在制数量")
    private BigDecimal qty;

    @Excel(name = "单位")
    private String qtyUnit;

    @Excel(name = "柜位/垛位")
    private String wipLocation;

    @Excel(name = "当前工序序号")
    private Integer currentStepSeq;

    @Excel(name = "当前工序")
    private String currentProcessName;

    /** 入柜/进入当前工序时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "入柜时间")
    private Date stepStartTime;

    /** 预计出柜时间（入柜时间+工序标准时长） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "预计出柜时间")
    private Date planStepEnd;

    /** 状态(0在制1已完成9已关闭) */
    @Excel(name = "状态", readConverterExp = "0=在制,1=已完成,9=已关闭")
    private String wipStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    private String delFlag;

    /** 在制天数（查询计算列：当前时间-入柜时间，天） */
    private BigDecimal wipDays;

    /** 温湿度/上霉记录列表（详情用） */
    private List<FoodWipEnv> envList;

    public Long getWipId() { return wipId; }
    public void setWipId(Long wipId) { this.wipId = wipId; }
    public String getWipNo() { return wipNo; }
    public void setWipNo(String wipNo) { this.wipNo = wipNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
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
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public String getQtyUnit() { return qtyUnit; }
    public void setQtyUnit(String qtyUnit) { this.qtyUnit = qtyUnit; }
    public String getWipLocation() { return wipLocation; }
    public void setWipLocation(String wipLocation) { this.wipLocation = wipLocation; }
    public Integer getCurrentStepSeq() { return currentStepSeq; }
    public void setCurrentStepSeq(Integer currentStepSeq) { this.currentStepSeq = currentStepSeq; }
    public String getCurrentProcessName() { return currentProcessName; }
    public void setCurrentProcessName(String currentProcessName) { this.currentProcessName = currentProcessName; }
    public Date getStepStartTime() { return stepStartTime; }
    public void setStepStartTime(Date stepStartTime) { this.stepStartTime = stepStartTime; }
    public Date getPlanStepEnd() { return planStepEnd; }
    public void setPlanStepEnd(Date planStepEnd) { this.planStepEnd = planStepEnd; }
    public String getWipStatus() { return wipStatus; }
    public void setWipStatus(String wipStatus) { this.wipStatus = wipStatus; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public BigDecimal getWipDays() { return wipDays; }
    public void setWipDays(BigDecimal wipDays) { this.wipDays = wipDays; }
    public List<FoodWipEnv> getEnvList() { return envList; }
    public void setEnvList(List<FoodWipEnv> envList) { this.envList = envList; }
}
