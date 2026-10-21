package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单退货审批记录 mk_order_return_approve_log
 *
 * @author ruoyi
 */
public class MkOrderReturnApproveLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long logId;

    /** 退货ID */
    private Long returnId;

    /** 操作类型（1提交审批 2审批通过 3审批驳回） */
    private String actionType;

    /** 审批意见 */
    private String approveOpinion;

    /** 审批人 */
    private String approveBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getApproveOpinion() { return approveOpinion; }
    public void setApproveOpinion(String approveOpinion) { this.approveOpinion = approveOpinion; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
}
