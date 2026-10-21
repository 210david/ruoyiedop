package com.ruoyi.fin.engine;

import com.ruoyi.fin.domain.FinMaterialTax;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinMaterialTaxMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 成本引擎（含税除税 + 精度保护）。
 *
 * <p>【Q4 决策】入库单价统一按含税价录入，本引擎负责还原为不含税单位成本。</p>
 *
 * <p>除税公式：</p>
 * <pre>
 *   unit_cost_excl_tax = unit_price / (1 + tax_rate)
 * </pre>
 *
 * <p>关键约束：</p>
 * <ol>
 *   <li>{@code unit_cost} 保留 4 位小数（{@code DECIMAL(18,4)}），缓解除法精度损失</li>
 *   <li>零税率物料必须显式维护 tax_rate=0，引擎不做除法直接取原值，
 *       未维护档案时抛 10008，避免默认 13% 误算</li>
 *   <li>税率来源优先级：fin_material_tax &gt; pms_invoice 反推 &gt; 默认 13%</li>
 * </ol>
 *
 * @author ruoyi
 */
@Component
public class FinCostEngine
{
    private static final Logger log = LoggerFactory.getLogger(FinCostEngine.class);

    /** 成本单价精度：4 位小数 */
    public static final int UNIT_COST_SCALE = 4;

    /** 默认税率（物料未维护档案且无发票可反推时） */
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.13");

    /** 除税偏差告警阈值（元），超出则抛 10013 */
    private static final BigDecimal DIFF_THRESHOLD = new BigDecimal("1.00");

    @Autowired
    private FinMaterialTaxMapper materialTaxMapper;

    /**
     * 含税单价转不含税成本单价。
     *
     * @param materialCode 物料编码（用于查税率档案）
     * @param taxRate      税率；传 null 时自动按优先级查档案
     * @param unitPriceIn  含税单价
     * @return 不含税单位成本，4 位小数
     */
    public BigDecimal toExclTax(String materialCode, BigDecimal taxRate, BigDecimal unitPriceIn)
    {
        if (unitPriceIn == null || unitPriceIn.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO.setScale(UNIT_COST_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal rate = resolveTaxRate(materialCode, taxRate);
        if (rate.compareTo(BigDecimal.ZERO) == 0)
        {
            // 零税率：不做除法，直接取原值
            return unitPriceIn.setScale(UNIT_COST_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal divisor = BigDecimal.ONE.add(rate);
        return unitPriceIn.divide(divisor, UNIT_COST_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 解析税率。
     *
     * @param materialCode 物料编码
     * @param taxRate      显式传入的税率，可为 null
     * @return 税率
     */
    private BigDecimal resolveTaxRate(String materialCode, BigDecimal taxRate)
    {
        if (taxRate != null)
        {
            return taxRate;
        }
        FinMaterialTax tax = materialTaxMapper.selectEffectiveByMaterial(materialCode);
        if (tax == null || tax.getTaxRate() == null)
        {
            throw FinException.materialTaxNotFound(materialCode);
        }
        return tax.getTaxRate();
    }

    /**
     * 入库成本校验：不含税成本 × 数量 应约等于发票不含税金额。
     *
     * @param materialCode   物料编码
     * @param qty            数量
     * @param unitCostExcl   不含税单位成本
     * @param invoiceAmtExcl 发票不含税金额（可为空，为空则跳过校验）
     */
    public void checkTaxDiff(String materialCode, BigDecimal qty, BigDecimal unitCostExcl,
                             BigDecimal invoiceAmtExcl)
    {
        if (invoiceAmtExcl == null || invoiceAmtExcl.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        BigDecimal computed = qty.multiply(unitCostExcl).setScale(2, RoundingMode.HALF_UP);
        BigDecimal diff = computed.subtract(invoiceAmtExcl).abs();
        if (diff.compareTo(DIFF_THRESHOLD) > 0)
        {
            log.warn("物料 {} 除税成本与发票金额偏差 {} 元", materialCode, diff);
            throw FinException.costTaxDiffExceeded(materialCode, diff.toPlainString());
        }
    }

    // ==================== 移动加权平均计价 ====================

    /** 计价方法：加权平均 */
    public static final String WEIGHTED_AVG = "WEIGHTED_AVG";

    /** 计价方法：先进先出 */
    public static final String FIFO = "FIFO";

    /**
     * 入库计价（移动加权平均法）。
     *
     * <p>公式：新单位成本 = (原结存金额 + 本次入库金额) / (原结存数量 + 本次入库数量)</p>
     *
     * @param oldQty     原结存数量
     * @param oldCost    原结存金额
     * @param inQty      本次入库数量
     * @param inUnitCost 本次入库不含税单位成本
     * @return 计价结果
     */
    public CostResult calcInboundWeighted(BigDecimal oldQty, BigDecimal oldCost,
                                          BigDecimal inQty, BigDecimal inUnitCost)
    {
        BigDecimal oldQtySafe = nz(oldQty);
        BigDecimal oldCostSafe = nz(oldCost);
        BigDecimal inQtySafe = nz(inQty);

        BigDecimal newQty = oldQtySafe.add(inQtySafe);
        BigDecimal inAmount = inQtySafe.multiply(nz(inUnitCost)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newTotalCost = oldCostSafe.add(inAmount);

        BigDecimal newUnitCost = newQty.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO.setScale(UNIT_COST_SCALE, RoundingMode.HALF_UP)
                : newTotalCost.divide(newQty, UNIT_COST_SCALE, RoundingMode.HALF_UP);

        return new CostResult(newQty, newUnitCost, newTotalCost, inAmount);
    }

    /**
     * 出库结转成本（移动加权平均法）。
     *
     * <p>出库成本 = 出库数量 × 当前单位成本。数量不足直接拒绝，不允许负结存。</p>
     *
     * @param curQty      当前结存数量
     * @param curUnitCost 当前单位成本
     * @param outQty      出库数量
     * @return 计价结果，getAmount() 为本次结转成本
     */
    public CostResult calcOutboundWeighted(BigDecimal curQty, BigDecimal curUnitCost,
                                           BigDecimal outQty)
    {
        BigDecimal curQtySafe = nz(curQty);
        BigDecimal curCost = nz(curUnitCost);
        BigDecimal outQtySafe = nz(outQty);

        if (outQtySafe.compareTo(curQtySafe) > 0)
        {
            throw FinException.costQtyNotEnough("-",
                    curQtySafe.toPlainString(), outQtySafe.toPlainString());
        }

        BigDecimal carryCost = outQtySafe.multiply(curCost).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newQty = curQtySafe.subtract(outQtySafe);
        BigDecimal newTotalCost = curQtySafe.multiply(curCost)
                .subtract(carryCost).setScale(2, RoundingMode.HALF_UP);

        return new CostResult(newQty, curCost, newTotalCost, carryCost);
    }

    /**
     * 校验计价方法是否支持。
     *
     * @param method 计价方法
     */
    public void checkMethod(String method)
    {
        if (!WEIGHTED_AVG.equals(method) && !FIFO.equals(method))
        {
            throw FinException.costMethodUnsupported(method);
        }
    }

    private static BigDecimal nz(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }

    /**
     * 计价结果。
     */
    public static class CostResult
    {
        private final BigDecimal qty;
        private final BigDecimal unitCost;
        private final BigDecimal totalCost;
        private final BigDecimal amount;

        CostResult(BigDecimal qty, BigDecimal unitCost, BigDecimal totalCost, BigDecimal amount)
        {
            this.qty = qty;
            this.unitCost = unitCost;
            this.totalCost = totalCost;
            this.amount = amount;
        }

        public BigDecimal getQty() { return qty; }

        public BigDecimal getUnitCost() { return unitCost; }

        public BigDecimal getTotalCost() { return totalCost; }

        public BigDecimal getAmount() { return amount; }
    }
}
