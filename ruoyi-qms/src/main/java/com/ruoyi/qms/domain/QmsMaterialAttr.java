package com.ruoyi.qms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 物料质量属性表 qms_material_attr
 *
 * @author ruoyi
 */
public class QmsMaterialAttr extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 属性ID */
    private Long attrId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码（冗余） */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称（冗余） */
    @Excel(name = "物料名称")
    private String materialName;

    /** 检验类型（多选逗号分隔：IQC,IPQC,FQC,OQC） */
    @Excel(name = "检验类型")
    private String inspectTypes;

    /** 行业类型（字典 qms_industry_type） */
    @Excel(name = "行业类型", readConverterExp = "1=新能源材料,2=绿色铝加工,3=精细化工,4=先进装备,5=高原食品")
    private String industryType;

    /** 是否免检（0否 1是） */
    @Excel(name = "是否免检", readConverterExp = "0=否,1=是")
    private String isExempt;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getAttrId() { return attrId; }
    public void setAttrId(Long attrId) { this.attrId = attrId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getInspectTypes() { return inspectTypes; }
    public void setInspectTypes(String inspectTypes) { this.inspectTypes = inspectTypes; }
    public String getIndustryType() { return industryType; }
    public void setIndustryType(String industryType) { this.industryType = industryType; }
    public String getIsExempt() { return isExempt; }
    public void setIsExempt(String isExempt) { this.isExempt = isExempt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
