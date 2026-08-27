package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;

public class HrPosition extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long positionId;
    @NotBlank(message = "岗位编码不能为空")
    @Excel(name = "岗位编码")
    private String positionCode;
    @NotBlank(message = "岗位名称不能为空")
    @Excel(name = "岗位名称")
    private String positionName;
    @Excel(name = "岗位序列")
    private String positionSeq;
    @Excel(name = "岗级")
    private String positionLevel;
    @Excel(name = "技能等级")
    private String skillLevel;
    @Excel(name = "所属组织ID")
    private Long orgId;
    @Excel(name = "编制人数")
    private Integer headcount;
    @Excel(name = "编制管理")
    private Integer headcountEnabled;
    @Excel(name = "必须持证")
    private String requireCert;
    @Excel(name = "排序号")
    private Integer orderNum;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }
    public String getPositionCode() { return positionCode; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public String getPositionSeq() { return positionSeq; }
    public void setPositionSeq(String positionSeq) { this.positionSeq = positionSeq; }
    public String getPositionLevel() { return positionLevel; }
    public void setPositionLevel(String positionLevel) { this.positionLevel = positionLevel; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Integer getHeadcount() { return headcount; }
    public void setHeadcount(Integer headcount) { this.headcount = headcount; }
    public Integer getHeadcountEnabled() { return headcountEnabled; }
    public void setHeadcountEnabled(Integer headcountEnabled) { this.headcountEnabled = headcountEnabled; }
    public String getRequireCert() { return requireCert; }
    public void setRequireCert(String requireCert) { this.requireCert = requireCert; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
