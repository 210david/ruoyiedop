package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 批次追溯 mms_batch_trace
 *
 * @author ruoyi
 */
public class MmsBatchTrace extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long traceId;

    private Long workOrderId;

    @Excel(name = "工序序号")
    private Integer opSeq;

    @Excel(name = "产出批次")
    private String batchOut;

    @Excel(name = "投入批次")
    private String batchIn;

    private Long materialIn;

    @Excel(name = "物料编码")
    private String materialCode;

    @Excel(name = "使用数量")
    private BigDecimal qtyUsed;

    public Long getTraceId() { return traceId; }
    public void setTraceId(Long traceId) { this.traceId = traceId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Integer getOpSeq() { return opSeq; }
    public void setOpSeq(Integer opSeq) { this.opSeq = opSeq; }
    public String getBatchOut() { return batchOut; }
    public void setBatchOut(String batchOut) { this.batchOut = batchOut; }
    public String getBatchIn() { return batchIn; }
    public void setBatchIn(String batchIn) { this.batchIn = batchIn; }
    public Long getMaterialIn() { return materialIn; }
    public void setMaterialIn(Long materialIn) { this.materialIn = materialIn; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public BigDecimal getQtyUsed() { return qtyUsed; }
    public void setQtyUsed(BigDecimal qtyUsed) { this.qtyUsed = qtyUsed; }
}
