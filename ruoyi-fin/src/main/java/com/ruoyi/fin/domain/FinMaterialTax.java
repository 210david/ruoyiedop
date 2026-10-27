package com.ruoyi.fin.domain;

import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 物料税率档案 fin_material_tax
 *
 * <p>【Q4 决策】入库单价统一按含税录入，本表提供除税所需的税率。
 * 零税率物料必须显式维护 tax_rate=0，否则 FinCostEngine 会抛 10008 拒绝除税。</p>
 *
 * @author ruoyi
 */
public class FinMaterialTax extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 税率档案ID */
    private Long materialTaxId;

    /** 物料ID（对应 wms_material.material_id，只读引用） */
    private Long materialId;

    /** 物料编码 */
    private String materialCode;

    /** 税率 0.1300 = 13% */
    private BigDecimal taxRate;

    /** 单价是否含税 1含税 0不含税（Q4 统一含税，此为兼容开关） */
    private String taxIncluded;

    /** 生效日期 */
    private Date effectiveDate;

    /** 失效日期（为空长期有效） */
    private Date expireDate;

    /** 预留账套ID */
    private Long companyId;

    /** 状态 0启用 1停用 */
    private String status;

    public Long getMaterialTaxId() { return materialTaxId; }
    public void setMaterialTaxId(Long materialTaxId) { this.materialTaxId = materialTaxId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }

    public String getTaxIncluded() { return taxIncluded; }
    public void setTaxIncluded(String taxIncluded) { this.taxIncluded = taxIncluded; }

    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }

    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
