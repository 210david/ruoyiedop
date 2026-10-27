package com.ruoyi.flowable.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 请假单 flow_leave（工作流示例业务表）
 *
 * @author ruoyi
 */
public class FlowLeave extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 请假单ID */
    private Long leaveId;

    /** 请假标题 */
    @Excel(name = "请假标题")
    private String title;

    /** 请假类型（1事假 2病假 3年假 4调休） */
    @Excel(name = "请假类型", readConverterExp = "1=事假,2=病假,3=年假,4=调休")
    private String leaveType;

    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开始日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结束日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 请假天数 */
    @Excel(name = "请假天数")
    private BigDecimal days;

    /** 请假事由 */
    private String reason;

    /** 状态（0草稿 1审批中 2已通过 3已驳回 4已撤销） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=审批中,2=已通过,3=已驳回,4=已撤销")
    private String status;

    /** 流程实例ID */
    private String instanceId;

    /** 申请人ID */
    private Long startUserId;

    /** 申请人姓名 */
    @Excel(name = "申请人")
    private String startUserName;

    /** 最终审批意见 */
    private String auditOpinion;

    /** 删除标志 */
    private String delFlag;

    public Long getLeaveId() { return leaveId; }
    public void setLeaveId(Long leaveId) { this.leaveId = leaveId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public BigDecimal getDays() { return days; }
    public void setDays(BigDecimal days) { this.days = days; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    public Long getStartUserId() { return startUserId; }
    public void setStartUserId(Long startUserId) { this.startUserId = startUserId; }
    public String getStartUserName() { return startUserName; }
    public void setStartUserName(String startUserName) { this.startUserName = startUserName; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
