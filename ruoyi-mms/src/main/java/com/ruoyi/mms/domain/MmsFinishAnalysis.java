package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工单完工分析报表 VO
 * 对应设计方案 RPT-05：工单完工分析
 * 指标：计划数量、完工数量、合格数量、损耗量、损耗率、收率/成材率、标准工时、实际工时、工时达成率
 *
 * @author ruoyi
 */
public class MmsFinishAnalysis extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long workOrderId;

    @Excel(name = "工单号")
    private String workOrderNo;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位", readConverterExp = "0=个,1=件,2=箱,3=kg,4=吨,5=米,6=把,7=千米,8=厘米,9=套,10=台,11=条")
    private String unit;

    @Excel(name = "计划数量")
    private BigDecimal planQty;

    @Excel(name = "完工数量")
    private BigDecimal finishQty;

    @Excel(name = "合格数量")
    private BigDecimal qualifiedQty;

    @Excel(name = "不良数量")
    private BigDecimal defectQty;

    @Excel(name = "损耗量")
    private BigDecimal lossQty;

    @Excel(name = "损耗率(%)")
    private BigDecimal lossRate;

    @Excel(name = "收率(%)")
    private BigDecimal yieldRate;

    @Excel(name = "完工率(%)")
    private BigDecimal finishRate;

    @Excel(name = "合格率(%)")
    private BigDecimal qualifiedRate;

    @Excel(name = "实际工时")
    private BigDecimal actualHours;

    @Excel(name = "计划工时")
    private BigDecimal plannedHours;

    @Excel(name = "工时达成率(%)")
    private BigDecimal hourAchieveRate;

    @Excel(name = "产能单元")
    private String resourceName;

    @Excel(name = "状态", readConverterExp = "0=新建,1=已下达,2=执行中,3=已完工,4=已关闭,5=已暂停,6=已作废")
    private String status;

    @Excel(name = "计划开工", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planStart;

    @Excel(name = "计划完工", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date planFinish;

    @Excel(name = "实际开工", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualStart;

    @Excel(name = "实际完工", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualFinish;

    @Excel(name = "是否准时", readConverterExp = "0=否,1=是")
    private String isOnTime;

    @Excel(name = "报工次数")
    private Integer reportCount;

    // ===== 汇总行字段 =====
    @Excel(name = "工单总数")
    private Integer workOrderCount;

    @Excel(name = "合计计划数量")
    private BigDecimal totalPlanQty;

    @Excel(name = "合计完工数量")
    private BigDecimal totalFinishQty;

    @Excel(name = "合计合格数量")
    private BigDecimal totalQualifiedQty;

    @Excel(name = "合计损耗量")
    private BigDecimal totalLossQty;

    @Excel(name = "平均损耗率(%)")
    private BigDecimal avgLossRate;

    @Excel(name = "平均收率(%)")
    private BigDecimal avgYieldRate;

    @Excel(name = "平均完工率(%)")
    private BigDecimal avgFinishRate;

    @Excel(name = "准时完工率(%)")
    private BigDecimal onTimeRate;

    // ===== get/set =====

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public BigDecimal getFinishQty() { return finishQty; }
    public void setFinishQty(BigDecimal finishQty) { this.finishQty = finishQty; }
    public BigDecimal getQualifiedQty() { return qualifiedQty; }
    public void setQualifiedQty(BigDecimal qualifiedQty) { this.qualifiedQty = qualifiedQty; }
    public BigDecimal getDefectQty() { return defectQty; }
    public void setDefectQty(BigDecimal defectQty) { this.defectQty = defectQty; }
    public BigDecimal getLossQty() { return lossQty; }
    public void setLossQty(BigDecimal lossQty) { this.lossQty = lossQty; }
    public BigDecimal getLossRate() { return lossRate; }
    public void setLossRate(BigDecimal lossRate) { this.lossRate = lossRate; }
    public BigDecimal getYieldRate() { return yieldRate; }
    public void setYieldRate(BigDecimal yieldRate) { this.yieldRate = yieldRate; }
    public BigDecimal getFinishRate() { return finishRate; }
    public void setFinishRate(BigDecimal finishRate) { this.finishRate = finishRate; }
    public BigDecimal getQualifiedRate() { return qualifiedRate; }
    public void setQualifiedRate(BigDecimal qualifiedRate) { this.qualifiedRate = qualifiedRate; }
    public BigDecimal getActualHours() { return actualHours; }
    public void setActualHours(BigDecimal actualHours) { this.actualHours = actualHours; }
    public BigDecimal getPlannedHours() { return plannedHours; }
    public void setPlannedHours(BigDecimal plannedHours) { this.plannedHours = plannedHours; }
    public BigDecimal getHourAchieveRate() { return hourAchieveRate; }
    public void setHourAchieveRate(BigDecimal hourAchieveRate) { this.hourAchieveRate = hourAchieveRate; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getPlanStart() { return planStart; }
    public void setPlanStart(Date planStart) { this.planStart = planStart; }
    public Date getPlanFinish() { return planFinish; }
    public void setPlanFinish(Date planFinish) { this.planFinish = planFinish; }
    public Date getActualStart() { return actualStart; }
    public void setActualStart(Date actualStart) { this.actualStart = actualStart; }
    public Date getActualFinish() { return actualFinish; }
    public void setActualFinish(Date actualFinish) { this.actualFinish = actualFinish; }
    public String getIsOnTime() { return isOnTime; }
    public void setIsOnTime(String isOnTime) { this.isOnTime = isOnTime; }
    public Integer getReportCount() { return reportCount; }
    public void setReportCount(Integer reportCount) { this.reportCount = reportCount; }
    public Integer getWorkOrderCount() { return workOrderCount; }
    public void setWorkOrderCount(Integer workOrderCount) { this.workOrderCount = workOrderCount; }
    public BigDecimal getTotalPlanQty() { return totalPlanQty; }
    public void setTotalPlanQty(BigDecimal totalPlanQty) { this.totalPlanQty = totalPlanQty; }
    public BigDecimal getTotalFinishQty() { return totalFinishQty; }
    public void setTotalFinishQty(BigDecimal totalFinishQty) { this.totalFinishQty = totalFinishQty; }
    public BigDecimal getTotalQualifiedQty() { return totalQualifiedQty; }
    public void setTotalQualifiedQty(BigDecimal totalQualifiedQty) { this.totalQualifiedQty = totalQualifiedQty; }
    public BigDecimal getTotalLossQty() { return totalLossQty; }
    public void setTotalLossQty(BigDecimal totalLossQty) { this.totalLossQty = totalLossQty; }
    public BigDecimal getAvgLossRate() { return avgLossRate; }
    public void setAvgLossRate(BigDecimal avgLossRate) { this.avgLossRate = avgLossRate; }
    public BigDecimal getAvgYieldRate() { return avgYieldRate; }
    public void setAvgYieldRate(BigDecimal avgYieldRate) { this.avgYieldRate = avgYieldRate; }
    public BigDecimal getAvgFinishRate() { return avgFinishRate; }
    public void setAvgFinishRate(BigDecimal avgFinishRate) { this.avgFinishRate = avgFinishRate; }
    public BigDecimal getOnTimeRate() { return onTimeRate; }
    public void setOnTimeRate(BigDecimal onTimeRate) { this.onTimeRate = onTimeRate; }
}
