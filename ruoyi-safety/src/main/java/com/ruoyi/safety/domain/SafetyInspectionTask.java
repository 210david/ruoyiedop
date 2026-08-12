package com.ruoyi.safety.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SafetyInspectionTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;
    @Excel(name = "任务编号")
    private String taskCode;
    @NotBlank(message = "任务类型不能为空")
    @Excel(name = "任务类型")
    private String taskType;
    @Excel(name = "任务名称")
    private String taskName;
    private Long riskPointId;
    @NotNull(message = "计划日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划日期", width = 120)
    private Date planDate;
    @NotNull(message = "执行人不能为空")
    private Long executorId;
    @Excel(name = "执行人")
    private String executorName;
    @Excel(name = "任务状态", readConverterExp = "0=待执行,1=执行中,2=已完成,3=已作废")
    private String taskStatus;
    @Excel(name = "检查结果", readConverterExp = "1=正常,2=有隐患")
    private String inspectResult;
    @Excel(name = "隐患数")
    private Integer hazardCount;
    @Excel(name = "检查时间", width = 160)
    private Date inspectTime;
    private Long enterpriseId;
    private String delFlag;

    /** 关联隐患列表（执行反馈时填写） */
    private List<SafetyHazard> hazardList;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Long getRiskPointId() { return riskPointId; }
    public void setRiskPointId(Long riskPointId) { this.riskPointId = riskPointId; }
    public Date getPlanDate() { return planDate; }
    public void setPlanDate(Date planDate) { this.planDate = planDate; }
    public Long getExecutorId() { return executorId; }
    public void setExecutorId(Long executorId) { this.executorId = executorId; }
    public String getExecutorName() { return executorName; }
    public void setExecutorName(String executorName) { this.executorName = executorName; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public String getInspectResult() { return inspectResult; }
    public void setInspectResult(String inspectResult) { this.inspectResult = inspectResult; }
    public Integer getHazardCount() { return hazardCount; }
    public void setHazardCount(Integer hazardCount) { this.hazardCount = hazardCount; }
    public Date getInspectTime() { return inspectTime; }
    public void setInspectTime(Date inspectTime) { this.inspectTime = inspectTime; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<SafetyHazard> getHazardList() { return hazardList; }
    public void setHazardList(List<SafetyHazard> hazardList) { this.hazardList = hazardList; }
}