package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

public class HrOrganization extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long orgId;
    @NotBlank(message = "组织编码不能为空")
    @Excel(name = "组织编码")
    private String orgCode;
    @NotBlank(message = "组织名称不能为空")
    @Excel(name = "组织名称")
    private String orgName;
    @Excel(name = "父组织ID")
    private Long parentId;
    @Excel(name = "排序号")
    private Integer orderNum;
    @Excel(name = "组织层级")
    private Integer orgLevel;
    @Excel(name = "负责人ID")
    private Long leaderId;
    @Excel(name = "负责人姓名")
    private String leaderName;
    @Excel(name = "成本中心")
    private Integer costCenter;
    @Excel(name = "考勤归属")
    private Integer attendanceOrg;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getOrgCode() { return orgCode; }
    public void setOrgCode(String orgCode) { this.orgCode = orgCode; }
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public Integer getOrgLevel() { return orgLevel; }
    public void setOrgLevel(Integer orgLevel) { this.orgLevel = orgLevel; }
    public Long getLeaderId() { return leaderId; }
    public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public Integer getCostCenter() { return costCenter; }
    public void setCostCenter(Integer costCenter) { this.costCenter = costCenter; }
    public Integer getAttendanceOrg() { return attendanceOrg; }
    public void setAttendanceOrg(Integer attendanceOrg) { this.attendanceOrg = attendanceOrg; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
