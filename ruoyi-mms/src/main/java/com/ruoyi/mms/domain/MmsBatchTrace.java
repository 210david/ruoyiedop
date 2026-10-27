package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 批次追溯（数据源：qms_lot_genealogy 谱系表）
 *
 * 改造说明（2026-09-19）：列表主表由「齐套明细」换为「批次谱系」，
 * 行粒度 = 一条谱系事件（领料 feeding / 报工 report / 完工 complete / 手工补录 manual），
 * 天然包含工序、产线、供应商、数量，且不依赖齐套明细是否存在。
 *
 * @author ruoyi
 */
public class MmsBatchTrace extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 谱系ID（主键） */
    private Long traceId;

    /** 来源类型（feeding=领料 report=报工 complete=完工 manual=补录 rework=返工） */
    private String sourceType;

    /** 来源单据号（幂等键） */
    @Excel(name = "来源单据")
    private String sourceNo;

    /** 产出批次 */
    @Excel(name = "产出批次")
    private String batchOut;

    /** 投入批次 */
    @Excel(name = "投入批次")
    private String batchIn;

    /** 生产工单号 */
    @Excel(name = "工单号")
    private String workOrderNo;

    private Long workOrderId;

    /** 工序名称 */
    @Excel(name = "工序")
    private String opName;

    private String opId;

    /** 产线/产能单元 */
    @Excel(name = "产线")
    private String lineName;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    private Long materialIn;

    /** 投入数量 */
    @Excel(name = "投入数量")
    private BigDecimal qtyUsed;

    /** 产出数量 */
    @Excel(name = "产出数量")
    private BigDecimal qtyOut;

    /** 供应商 */
    @Excel(name = "供应商")
    private String supplierName;

    /** 操作员 */
    @Excel(name = "操作员")
    private String operator;

    /** 产品编码（来源工单，非谱系字段） */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称（来源工单，非谱系字段） */
    @Excel(name = "产品名称")
    private String productName;

    /** 规格型号（来源工单，非谱系字段） */
    private String specModel;

    /** 单位（来源工单，非谱系字段） */
    private String unit;

    /** 谱系时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "追溯时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date traceTime;

    /** 断点标记（0=正常 1=断点） */
    private Integer breakFlag;

    /** 断点原因 */
    private String breakReason;

    public Long getTraceId() { return traceId; }
    public void setTraceId(Long traceId) { this.traceId = traceId; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public String getBatchOut() { return batchOut; }
    public void setBatchOut(String batchOut) { this.batchOut = batchOut; }
    public String getBatchIn() { return batchIn; }
    public void setBatchIn(String batchIn) { this.batchIn = batchIn; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getOpName() { return opName; }
    public void setOpName(String opName) { this.opName = opName; }
    public String getOpId() { return opId; }
    public void setOpId(String opId) { this.opId = opId; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public Long getMaterialIn() { return materialIn; }
    public void setMaterialIn(Long materialIn) { this.materialIn = materialIn; }
    public BigDecimal getQtyUsed() { return qtyUsed; }
    public void setQtyUsed(BigDecimal qtyUsed) { this.qtyUsed = qtyUsed; }
    public BigDecimal getQtyOut() { return qtyOut; }
    public void setQtyOut(BigDecimal qtyOut) { this.qtyOut = qtyOut; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Date getTraceTime() { return traceTime; }
    public void setTraceTime(Date traceTime) { this.traceTime = traceTime; }
    public Integer getBreakFlag() { return breakFlag; }
    public void setBreakFlag(Integer breakFlag) { this.breakFlag = breakFlag; }
    public String getBreakReason() { return breakReason; }
    public void setBreakReason(String breakReason) { this.breakReason = breakReason; }
}
