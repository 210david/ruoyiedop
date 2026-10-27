package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 商机阶段变更日志 mk_opportunity_stage_log
 *
 * @author ruoyi
 */
public class MkOpportunityStageLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 商机ID */
    private Long opportunityId;

    /** 商机名称（关联查询） */
    private String opportunityName;

    /** 变更前阶段编码 */
    private String fromStageCode;

    /** 变更前阶段名称 */
    private String fromStageName;

    /** 变更后阶段编码 */
    private String toStageCode;

    /** 变更后阶段名称 */
    private String toStageName;

    /** 操作类型（advance推进 / retreat退回 / win赢单 / lose输单 / reopen重开） */
    private String actionType;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public String getFromStageCode() { return fromStageCode; }
    public void setFromStageCode(String fromStageCode) { this.fromStageCode = fromStageCode; }
    public String getFromStageName() { return fromStageName; }
    public void setFromStageName(String fromStageName) { this.fromStageName = fromStageName; }
    public String getToStageCode() { return toStageCode; }
    public void setToStageCode(String toStageCode) { this.toStageCode = toStageCode; }
    public String getToStageName() { return toStageName; }
    public void setToStageName(String toStageName) { this.toStageName = toStageName; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getOperateTime() { return operateTime; }
    public void setOperateTime(Date operateTime) { this.operateTime = operateTime; }
}
