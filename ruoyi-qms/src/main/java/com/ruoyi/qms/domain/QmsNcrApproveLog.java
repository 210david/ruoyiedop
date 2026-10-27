package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * NCR审批记录表 qms_ncr_approve_log
 *
 * @author ruoyi
 */
public class QmsNcrApproveLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** NCR ID */
    private Long ncrId;

    /** 审批节点序号 */
    private Integer nodeSeq;

    /** 审批节点名称 */
    private String nodeName;

    /** 审批角色标识 */
    private String approverRole;

    /** 操作类型（1提交评审 2审批通过 3审批驳回 4处置提交 5验证通过 6验证不通过 7关闭） */
    private String actionType;

    /** 审批意见 */
    private String approveOpinion;

    /** 审批人ID */
    private Long approverId;

    /** 审批人名称 */
    private String approverName;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getNcrId() { return ncrId; }
    public void setNcrId(Long ncrId) { this.ncrId = ncrId; }
    public Integer getNodeSeq() { return nodeSeq; }
    public void setNodeSeq(Integer nodeSeq) { this.nodeSeq = nodeSeq; }
    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }
    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getApproveOpinion() { return approveOpinion; }
    public void setApproveOpinion(String approveOpinion) { this.approveOpinion = approveOpinion; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
}
