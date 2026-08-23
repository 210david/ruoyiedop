package com.ruoyi.mms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class MmsAbnormal extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long abnormalId;
    @Excel(name = "异常单号") private String abnormalNo;
    private Long workOrderId;
    @Excel(name = "工单号") private String workOrderNo;
    private Long resourceId;
    @Excel(name = "产能单元") private String resourceName;
    @Excel(name = "产品编号") private String productCode;
    @Excel(name = "产品名称") private String productName;
    @Excel(name = "规格型号") private String specModel;
    @Excel(name = "单位") private String unit;
    @Excel(name = "异常类型", readConverterExp = "0=设备,1=物料,2=质量,3=安全,4=其他") private String abnormalType;
    @Excel(name = "严重等级", readConverterExp = "0=一般,1=严重,2=紧急") private String severity;
    private String description;
    @Excel(name = "上报人") private String reportBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date reportTime;
    private String responseBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date responseTime;
    private String handleBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date handleTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date closeTime;
    private String handleResult;
    @Excel(name = "状态", readConverterExp = "0=待响应,1=处理中,2=已关闭") private String status;
    private String delFlag;

    public Long getAbnormalId() { return abnormalId; }
    public void setAbnormalId(Long abnormalId) { this.abnormalId = abnormalId; }
    public String getAbnormalNo() { return abnormalNo; }
    public void setAbnormalNo(String abnormalNo) { this.abnormalNo = abnormalNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getAbnormalType() { return abnormalType; }
    public void setAbnormalType(String abnormalType) { this.abnormalType = abnormalType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReportBy() { return reportBy; }
    public void setReportBy(String reportBy) { this.reportBy = reportBy; }
    public Date getReportTime() { return reportTime; }
    public void setReportTime(Date reportTime) { this.reportTime = reportTime; }
    public String getResponseBy() { return responseBy; }
    public void setResponseBy(String responseBy) { this.responseBy = responseBy; }
    public Date getResponseTime() { return responseTime; }
    public void setResponseTime(Date responseTime) { this.responseTime = responseTime; }
    public String getHandleBy() { return handleBy; }
    public void setHandleBy(String handleBy) { this.handleBy = handleBy; }
    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }
    public Date getCloseTime() { return closeTime; }
    public void setCloseTime(Date closeTime) { this.closeTime = closeTime; }
    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String handleResult) { this.handleResult = handleResult; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
