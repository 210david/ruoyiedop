package com.ruoyi.safety.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyArea extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long areaId;
    private Long parentId;
    private String ancestors;
    @Excel(name = "区域编码")
    private String areaCode;
    @Excel(name = "区域名称")
    private String areaName;
    @Excel(name = "节点类型", readConverterExp = "1=厂区,2=车间,3=区域")
    private String nodeType;
    private Long enterpriseId;
    @Excel(name = "区域类型")
    private String areaType;
    private Long safetyPersonId;
    @Excel(name = "安全责任人")
    private String safetyPersonName;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getAncestors() { return ancestors; }
    public void setAncestors(String ancestors) { this.ancestors = ancestors; }
    public String getAreaCode() { return areaCode; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }
    public Long getSafetyPersonId() { return safetyPersonId; }
    public void setSafetyPersonId(Long safetyPersonId) { this.safetyPersonId = safetyPersonId; }
    public String getSafetyPersonName() { return safetyPersonName; }
    public void setSafetyPersonName(String safetyPersonName) { this.safetyPersonName = safetyPersonName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}