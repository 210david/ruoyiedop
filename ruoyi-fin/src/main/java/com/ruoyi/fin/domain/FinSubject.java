package com.ruoyi.fin.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 会计科目 fin_subject
 *
 * <p>科目树最多 4 级；is_leaf=0 的非明细科目禁止直接录入凭证。
 * 停用科目用 status='1' 而非删除，保证历史凭证可追溯。</p>
 *
 * <p>【导出规范 4.3.11】日期类字段无、字典字段已配 readConverterExp。</p>
 *
 * @author ruoyi
 */
public class FinSubject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 科目ID */
    private Long subjectId;

    /** 科目编码 */
    @Excel(name = "科目编码")
    private String subjectCode;

    /** 科目名称 */
    @Excel(name = "科目名称")
    private String subjectName;

    /** 上级科目ID */
    private Long parentId;

    /** 上级科目编码（冗余，便于树查询） */
    @Excel(name = "上级科目编码")
    private String parentCode;

    /** 级次 1-4 */
    @Excel(name = "级次")
    private Integer level;

    /** 类别 ASSET/LIABILITY/EQUITY/COST/INCOME */
    @Excel(name = "科目类别", readConverterExp = "ASSET=资产,LIABILITY=负债,EQUITY=所有者权益,COST=成本,INCOME=损益")
    private String category;

    /** 余额方向 DEBIT/CREDIT */
    @Excel(name = "余额方向", readConverterExp = "DEBIT=借,CREDIT=贷")
    private String direction;

    /** 是否明细科目 0否 1是 */
    @Excel(name = "是否明细", readConverterExp = "0=非明细,1=明细科目")
    private String isLeaf;

    /** 是否现金等价物 */
    @Excel(name = "现金等价物", readConverterExp = "0=否,1=是")
    private String isCashEquiv;

    /** 现金流分类 OPERATING/INVESTING/FINANCING */
    @Excel(name = "现金流分类", readConverterExp = "OPERATING=经营活动,INVESTING=投资活动,FINANCING=筹资活动")
    private String cashFlowCategory;

    /** 辅助核算类型，逗号分隔 C/S/D/P */
    @Excel(name = "辅助核算")
    private String auxTypes;

    /** 预留账套ID */
    private Long companyId;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态 0正常 1停用 */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public String getIsLeaf() { return isLeaf; }
    public void setIsLeaf(String isLeaf) { this.isLeaf = isLeaf; }

    public String getIsCashEquiv() { return isCashEquiv; }
    public void setIsCashEquiv(String isCashEquiv) { this.isCashEquiv = isCashEquiv; }

    public String getCashFlowCategory() { return cashFlowCategory; }
    public void setCashFlowCategory(String cashFlowCategory) { this.cashFlowCategory = cashFlowCategory; }

    public String getAuxTypes() { return auxTypes; }
    public void setAuxTypes(String auxTypes) { this.auxTypes = auxTypes; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
