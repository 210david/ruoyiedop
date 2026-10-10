package com.ruoyi.fin.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * 存货跌价准备 fin_inventory_valuation
 *
 * <p><b>会计含义</b>：存货成本高于可变现净值时计提跌价准备，
 * 借「资产减值损失」贷「存货跌价准备」，使存货账面价值回归可变现净值。</p>
 *
 * <p><b>可变现净值</b> = 估计售价 - 估计完工成本 - 估计销售费用 - 相关税费。
 * 简化口径下按「成本 × （1 - 跌价比例）」估算，由财务按行业与物料特性定比例。</p>
 *
 * <p><b>粒度</b>：按「会计期间 + 物料」计提，不按仓库维度
 * （计提准备是企业整体的存货跌价估计，不宜细化到单个仓库）。</p>
 *
 * @author ruoyi
 */
public class FinInventoryValuation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 存货跌价记录ID */
    private Long valId;

    /** 预留账套ID */
    private Long companyId;

    /** 会计期间 */
    @Excel(name = "会计期间")
    private String period;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称（冗余） */
    @Excel(name = "物料名称")
    private String materialName;

    /** 仓库ID（计提基准，非计提粒度） */
    private Long warehouseId;

    /** 结存数量 */
    @Excel(name = "结存数量")
    private BigDecimal qty;

    /** 单位成本（4 位小数） */
    @Excel(name = "单位成本")
    private BigDecimal unitCost;

    /** 存货成本（结存价值） */
    @Excel(name = "存货成本")
    private BigDecimal totalValue;

    /** 可变现净值（运行时计算，不入库） */
    private BigDecimal netValue;

    /**
     * 跌价比例（<b>运行时字段，不入库</b>）。
     *
     * <p>表结构无此列，用于计算本次计提金额：
     * {@code 计提 = 成本 × 跌价比例}。计提完成后只落计提金额，
     * 比例由财务每次计提时重新指定。</p>
     */
    @Excel(name = "跌价比例")
    private BigDecimal provisionRatio;

    /** 本期计提金额 */
    @Excel(name = "计提金额")
    private BigDecimal provisionAmount;

    public Long getValId() { return valId; }
    public void setValId(Long valId) { this.valId = valId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getNetValue() { return netValue; }
    public void setNetValue(BigDecimal netValue) { this.netValue = netValue; }

    public BigDecimal getProvisionRatio() { return provisionRatio; }
    public void setProvisionRatio(BigDecimal provisionRatio) { this.provisionRatio = provisionRatio; }

    public BigDecimal getProvisionAmount() { return provisionAmount; }
    public void setProvisionAmount(BigDecimal provisionAmount) { this.provisionAmount = provisionAmount; }
}