package com.ruoyi.qms.domain.vo;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 降级追溯结果 VO
 *
 * 当谱系表（qms_lot_genealogy）中无该批次数据时，
 * 利用已有业务单据中的 batch_no 做辅助关联追溯，
 * 返回检验记录、NCR、仓库流转、采购收货、客诉等多维度信息。
 *
 * @author ruoyi
 */
public class TraceFallbackVO
{
    /** 追溯的批次号 */
    private String batchNo;

    /** 追溯方向（forward=正向 / backward=反向） */
    private String direction;

    /** 追溯时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date traceTime;

    /** 谱系节点数（谱系表有数据时 >0，为0表示谱系未建立） */
    private int genealogyNodeCount;

    /** 是否为降级追溯（true=谱系无数据，使用业务单据辅助追溯） */
    private boolean fallback;

    /** 提示消息 */
    private String message;

    /** 检验记录列表 */
    private List<InspTaskSummary> inspectTasks;

    /** NCR记录列表 */
    private List<NcrSummary> ncrList;

    /** 仓库流转记录列表 */
    private List<InventoryLogSummary> inventoryLogs;

    /** 采购收货记录列表 */
    private List<ReceiveSummary> receiveList;

    /** 客诉记录列表 */
    private List<ComplaintSummary> complaintList;

    /** 库存快照列表 */
    private List<InventorySummary> inventoryList;

    /** 销售发货记录列表 */
    private List<ShipmentSummary> shipmentList;

    // ==================== 内部类 ====================

    /** 检验记录摘要 */
    public static class InspTaskSummary
    {
        private String taskNo;
        private String taskType;
        private String sourceType;
        private String sourceNo;
        private String materialCode;
        private String materialName;
        private String supplierName;
        private String batchNo;
        private String inspectResult;
        private String defectLevel;
        private String inspectorName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date inspectTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date judgeTime;

        public String getTaskNo() { return taskNo; }
        public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getSourceNo() { return sourceNo; }
        public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public String getInspectResult() { return inspectResult; }
        public void setInspectResult(String inspectResult) { this.inspectResult = inspectResult; }
        public String getDefectLevel() { return defectLevel; }
        public void setDefectLevel(String defectLevel) { this.defectLevel = defectLevel; }
        public String getInspectorName() { return inspectorName; }
        public void setInspectorName(String inspectorName) { this.inspectorName = inspectorName; }
        public Date getInspectTime() { return inspectTime; }
        public void setInspectTime(Date inspectTime) { this.inspectTime = inspectTime; }
        public Date getJudgeTime() { return judgeTime; }
        public void setJudgeTime(Date judgeTime) { this.judgeTime = judgeTime; }
    }

    /** NCR记录摘要 */
    public static class NcrSummary
    {
        private String ncrNo;
        private String sourceType;
        private String sourceNo;
        private String materialCode;
        private String materialName;
        private String supplierName;
        private String batchNo;
        private String defectLevel;
        private String defectDesc;
        private String disposition;
        private String ncrStatus;
        private String discovererName;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date discoverTime;

        public String getNcrNo() { return ncrNo; }
        public void setNcrNo(String ncrNo) { this.ncrNo = ncrNo; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getSourceNo() { return sourceNo; }
        public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public String getDefectLevel() { return defectLevel; }
        public void setDefectLevel(String defectLevel) { this.defectLevel = defectLevel; }
        public String getDefectDesc() { return defectDesc; }
        public void setDefectDesc(String defectDesc) { this.defectDesc = defectDesc; }
        public String getDisposition() { return disposition; }
        public void setDisposition(String disposition) { this.disposition = disposition; }
        public String getNcrStatus() { return ncrStatus; }
        public void setNcrStatus(String ncrStatus) { this.ncrStatus = ncrStatus; }
        public String getDiscovererName() { return discovererName; }
        public void setDiscovererName(String discovererName) { this.discovererName = discovererName; }
        public Date getDiscoverTime() { return discoverTime; }
        public void setDiscoverTime(Date discoverTime) { this.discoverTime = discoverTime; }
    }

    /** 仓库库存流水摘要 */
    public static class InventoryLogSummary
    {
        private String materialCode;
        private String materialName;
        private String warehouseName;
        private String batchNo;
        private String changeType;
        private String changeQty;
        private String afterQty;
        private String refOrderType;
        private String refOrderNo;
        private String createBy;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date createTime;

        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
        public String getChangeQty() { return changeQty; }
        public void setChangeQty(String changeQty) { this.changeQty = changeQty; }
        public String getAfterQty() { return afterQty; }
        public void setAfterQty(String afterQty) { this.afterQty = afterQty; }
        public String getRefOrderType() { return refOrderType; }
        public void setRefOrderType(String refOrderType) { this.refOrderType = refOrderType; }
        public String getRefOrderNo() { return refOrderNo; }
        public void setRefOrderNo(String refOrderNo) { this.refOrderNo = refOrderNo; }
        public String getCreateBy() { return createBy; }
        public void setCreateBy(String createBy) { this.createBy = createBy; }
        public Date getCreateTime() { return createTime; }
        public void setCreateTime(Date createTime) { this.createTime = createTime; }
    }

    /** 采购收货记录摘要 */
    public static class ReceiveSummary
    {
        private String receiveNo;
        private String orderNo;
        private String supplierName;
        private String materialCode;
        private String materialName;
        private String batchNo;
        private String receiveQty;
        private String qualifiedQty;
        private String unqualifiedQty;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date productionDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date expiryDate;
        private String receiveDate;
        private String status;

        public String getReceiveNo() { return receiveNo; }
        public void setReceiveNo(String receiveNo) { this.receiveNo = receiveNo; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public String getSupplierName() { return supplierName; }
        public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public String getReceiveQty() { return receiveQty; }
        public void setReceiveQty(String receiveQty) { this.receiveQty = receiveQty; }
        public String getQualifiedQty() { return qualifiedQty; }
        public void setQualifiedQty(String qualifiedQty) { this.qualifiedQty = qualifiedQty; }
        public String getUnqualifiedQty() { return unqualifiedQty; }
        public void setUnqualifiedQty(String unqualifiedQty) { this.unqualifiedQty = unqualifiedQty; }
        public Date getProductionDate() { return productionDate; }
        public void setProductionDate(Date productionDate) { this.productionDate = productionDate; }
        public Date getExpiryDate() { return expiryDate; }
        public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
        public String getReceiveDate() { return receiveDate; }
        public void setReceiveDate(String receiveDate) { this.receiveDate = receiveDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /** 客诉记录摘要 */
    public static class ComplaintSummary
    {
        private String complaintNo;
        private String customerName;
        private String materialCode;
        private String materialName;
        private String batchNo;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date complaintDate;
        private String complaintType;
        private String severity;
        private String defectDesc;
        private String complaintStatus;

        public String getComplaintNo() { return complaintNo; }
        public void setComplaintNo(String complaintNo) { this.complaintNo = complaintNo; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public Date getComplaintDate() { return complaintDate; }
        public void setComplaintDate(Date complaintDate) { this.complaintDate = complaintDate; }
        public String getComplaintType() { return complaintType; }
        public void setComplaintType(String complaintType) { this.complaintType = complaintType; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getDefectDesc() { return defectDesc; }
        public void setDefectDesc(String defectDesc) { this.defectDesc = defectDesc; }
        public String getComplaintStatus() { return complaintStatus; }
        public void setComplaintStatus(String complaintStatus) { this.complaintStatus = complaintStatus; }
    }

    /** 库存快照摘要 */
    public static class InventorySummary
    {
        private String materialCode;
        private String materialName;
        private String warehouseName;
        private String locationName;
        private String batchNo;
        private String qty;
        private String lockQty;

        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getWarehouseName() { return warehouseName; }
        public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
        public String getLocationName() { return locationName; }
        public void setLocationName(String locationName) { this.locationName = locationName; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public String getQty() { return qty; }
        public void setQty(String qty) { this.qty = qty; }
        public String getLockQty() { return lockQty; }
        public void setLockQty(String lockQty) { this.lockQty = lockQty; }
    }

    /** 销售发货记录摘要 */
    public static class ShipmentSummary
    {
        private String shipmentNo;
        private String orderNo;
        private String customerName;
        private String outboundOrderNo;
        private String shipmentDate;
        private String status;
        private String materialCode;
        private String materialName;
        private String batchNo;
        private String shipQty;
        private String receiverName;
        private String logisticsCompany;
        private String trackingNo;

        public String getShipmentNo() { return shipmentNo; }
        public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getOutboundOrderNo() { return outboundOrderNo; }
        public void setOutboundOrderNo(String outboundOrderNo) { this.outboundOrderNo = outboundOrderNo; }
        public String getShipmentDate() { return shipmentDate; }
        public void setShipmentDate(String shipmentDate) { this.shipmentDate = shipmentDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public String getBatchNo() { return batchNo; }
        public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
        public String getShipQty() { return shipQty; }
        public void setShipQty(String shipQty) { this.shipQty = shipQty; }
        public String getReceiverName() { return receiverName; }
        public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
        public String getLogisticsCompany() { return logisticsCompany; }
        public void setLogisticsCompany(String logisticsCompany) { this.logisticsCompany = logisticsCompany; }
        public String getTrackingNo() { return trackingNo; }
        public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    }

    // ==================== Getter / Setter ====================

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public Date getTraceTime() { return traceTime; }
    public void setTraceTime(Date traceTime) { this.traceTime = traceTime; }
    public int getGenealogyNodeCount() { return genealogyNodeCount; }
    public void setGenealogyNodeCount(int genealogyNodeCount) { this.genealogyNodeCount = genealogyNodeCount; }
    public boolean isFallback() { return fallback; }
    public void setFallback(boolean fallback) { this.fallback = fallback; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public List<InspTaskSummary> getInspectTasks() { return inspectTasks; }
    public void setInspectTasks(List<InspTaskSummary> inspectTasks) { this.inspectTasks = inspectTasks; }
    public List<NcrSummary> getNcrList() { return ncrList; }
    public void setNcrList(List<NcrSummary> ncrList) { this.ncrList = ncrList; }
    public List<InventoryLogSummary> getInventoryLogs() { return inventoryLogs; }
    public void setInventoryLogs(List<InventoryLogSummary> inventoryLogs) { this.inventoryLogs = inventoryLogs; }
    public List<ReceiveSummary> getReceiveList() { return receiveList; }
    public void setReceiveList(List<ReceiveSummary> receiveList) { this.receiveList = receiveList; }
    public List<ComplaintSummary> getComplaintList() { return complaintList; }
    public void setComplaintList(List<ComplaintSummary> complaintList) { this.complaintList = complaintList; }
    public List<InventorySummary> getInventoryList() { return inventoryList; }
    public void setInventoryList(List<InventorySummary> inventoryList) { this.inventoryList = inventoryList; }
    public List<ShipmentSummary> getShipmentList() { return shipmentList; }
    public void setShipmentList(List<ShipmentSummary> shipmentList) { this.shipmentList = shipmentList; }
}
