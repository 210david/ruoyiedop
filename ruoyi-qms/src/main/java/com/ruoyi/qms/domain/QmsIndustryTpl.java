package com.ruoyi.qms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 行业模板配置表 qms_industry_tpl
 *
 * @author ruoyi
 */
public class QmsIndustryTpl extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long tplId;

    /** 模板编码 */
    @Excel(name = "模板编码")
    private String tplCode;

    /** 模板名称 */
    @Excel(name = "模板名称")
    private String tplName;

    /** 行业类型（字典 qms_industry_type） */
    @Excel(name = "行业类型", readConverterExp = "1=新能源材料,2=绿色铝加工,3=精细化工,4=先进装备,5=高原食品")
    private String industryType;

    /** 是否强制审计追踪（0否 1是） */
    @Excel(name = "强制审计", readConverterExp = "0=否,1=是")
    private String auditForce;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getTplId() { return tplId; }
    public void setTplId(Long tplId) { this.tplId = tplId; }
    public String getTplCode() { return tplCode; }
    public void setTplCode(String tplCode) { this.tplCode = tplCode; }
    public String getTplName() { return tplName; }
    public void setTplName(String tplName) { this.tplName = tplName; }
    public String getIndustryType() { return industryType; }
    public void setIndustryType(String industryType) { this.industryType = industryType; }
    public String getAuditForce() { return auditForce; }
    public void setAuditForce(String auditForce) { this.auditForce = auditForce; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
