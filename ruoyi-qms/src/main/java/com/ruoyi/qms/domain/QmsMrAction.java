package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsMrAction extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long actionId;
    private Long mrId;
    private String mrNo;
    @Excel(name = "改进项描述") private String actionDesc;
    private String actionType;
    private Long refId;
    private String refNo;
    private Long respDeptId;
    @Excel(name = "责任部门") private String respDeptName;
    @Excel(name = "责任人") private String responsiblePerson;
    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "计划完成日期", dateFormat = "yyyy-MM-dd") private Date planCloseDate;
    @JsonFormat(pattern = "yyyy-MM-dd") private Date actualCloseDate;
    private String actionResult;
    @Excel(name = "状态") private String actionStatus;
    private String delFlag;

    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    public Long getMrId() { return mrId; }
    public void setMrId(Long mrId) { this.mrId = mrId; }
    public String getMrNo() { return mrNo; }
    public void setMrNo(String mrNo) { this.mrNo = mrNo; }
    public String getActionDesc() { return actionDesc; }
    public void setActionDesc(String actionDesc) { this.actionDesc = actionDesc; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public Long getRefId() { return refId; }
    public void setRefId(Long refId) { this.refId = refId; }
    public String getRefNo() { return refNo; }
    public void setRefNo(String refNo) { this.refNo = refNo; }
    public Long getRespDeptId() { return respDeptId; }
    public void setRespDeptId(Long respDeptId) { this.respDeptId = respDeptId; }
    public String getRespDeptName() { return respDeptName; }
    public void setRespDeptName(String respDeptName) { this.respDeptName = respDeptName; }
    public String getResponsiblePerson() { return responsiblePerson; }
    public void setResponsiblePerson(String responsiblePerson) { this.responsiblePerson = responsiblePerson; }
    public Date getPlanCloseDate() { return planCloseDate; }
    public void setPlanCloseDate(Date planCloseDate) { this.planCloseDate = planCloseDate; }
    public Date getActualCloseDate() { return actualCloseDate; }
    public void setActualCloseDate(Date actualCloseDate) { this.actualCloseDate = actualCloseDate; }
    public String getActionResult() { return actionResult; }
    public void setActionResult(String actionResult) { this.actionResult = actionResult; }
    public String getActionStatus() { return actionStatus; }
    public void setActionStatus(String actionStatus) { this.actionStatus = actionStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
