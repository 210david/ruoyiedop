package com.ruoyi.qms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 批次谱系表 qms_lot_genealogy
 *
 * 记录投入批次↔工单↔产出批次的关联关系链，支撑正反向追溯。
 *
 * @author ruoyi
 */
public class QmsLotGenealogy extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 投入批次号（原料/半成品） */
    @Excel(name = "投入批次号")
    private String parentBatchNo;

    /** 产出批次号（半成品/成品） */
    @Excel(name = "产出批次号")
    private String childBatchNo;

    /** 生产工单号 */
    @Excel(name = "工单号")
    private String workOrderNo;

    /** 工序ID */
    private String opId;

    /** 工序名称 */
    @Excel(name = "工序")
    private String opName;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 投入数量 */
    @Excel(name = "投入数量")
    private BigDecimal qtyIn;

    /** 产出数量 */
    @Excel(name = "产出数量")
    private BigDecimal qtyOut;

    /** 产线ID */
    private String lineId;

    /** 产线名称 */
    @Excel(name = "产线")
    private String lineName;

    /** 操作员 */
    @Excel(name = "操作员")
    private String operator;

    /** 供应商ID */
    private Long supplierId;

    /** 供应商名称 */
    @Excel(name = "供应商")
    private String supplierName;

    /** 来源类型（feeding=投料, report=报工, complete=完工, transfer=调拨, rework=返工, merge=合并, split=拆分, manual=补录） */
    @Excel(name = "来源类型", readConverterExp = "feeding=投料,report=报工,complete=完工,transfer=调拨,rework=返工,merge=合并,split=拆分,manual=补录")
    private String sourceType;

    /** 谱系时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "谱系时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date traceTime;

    /** 断点标记（0=正常 1=断点） */
    @Excel(name = "断点", readConverterExp = "0=否,1=是")
    private Integer breakFlag;

    /** 断点原因 */
    private String breakReason;

    /** 删除标志 */
    private String delFlag;

    /** 状态（0正常 1停用） */
    private String status;

    /** 子节点列表（追溯树用，非数据库字段） */
    private List<QmsLotGenealogy> children;

    /** 关联检验记录（追溯用，非数据库字段） */
    private List<QmsInspTask> inspectTasks;

    /** 关联NCR记录（追溯用，非数据库字段） */
    private List<QmsNcr> ncrList;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getParentBatchNo() { return parentBatchNo; }
    public void setParentBatchNo(String parentBatchNo) { this.parentBatchNo = parentBatchNo; }
    public String getChildBatchNo() { return childBatchNo; }
    public void setChildBatchNo(String childBatchNo) { this.childBatchNo = childBatchNo; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public String getOpId() { return opId; }
    public void setOpId(String opId) { this.opId = opId; }
    public String getOpName() { return opName; }
    public void setOpName(String opName) { this.opName = opName; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public BigDecimal getQtyIn() { return qtyIn; }
    public void setQtyIn(BigDecimal qtyIn) { this.qtyIn = qtyIn; }
    public BigDecimal getQtyOut() { return qtyOut; }
    public void setQtyOut(BigDecimal qtyOut) { this.qtyOut = qtyOut; }
    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public Date getTraceTime() { return traceTime; }
    public void setTraceTime(Date traceTime) { this.traceTime = traceTime; }
    public Integer getBreakFlag() { return breakFlag; }
    public void setBreakFlag(Integer breakFlag) { this.breakFlag = breakFlag; }
    public String getBreakReason() { return breakReason; }
    public void setBreakReason(String breakReason) { this.breakReason = breakReason; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<QmsLotGenealogy> getChildren() { return children; }
    public void setChildren(List<QmsLotGenealogy> children) { this.children = children; }
    public List<QmsInspTask> getInspectTasks() { return inspectTasks; }
    public void setInspectTasks(List<QmsInspTask> inspectTasks) { this.inspectTasks = inspectTasks; }
    public List<QmsNcr> getNcrList() { return ncrList; }
    public void setNcrList(List<QmsNcr> ncrList) { this.ncrList = ncrList; }
}
