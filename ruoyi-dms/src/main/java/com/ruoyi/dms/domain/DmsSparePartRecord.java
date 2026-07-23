package com.ruoyi.dms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 备件出入库记录 dms_spare_part_record
 */
public class DmsSparePartRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long partId;

    @Excel(name = "单据号")
    private String documentCode;

    @Excel(name = "备件编号")
    private String partCode;

    @Excel(name = "备件名称")
    private String partName;

    /** 单位（关联备件表带出） */
    @Excel(name = "单位", dictType = "wms_unit")
    private String unit;

    @Excel(name = "类型", dictType = "dms_stock_move_type")
    private String moveType;

    @Excel(name = "入库类型", dictType = "dms_partin_type")
    private String sourceType;

    @Excel(name = "出库类型", dictType = "dms_partout_type")
    private String targetType;

    @Excel(name = "供应商/领用部门")
    private String supplierOrDept;

    /** 领用人 */
    @Excel(name = "领用人")
    private String receiver;

    @Excel(name = "数量")
    private BigDecimal quantity;

    @Excel(name = "变更前")
    private BigDecimal beforeStock;
    @Excel(name = "变更后")
    private BigDecimal afterStock;

    private String refType;
    private Long refId;
    private String refNo;

    @Excel(name = "操作人")
    private String operatorName;

    /** 操作日期（入库日期/出库日期） */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "操作日期", width = 20)
    private Date operateDate;

    /** 存放位置（入库时填写） */
    @Excel(name = "存放位置")
    private String storageLocation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "操作时间", width = 30)
    private Date createTime;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getPartId() { return partId; }
    public void setPartId(Long partId) { this.partId = partId; }
    public String getDocumentCode() { return documentCode; }
    public void setDocumentCode(String documentCode) { this.documentCode = documentCode; }
    public String getPartCode() { return partCode; }
    public void setPartCode(String partCode) { this.partCode = partCode; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getMoveType() { return moveType; }
    public void setMoveType(String moveType) { this.moveType = moveType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getSupplierOrDept() { return supplierOrDept; }
    public void setSupplierOrDept(String supplierOrDept) { this.supplierOrDept = supplierOrDept; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getBeforeStock() { return beforeStock; }
    public void setBeforeStock(BigDecimal beforeStock) { this.beforeStock = beforeStock; }
    public BigDecimal getAfterStock() { return afterStock; }
    public void setAfterStock(BigDecimal afterStock) { this.afterStock = afterStock; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }
    public String getRefNo() { return refNo; }
    public void setRefNo(String refNo) { this.refNo = refNo; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getOperateDate() { return operateDate; }
    public void setOperateDate(Date operateDate) { this.operateDate = operateDate; }
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    @Override
    public Date getCreateTime() { return createTime; }
    @Override
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
