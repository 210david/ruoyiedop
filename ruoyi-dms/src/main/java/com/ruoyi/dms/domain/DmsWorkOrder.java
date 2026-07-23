package com.ruoyi.dms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工单管理 dms_work_order
 *
 * @author ruoyi
 */
public class DmsWorkOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 工单ID */
    private Long orderId;

    /** 工单号 */
    @Excel(name = "工单号")
    private String orderNo;

    /** 设备ID */
    private Long equipmentId;

    /** 设备编号（冗余） */
    @Excel(name = "设备编号")
    private String equipmentCode;

    /** 设备名称（冗余） */
    @Excel(name = "设备名称")
    private String equipmentName;

    /** 工单类型（字典 dms_order_type） */
    @Excel(name = "工单类型", dictType = "dms_order_type")
    private String orderType;

    /** 故障现象/任务描述 */
    @Excel(name = "故障描述")
    private String faultDescription;

    /** 报修人ID */
    private Long reporterId;

    /** 报修人名称 */
    @Excel(name = "报修人")
    private String reporterName;

    /** 报修时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "报修时间", width = 30)
    private Date reportTime;

    /** 优先级（字典 dms_priority） */
    @Excel(name = "优先级", dictType = "dms_priority")
    private String priority;

    /** 工单状态（字典 dms_order_status） */
    @Excel(name = "工单状态", dictType = "dms_order_status")
    private String orderStatus;

    /** 维修人ID */
    private Long assigneeId;

    /** 维修人名称 */
    @Excel(name = "维修人")
    private String assigneeName;

    /** 派工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date assignTime;

    /** 到达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date arriveTime;

    /** SLA响应截止时间（派工时效） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date slaResponseDeadline;

    /** SLA处理截止时间（处理时效） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date slaProcessDeadline;

    /** SLA超时状态(0正常 1响应超时 2处理超时) */
    @Excel(name = "SLA超时", readConverterExp = "0=正常,1=响应超时,2=处理超时")
    private String slaTimeoutStatus;

    /** SLA是否已升级(0否 1是) */
    private String slaEscalated;

    /** 故障原因 */
    private String faultCause;

    /** 维修措施 */
    private String repairMeasure;

    /** 更换备件 */
    private String sparePartsUsed;

    /** 任务清单（JSON数组，含完成状态） */
    private String taskChecklist;

    /** 停机时长（小时） */
    @Excel(name = "停机时长")
    private BigDecimal downtimeDuration;

    /** 完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

    /** 完工说明 */
    private String completeRemark;

    /** 偏差原因（PM工单未按时完成时记录） */
    private String deviationReason;

    /** 派工人ID */
    private Long assignerId;

    /** 派工人名称 */
    private String assignerName;

    /** 验收人ID */
    private Long verifierId;

    /** 验收人名称 */
    @Excel(name = "验收人")
    private String verifierName;

    /** 验收时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date verifyTime;

    /** 验收意见 */
    private String verifyOpinion;

    /** 评价（1-5星） */
    @Excel(name = "评价")
    private Integer rating;

    /** 删除标志 */
    private String delFlag;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getFaultDescription() { return faultDescription; }
    public void setFaultDescription(String faultDescription) { this.faultDescription = faultDescription; }
    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public Date getReportTime() { return reportTime; }
    public void setReportTime(Date reportTime) { this.reportTime = reportTime; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public Date getAssignTime() { return assignTime; }
    public void setAssignTime(Date assignTime) { this.assignTime = assignTime; }
    public Date getArriveTime() { return arriveTime; }
    public void setArriveTime(Date arriveTime) { this.arriveTime = arriveTime; }
    public Date getSlaResponseDeadline() { return slaResponseDeadline; }
    public void setSlaResponseDeadline(Date slaResponseDeadline) { this.slaResponseDeadline = slaResponseDeadline; }
    public Date getSlaProcessDeadline() { return slaProcessDeadline; }
    public void setSlaProcessDeadline(Date slaProcessDeadline) { this.slaProcessDeadline = slaProcessDeadline; }
    public String getSlaTimeoutStatus() { return slaTimeoutStatus; }
    public void setSlaTimeoutStatus(String slaTimeoutStatus) { this.slaTimeoutStatus = slaTimeoutStatus; }
    public String getSlaEscalated() { return slaEscalated; }
    public void setSlaEscalated(String slaEscalated) { this.slaEscalated = slaEscalated; }
    public String getFaultCause() { return faultCause; }
    public void setFaultCause(String faultCause) { this.faultCause = faultCause; }
    public String getRepairMeasure() { return repairMeasure; }
    public void setRepairMeasure(String repairMeasure) { this.repairMeasure = repairMeasure; }
    public String getSparePartsUsed() { return sparePartsUsed; }
    public void setSparePartsUsed(String sparePartsUsed) { this.sparePartsUsed = sparePartsUsed; }
    public String getTaskChecklist() { return taskChecklist; }
    public void setTaskChecklist(String taskChecklist) { this.taskChecklist = taskChecklist; }
    public BigDecimal getDowntimeDuration() { return downtimeDuration; }
    public void setDowntimeDuration(BigDecimal downtimeDuration) { this.downtimeDuration = downtimeDuration; }
    public Date getCompleteTime() { return completeTime; }
    public void setCompleteTime(Date completeTime) { this.completeTime = completeTime; }
    public String getCompleteRemark() { return completeRemark; }
    public void setCompleteRemark(String completeRemark) { this.completeRemark = completeRemark; }
    public String getDeviationReason() { return deviationReason; }
    public void setDeviationReason(String deviationReason) { this.deviationReason = deviationReason; }
    public Long getAssignerId() { return assignerId; }
    public void setAssignerId(Long assignerId) { this.assignerId = assignerId; }
    public String getAssignerName() { return assignerName; }
    public void setAssignerName(String assignerName) { this.assignerName = assignerName; }
    public Long getVerifierId() { return verifierId; }
    public void setVerifierId(Long verifierId) { this.verifierId = verifierId; }
    public String getVerifierName() { return verifierName; }
    public void setVerifierName(String verifierName) { this.verifierName = verifierName; }
    public Date getVerifyTime() { return verifyTime; }
    public void setVerifyTime(Date verifyTime) { this.verifyTime = verifyTime; }
    public String getVerifyOpinion() { return verifyOpinion; }
    public void setVerifyOpinion(String verifyOpinion) { this.verifyOpinion = verifyOpinion; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
