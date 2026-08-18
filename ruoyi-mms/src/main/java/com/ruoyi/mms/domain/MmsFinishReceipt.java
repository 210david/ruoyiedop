package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class MmsFinishReceipt extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long finishId;
    @Excel(name = "入库单号") private String finishNo;
    private Long workOrderId;
    @Excel(name = "工单号") private String workOrderNo;
    private Long productId;
    @Excel(name = "产品编码") private String productCode;
    @Excel(name = "产品名称") private String productName;
    @Excel(name = "单位") private String unit;
    @Excel(name = "完工数量") private BigDecimal finishQty;
    @Excel(name = "合格数量") private BigDecimal qualifiedQty;
    @Excel(name = "批次号") private String batchNo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date finishTime;
    @Excel(name = "完工人") private String finishBy;
    @Excel(name = "是否按期") private String isOnTime;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long getFinishId() { return finishId; }
    public void setFinishId(Long finishId) { this.finishId = finishId; }
    public String getFinishNo() { return finishNo; }
    public void setFinishNo(String finishNo) { this.finishNo = finishNo; }
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
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getFinishQty() { return finishQty; }
    public void setFinishQty(BigDecimal finishQty) { this.finishQty = finishQty; }
    public BigDecimal getQualifiedQty() { return qualifiedQty; }
    public void setQualifiedQty(BigDecimal qualifiedQty) { this.qualifiedQty = qualifiedQty; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Date getFinishTime() { return finishTime; }
    public void setFinishTime(Date finishTime) { this.finishTime = finishTime; }
    public String getFinishBy() { return finishBy; }
    public void setFinishBy(String finishBy) { this.finishBy = finishBy; }
    public String getIsOnTime() { return isOnTime; }
    public void setIsOnTime(String isOnTime) { this.isOnTime = isOnTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
