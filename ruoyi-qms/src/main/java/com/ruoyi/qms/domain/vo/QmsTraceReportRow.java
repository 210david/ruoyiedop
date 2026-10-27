package com.ruoyi.qms.domain.vo;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 批次追溯报告行（Excel导出用）
 *
 * 将正反向追溯树拍平为扁平行：每个谱系节点一行，
 * 汇总该批次的检验记录与NCR摘要，供召回/审核留档。
 *
 * @author ruoyi
 */
public class QmsTraceReportRow implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 追溯方向 */
    @Excel(name = "追溯方向", readConverterExp = "backward=上游原料,root=起点批次,forward=下游去向")
    private String direction;

    /** 层级（起点为0，向下/向上逐层+1） */
    @Excel(name = "层级")
    private Integer level;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchNo;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 事件类型 */
    @Excel(name = "事件类型", readConverterExp = "feeding=投料,report=报工,complete=完工,transfer=调拨,rework=返工,merge=合并,split=拆分,manual=补录")
    private String sourceType;

    /** 来源单据号 */
    @Excel(name = "来源单据号")
    private String sourceNo;

    /** 工单号 */
    @Excel(name = "工单号")
    private String workOrderNo;

    /** 投入数量 */
    @Excel(name = "投入数量")
    private String qtyIn;

    /** 供应商 */
    @Excel(name = "供应商")
    private String supplierName;

    /** 检验记录摘要（任务号：结果 多条分号分隔） */
    @Excel(name = "检验记录")
    private String inspectInfo;

    /** NCR记录摘要（单号 多条分号分隔） */
    @Excel(name = "NCR记录")
    private String ncrInfo;

    /** 谱系时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "谱系时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date traceTime;

    /** 断点标记 */
    @Excel(name = "断点", readConverterExp = "0=否,1=是")
    private Integer breakFlag;

    /** 断点原因 */
    @Excel(name = "断点原因")
    private String breakReason;

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public String getQtyIn() { return qtyIn; }
    public void setQtyIn(String qtyIn) { this.qtyIn = qtyIn; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getInspectInfo() { return inspectInfo; }
    public void setInspectInfo(String inspectInfo) { this.inspectInfo = inspectInfo; }
    public String getNcrInfo() { return ncrInfo; }
    public void setNcrInfo(String ncrInfo) { this.ncrInfo = ncrInfo; }
    public Date getTraceTime() { return traceTime; }
    public void setTraceTime(Date traceTime) { this.traceTime = traceTime; }
    public Integer getBreakFlag() { return breakFlag; }
    public void setBreakFlag(Integer breakFlag) { this.breakFlag = breakFlag; }
    public String getBreakReason() { return breakReason; }
    public void setBreakReason(String breakReason) { this.breakReason = breakReason; }
}
