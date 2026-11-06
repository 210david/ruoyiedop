package com.ruoyi.pjm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目里程碑表 pjm_milestone
 */
public class PjmMilestone extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long milestoneId;
    private Long projectId;
    private String milestoneName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualDate;

    private String status;     // 0未达成 1已达成 2延期
    private Long ownerId;      // 负责人（sys_user.user_id）
    private String ownerName;
    private String description;
    private String delFlag;

    /** 关联查询字段 */
    private String projectName;

    public Long getMilestoneId() { return milestoneId; }
    public void setMilestoneId(Long milestoneId) { this.milestoneId = milestoneId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getMilestoneName() { return milestoneName; }
    public void setMilestoneName(String milestoneName) { this.milestoneName = milestoneName; }
    public Date getPlanDate() { return planDate; }
    public void setPlanDate(Date planDate) { this.planDate = planDate; }
    public Date getActualDate() { return actualDate; }
    public void setActualDate(Date actualDate) { this.actualDate = actualDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
}
