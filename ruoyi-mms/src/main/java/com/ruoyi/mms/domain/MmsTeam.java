package com.ruoyi.mms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 班组 mms_team
 *
 * @author ruoyi
 */
public class MmsTeam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long teamId;

    @Excel(name = "班组编号")
    private String teamCode;

    @Excel(name = "班组名称")
    private String teamName;

    /** 所属产能单元ID */
    private Long resourceId;

    @Excel(name = "产能单元")
    private String resourceName;

    /** 班组长 */
    @Excel(name = "班组长")
    private String leader;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;

    /** 班组人数 */
    @Excel(name = "班组人数")
    private Integer memberCount;

    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    private String delFlag;

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamCode() { return teamCode; }
    public void setTeamCode(String teamCode) { this.teamCode = teamCode; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getLeader() { return leader; }
    public void setLeader(String leader) { this.leader = leader; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
