package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/** 危化品出入库记录 */
public class SafetyMaterialRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;
    @Excel(name = "出入库单号")
    private String recordCode;
    @NotNull(message = "危化品不能为空")
    private Long materialId;
    @Excel(name = "危化品编号")
    private String materialCode;
    @Excel(name = "危化品名称")
    private String materialName;
    @Excel(name = "类型", readConverterExp = "1=入库,2=出库")
    private String recordType;
    @Excel(name = "数量")
    private BigDecimal quantity;
    @Excel(name = "单位")
    private String unit;
    private BigDecimal beforeStock;
    private BigDecimal afterStock;
    private Long supplierId;
    @Excel(name = "供应商/客户")
    private String supplierName;
    @Excel(name = "领用人")
    private String recipient;
    @Excel(name = "用途")
    private String purpose;
    @Excel(name = "批次号")
    private String batchNo;
    @Excel(name = "操作人")
    private String operator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "出入库时间", width = 160)
    private Date recordTime;
    private String attachment;
    private Long enterpriseId;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getRecordCode() { return recordCode; }
    public void setRecordCode(String recordCode) { this.recordCode = recordCode; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getBeforeStock() { return beforeStock; }
    public void setBeforeStock(BigDecimal beforeStock) { this.beforeStock = beforeStock; }
    public BigDecimal getAfterStock() { return afterStock; }
    public void setAfterStock(BigDecimal afterStock) { this.afterStock = afterStock; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public Date getRecordTime() { return recordTime; }
    public void setRecordTime(Date recordTime) { this.recordTime = recordTime; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
