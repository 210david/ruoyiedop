package com.ruoyi.fin.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 移动加权平均计价单元测试
 *
 * <p>计价是存货价值的基础，算错会导致资产负债表失真，
 * 因此入库加权与出库结转两端的边界情况全部覆盖。</p>
 *
 * @author ruoyi
 */
class FinCostEngineWeightedTest
{
    private final FinCostEngine engine = new FinCostEngine();

    private static BigDecimal bd(String s)
    {
        return new BigDecimal(s);
    }

    // ==================== 入库计价 ====================

    @Test
    @DisplayName("首次入库：新单位成本 = 入库单位成本")
    void testInbound_FirstTime()
    {
        FinCostEngine.CostResult r = engine.calcInboundWeighted(
                BigDecimal.ZERO, BigDecimal.ZERO, bd("100"), bd("10.0000"));

        assertEquals(0, bd("100").compareTo(r.getQty()), "结存数量应为 100");
        assertEquals(0, bd("10.0000").compareTo(r.getUnitCost()), "单位成本应等于入库成本");
        assertEquals(0, bd("1000.00").compareTo(r.getTotalCost()), "结存金额应为 1000");
        assertEquals(0, bd("1000.00").compareTo(r.getAmount()), "本次入库金额应为 1000");
    }

    @Test
    @DisplayName("二次入库同价：单位成本不变")
    void testInbound_SamePrice()
    {
        FinCostEngine.CostResult r1 = engine.calcInboundWeighted(
                BigDecimal.ZERO, BigDecimal.ZERO, bd("100"), bd("10.0000"));
        FinCostEngine.CostResult r2 = engine.calcInboundWeighted(
                r1.getQty(), r1.getTotalCost(), bd("50"), bd("10.0000"));

        assertEquals(0, bd("150").compareTo(r2.getQty()), "结存数量应为 150");
        assertEquals(0, bd("10.0000").compareTo(r2.getUnitCost()), "同价入库单位成本不变");
        assertEquals(0, bd("1500.00").compareTo(r2.getTotalCost()), "结存金额应为 1500");
    }

    @Test
    @DisplayName("二次入库涨价：单位成本按加权平均上升")
    void testInbound_PriceUp()
    {
        // 原有 100 件 @10 = 1000 元；新进 100 件 @20 = 2000 元
        // 加权后 = (1000+2000)/(100+100) = 15
        FinCostEngine.CostResult r = engine.calcInboundWeighted(
                bd("100"), bd("1000.00"), bd("100"), bd("20.0000"));

        assertEquals(0, bd("200").compareTo(r.getQty()), "结存数量应为 200");
        assertEquals(0, bd("15.0000").compareTo(r.getUnitCost()), "加权单位成本应为 15");
        assertEquals(0, bd("3000.00").compareTo(r.getTotalCost()), "结存金额应为 3000");
    }

    @Test
    @DisplayName("二次入库降价：单位成本按加权平均下降")
    void testInbound_PriceDown()
    {
        // 原有 100 件 @20 = 2000 元；新进 300 件 @10 = 3000 元
        // 加权后 = (2000+3000)/(100+300) = 12.5
        FinCostEngine.CostResult r = engine.calcInboundWeighted(
                bd("100"), bd("2000.00"), bd("300"), bd("10.0000"));

        assertEquals(0, bd("400").compareTo(r.getQty()));
        assertEquals(0, bd("12.5000").compareTo(r.getUnitCost()), "加权单位成本应为 12.5");
        assertEquals(0, bd("5000.00").compareTo(r.getTotalCost()));
    }

    @Test
    @DisplayName("单位成本保留 4 位小数")
    void testInbound_FourDecimal()
    {
        FinCostEngine.CostResult r = engine.calcInboundWeighted(
                bd("3"), bd("10.00"), bd("3"), bd("10.0000"));
        assertEquals(4, r.getUnitCost().scale(), "unit_cost 必须 4 位小数");
    }

    @Test
    @DisplayName("除零保护：结存与入库数量均为0时不崩")
    void testInbound_ZeroQty()
    {
        FinCostEngine.CostResult r = engine.calcInboundWeighted(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, bd("10.0000"));
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getUnitCost()), "零数量时单位成本为 0，避免除零");
    }

    // ==================== 出库结转 ====================

    @Test
    @DisplayName("出库结转：成本 = 数量 × 单位成本")
    void testOutbound_Normal()
    {
        FinCostEngine.CostResult r = engine.calcOutboundWeighted(
                bd("200"), bd("15.0000"), bd("50"));

        assertEquals(0, bd("750.00").compareTo(r.getAmount()), "出库成本应为 50×15=750");
        assertEquals(0, bd("150").compareTo(r.getQty()), "结存数量应为 150");
        assertEquals(0, bd("2250.00").compareTo(r.getTotalCost()), "结存金额应为 2250");
    }

    @Test
    @DisplayName("全部出完：结存数量与金额归零")
    void testOutbound_AllOut()
    {
        FinCostEngine.CostResult r = engine.calcOutboundWeighted(
                bd("100"), bd("10.0000"), bd("100"));
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getQty()), "全部出完数量归零");
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getTotalCost()), "全部出完金额归零");
        assertEquals(0, bd("1000.00").compareTo(r.getAmount()), "出库成本 1000");
    }

    @Test
    @DisplayName("出库超量：抛异常不允许负结存")
    void testOutbound_Exceed()
    {
        assertThrows(RuntimeException.class,
                () -> engine.calcOutboundWeighted(bd("50"), bd("10.0000"), bd("80")),
                "出库 80 > 结存 50 应拒绝");
    }

    @Test
    @DisplayName("出库数量等于结存：允许，归零")
    void testOutbound_Equal()
    {
        FinCostEngine.CostResult r = engine.calcOutboundWeighted(
                bd("50"), bd("10.0000"), bd("50"));
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getQty()));
        assertEquals(0, BigDecimal.ZERO.compareTo(r.getTotalCost()));
    }

    // ==================== 计价方法校验 ====================

    @Test
    @DisplayName("支持加权平均与先进先出")
    void testCheckMethod_Supported()
    {
        engine.checkMethod("WEIGHTED_AVG");
        engine.checkMethod("FIFO");
    }

    @Test
    @DisplayName("不支持的计价方法抛异常")
    void testCheckMethod_Unsupported()
    {
        assertThrows(RuntimeException.class,
                () -> engine.checkMethod("LIFO"), "后进先出不在支持范围");
    }
}