package com.ruoyi.qms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 缺陷代码表 qms_defect_code
 *
 * @author ruoyi
 */
public class QmsDefectCode extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 缺陷ID */
    private Long defectId;

    /** 缺陷代码 */
    @Excel(name = "缺陷代码")
    private String defectCode;

    /** 缺陷名称 */
    @Excel(name = "缺陷名称")
    private String defectName;

    /** 缺陷等级（1致命 2严重 3一般 4轻微） */
    @Excel(name = "缺陷等级", readConverterExp = "1=致命,2=严重,3=一般,4=轻微")
    private String defectLevel;

    /** 缺陷分类 */
    @Excel(name = "缺陷分类")
    private String defectCategory;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getDefectId() { return defectId; }
    public void setDefectId(Long defectId) { this.defectId = defectId; }
    public String getDefectCode() { return defectCode; }
    public void setDefectCode(String defectCode) { this.defectCode = defectCode; }
    public String getDefectName() { return defectName; }
    public void setDefectName(String defectName) { this.defectName = defectName; }
    public String getDefectLevel() { return defectLevel; }
    public void setDefectLevel(String defectLevel) { this.defectLevel = defectLevel; }
    public String getDefectCategory() { return defectCategory; }
    public void setDefectCategory(String defectCategory) { this.defectCategory = defectCategory; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
