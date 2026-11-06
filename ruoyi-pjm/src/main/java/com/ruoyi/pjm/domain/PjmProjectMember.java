package com.ruoyi.pjm.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目团队成员 pjm_project_member
 */
public class PjmProjectMember extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long memberId;
    private Long projectId;
    private Long userId;
    private String userName;
    private Long deptId;
    private String deptName;
    private String roleType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date joinDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date leaveDate;

    private java.math.BigDecimal workload;
    private Integer taskCount;
    private String delFlag;

    /** 关联查询 */
    private String projectName;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }
    public Date getJoinDate() { return joinDate; }
    public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }
    public Date getLeaveDate() { return leaveDate; }
    public void setLeaveDate(Date leaveDate) { this.leaveDate = leaveDate; }
    public java.math.BigDecimal getWorkload() { return workload; }
    public void setWorkload(java.math.BigDecimal workload) { this.workload = workload; }
    public Integer getTaskCount() { return taskCount; }
    public void setTaskCount(Integer taskCount) { this.taskCount = taskCount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
}
