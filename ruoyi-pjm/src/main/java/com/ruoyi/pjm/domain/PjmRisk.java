package com.ruoyi.pjm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目风险/问题登记表 pjm_risk
 */
public class PjmRisk extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long riskId;
    private Long projectId;
    private Long taskId;
    private String riskType;          // 1风险 2问题
    private String engineeringCategory; // 工程类别(pjm_engineering_category)
    private Long riskPointId;         // 关联国家标准风险点ID
    private String riskStdCode;       // 国家标准/规范条款号(GB xxxx-xxxx)
    private String title;
    private String level;      // 1低 2中 3高 4紧急
    private String status;     // 0开放 1处理中 2已解决 3已关闭

    private Long ownerId;
    private String ownerName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date occurDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date resolveDate;

    private String solution;
    private String description;
    private String delFlag;

    /** 关联查询字段 */
    private String projectName;
    private String taskName;

    public Long getRiskId() { return riskId; }
    public void setRiskId(Long riskId) { this.riskId = riskId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getRiskType() { return riskType; }
    public void setRiskType(String riskType) { this.riskType = riskType; }
    public String getEngineeringCategory() { return engineeringCategory; }
    public void setEngineeringCategory(String engineeringCategory) { this.engineeringCategory = engineeringCategory; }
    public Long getRiskPointId() { return riskPointId; }
    public void setRiskPointId(Long riskPointId) { this.riskPointId = riskPointId; }
    public String getRiskStdCode() { return riskStdCode; }
    public void setRiskStdCode(String riskStdCode) { this.riskStdCode = riskStdCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public Date getOccurDate() { return occurDate; }
    public void setOccurDate(Date occurDate) { this.occurDate = occurDate; }
    public Date getResolveDate() { return resolveDate; }
    public void setResolveDate(Date resolveDate) { this.resolveDate = resolveDate; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
}
