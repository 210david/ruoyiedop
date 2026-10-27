package com.ruoyi.food.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产出率损耗核算行（完工分析产出率视图）
 *
 * 火腿按产出率模型：投入鲜腿 × 基准产出率(76%) = 理论产出，与实际完工对比核算损耗；
 * 分环节损耗来自工序转移单（修割损耗/盐渍损耗/发酵失重/其他）。
 *
 * @author ruoyi
 */
public class FoodYieldRow extends BaseEntity
{
    private Long workOrderId;

    @Excel(name = "工单号")
    private String workOrderNo;

    private Long productId;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "配方模型")
    private String yieldModel;

    @Excel(name = "排产模式")
    private String scheduleMode;

    @Excel(name = "基准产出率(%)")
    private BigDecimal stdYieldRate;

    @Excel(name = "产出率告警下限(%)")
    private BigDecimal yieldAlarmRate;

    @Excel(name = "计划数量")
    private BigDecimal planQty;

    /** 投入量（工单领料明细合计） */
    @Excel(name = "投入量")
    private BigDecimal inputQty;

    /** 理论产出 = 投入量 × 基准产出率 */
    @Excel(name = "理论产出")
    private BigDecimal theoryQty;

    /** 实际产出 = 工单合格数量 */
    @Excel(name = "实际产出")
    private BigDecimal actualQty;

    /** 产出率 = 实际/投入×100 */
    @Excel(name = "产出率(%)")
    private BigDecimal yieldRate;

    /** 损耗率 = 100 - 产出率 */
    @Excel(name = "损耗率(%)")
    private BigDecimal lossRate;

    @Excel(name = "修割损耗")
    private BigDecimal lossCut;

    @Excel(name = "盐渍损耗")
    private BigDecimal lossSalt;

    @Excel(name = "发酵失重")
    private BigDecimal lossFerment;

    @Excel(name = "其他损耗")
    private BigDecimal lossOther;

    @Excel(name = "损耗合计")
    private BigDecimal lossTotal;

    /** 告警标志(0正常1低于告警下限) */
    @Excel(name = "告警", readConverterExp = "0=正常,1=告警")
    private String alarmFlag;

    @Excel(name = "工单状态")
    private String status;

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getYieldModel() { return yieldModel; }
    public void setYieldModel(String yieldModel) { this.yieldModel = yieldModel; }
    public String getScheduleMode() { return scheduleMode; }
    public void setScheduleMode(String scheduleMode) { this.scheduleMode = scheduleMode; }
    public BigDecimal getStdYieldRate() { return stdYieldRate; }
    public void setStdYieldRate(BigDecimal stdYieldRate) { this.stdYieldRate = stdYieldRate; }
    public BigDecimal getYieldAlarmRate() { return yieldAlarmRate; }
    public void setYieldAlarmRate(BigDecimal yieldAlarmRate) { this.yieldAlarmRate = yieldAlarmRate; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public BigDecimal getInputQty() { return inputQty; }
    public void setInputQty(BigDecimal inputQty) { this.inputQty = inputQty; }
    public BigDecimal getTheoryQty() { return theoryQty; }
    public void setTheoryQty(BigDecimal theoryQty) { this.theoryQty = theoryQty; }
    public BigDecimal getActualQty() { return actualQty; }
    public void setActualQty(BigDecimal actualQty) { this.actualQty = actualQty; }
    public BigDecimal getYieldRate() { return yieldRate; }
    public void setYieldRate(BigDecimal yieldRate) { this.yieldRate = yieldRate; }
    public BigDecimal getLossRate() { return lossRate; }
    public void setLossRate(BigDecimal lossRate) { this.lossRate = lossRate; }
    public BigDecimal getLossCut() { return lossCut; }
    public void setLossCut(BigDecimal lossCut) { this.lossCut = lossCut; }
    public BigDecimal getLossSalt() { return lossSalt; }
    public void setLossSalt(BigDecimal lossSalt) { this.lossSalt = lossSalt; }
    public BigDecimal getLossFerment() { return lossFerment; }
    public void setLossFerment(BigDecimal lossFerment) { this.lossFerment = lossFerment; }
    public BigDecimal getLossOther() { return lossOther; }
    public void setLossOther(BigDecimal lossOther) { this.lossOther = lossOther; }
    public BigDecimal getLossTotal() { return lossTotal; }
    public void setLossTotal(BigDecimal lossTotal) { this.lossTotal = lossTotal; }
    public String getAlarmFlag() { return alarmFlag; }
    public void setAlarmFlag(String alarmFlag) { this.alarmFlag = alarmFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
