package com.ruoyi.fin.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 成本结转流水 fin_cost_carry_forward
 *
 * <p>入库计价与出库结转均记入本表，是总账「借主营业务成本 / 贷库存商品」
 * 与「借库存商品 / 贷原材料」取数的唯一来源。</p>
 *
 * <p>【幂等】唯一键 uk_cf_source(source_module, source_type, source_id, material_id, del_flag)
 * 保证同一出入库明细不会重复结转。</p>
 *
 * @author ruoyi
 */
public class FinCostCarryForward extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 来源类型：入库 */
    public static final String TYPE_INBOUND = "inbound";

    /** 来源类型：出库 */
    public static final String TYPE_OUTBOUND = "outbound";

    /** 来源类型：销售退货 */
    public static final String TYPE_SALE_RETURN = "sale_return";

    /** 来源类型：采购退货 */
    public static final String TYPE_PURCHASE_RETURN = "purchase_return";

    /** 来源类型：报废 */
    public static final String TYPE_SCRAP = "scrap";

    /** 结转目标：销售成本 */
    public static final String TARGET_SALES_COST = "SALES_COST";

    /** 结转目标：生产成本 */
    public static final String TARGET_PROD_COST = "PROD_COST";

    /** 结转目标：费用 */
    public static final String TARGET_EXPENSE = "EXPENSE";

    /** 结转ID */
    private Long cfId;

    /** 结转单号 */
    @Excel(name = "结转单号")
    private String cfNo;

    /** 预留账套ID */
    private Long companyId;

    /** 结转日期 */
    private Date costDate;

    /** 会计期间 */
    private String period;

    /** 来源模块 wms/mms/mk */
    private String sourceModule;

    /** 来源类型 */
    private String sourceType;

    /** 来源单据明细ID */
    private Long sourceId;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 结转数量（正数，方向由 target_type 决定） */
    @Excel(name = "数量")
    private BigDecimal qty;

    /** 单位成本 */
    @Excel(name = "单位成本")
    private BigDecimal unitCost;

    /** 结转金额 */
    @Excel(name = "结转金额")
    private BigDecimal totalCost;

    /** 结转目标类型 */
    private String targetType;

    /** 结转目标键（如订单号） */
    private String targetKey;

    /** 关联凭证ID */
    private Long voucherId;

    public Long getCfId() { return cfId; }
    public void setCfId(Long cfId) { this.cfId = cfId; }

    public String getCfNo() { return cfNo; }
    public void setCfNo(String cfNo) { this.cfNo = cfNo; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public Date getCostDate() { return costDate; }
    public void setCostDate(Date costDate) { this.costDate = costDate; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetKey() { return targetKey; }
    public void setTargetKey(String targetKey) { this.targetKey = targetKey; }

    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }
}