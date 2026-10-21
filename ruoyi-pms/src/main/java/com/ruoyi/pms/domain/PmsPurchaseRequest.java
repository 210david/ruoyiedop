package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购申请表 pms_purchase_request
 *
 * @author ruoyi
 */
public class PmsPurchaseRequest extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 申请ID */
    private Long requestId;

    /** 申请单号 */
    @Excel(name = "申请单号")
    private String requestNo;

    /** 申请标题 */
    @Excel(name = "申请标题")
    private String title;

    /** 状态（字典 pms_request_status） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=待审批,2=已审批,3=已驳回,4=已转采购,5=已作废")
    private String status;

    /** 优先级（字典 pms_priority） */
    @Excel(name = "优先级", readConverterExp = "0=普通,1=紧急,2=特急")
    private String priority;

    /** 期望交货日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "期望交货日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expectDate;

    /** 采购用途 */
    @Excel(name = "采购用途")
    private String purpose;

    /** 预估总金额 */
    @Excel(name = "预估总金额")
    private BigDecimal totalAmount;

    /** 申请人ID */
    private Long requesterId;

    /** 申请人姓名 */
    @Excel(name = "申请人")
    private String requesterName;

    /** 申请部门ID */
    private Long deptId;

    /** 申请部门名称 */
    @Excel(name = "申请部门")
    private String deptName;

    /** 审批人 */
    @Excel(name = "审批人")
    private String auditBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审批意见 */
    private String auditOpinion;

    /** 关联采购计划ID */
    private Long planId;

    /** 关联采购计划单号 */
    private String planNo;

    /** 删除标志 */
    private String delFlag;

    /** 采购申请明细列表 */
    private List<PmsPurchaseRequestDetail> detailList;

    /** 审核日志列表 */
    private List<PmsRequestAuditLog> auditLogList;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getRequestNo() { return requestNo; }
    public void setRequestNo(String requestNo) { this.requestNo = requestNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Date getExpectDate() { return expectDate; }
    public void setExpectDate(Date expectDate) { this.expectDate = expectDate; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanNo() { return planNo; }
    public void setPlanNo(String planNo) { this.planNo = planNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PmsPurchaseRequestDetail> getDetailList() { return detailList; }
    public void setDetailList(List<PmsPurchaseRequestDetail> detailList) { this.detailList = detailList; }

    public List<PmsRequestAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsRequestAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
