package com.ruoyi.fin.engine;

import com.ruoyi.fin.domain.FinMaterialTax;
import com.ruoyi.fin.mapper.FinMaterialTaxMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 成本引擎单元测试（P2 缺口要求：成本引擎 100% 覆盖）
 *
 * <p>重点验证 Q4 决策的含税除税口径与精度保护：
 * 除税后成本乘以数量 必须等于发票不含税金额。</p>
 *
 * <p>不依赖 Mockito：工程运行于 JDK 17，Mockito 的 inline mock maker
 * 需要自附加 agent，在当前构建环境不可用（Could not self-attach to current VM）。
 * 此处用手写桩实现替代 mockito-inline。</p>
 *
 * @author ruoyi
 */
class FinCostEngineTest
{
    private static final String MAT = "MAT-0001";

    private static final BigDecimal RATE_13 = new BigDecimal("0.1300");

    private static final BigDecimal RATE_6 = new BigDecimal("0.0600");

    private FinCostEngine engine;

    /** 手写桩：模拟 tax_rate 非空时正常返回 */
    private static class StubMapper implements FinMaterialTaxMapper
    {
        private final FinMaterialTax tax = new FinMaterialTax();

        @Override
        public FinMaterialTax selectEffectiveByMaterial(String materialCode)
        {
            return tax.getTaxRate() == null ? null : tax;
        }

        @Override
        public FinMaterialTax selectEffectiveByMaterialForUpdate(String materialCode)
        {
            return selectEffectiveByMaterial(materialCode);
        }

        @Override public java.util.List<FinMaterialTax> selectTaxList(FinMaterialTax q) { return java.util.Collections.emptyList(); }
        @Override public FinMaterialTax selectTaxById(Long id) { return null; }
        @Override public int checkDuplicate(String code, java.util.Date d, Long id) { return 0; }
        @Override public int insertTax(FinMaterialTax t) { return 1; }
        @Override public int updateTax(FinMaterialTax t) { return 1; }
        @Override public int deleteTaxByIds(Long[] ids) { return 0; }
        @Override public java.util.List<String> selectTaxedMaterialCodes() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<java.util.Map<String, Object>> selectUntaxedMaterials(int limit) { return java.util.Collections.emptyList(); }
        @Override public int countEffective() { return 0; }

        void setRate(String rate)
        {
            tax.setTaxRate(new BigDecimal(rate));
        }
    }

    /** 手写桩：模拟无税率档案 */
    private static class EmptyMapper implements FinMaterialTaxMapper
    {
        @Override
        public FinMaterialTax selectEffectiveByMaterial(String materialCode)
        {
            return null;
        }

        @Override
        public FinMaterialTax selectEffectiveByMaterialForUpdate(String materialCode)
        {
            return null;
        }

        @Override public java.util.List<FinMaterialTax> selectTaxList(FinMaterialTax q) { return java.util.Collections.emptyList(); }
        @Override public FinMaterialTax selectTaxById(Long id) { return null; }
        @Override public int checkDuplicate(String code, java.util.Date d, Long id) { return 0; }
        @Override public int insertTax(FinMaterialTax t) { return 1; }
        @Override public int updateTax(FinMaterialTax t) { return 1; }
        @Override public int deleteTaxByIds(Long[] ids) { return 0; }
        @Override public java.util.List<String> selectTaxedMaterialCodes() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<java.util.Map<String, Object>> selectUntaxedMaterials(int limit) { return java.util.Collections.emptyList(); }
        @Override public int countEffective() { return 0; }
    }

    private void inject(Object mapper) throws Exception
    {
        injectInto(engine, mapper);
    }

    private void injectInto(FinCostEngine target, Object mapper) throws Exception
    {
        Field field = FinCostEngine.class.getDeclaredField("materialTaxMapper");
        field.setAccessible(true);
        field.set(target, mapper);
    }

    @BeforeEach
    void setUp() throws Exception
    {
        engine = new FinCostEngine();
        inject(new StubMapper());
    }

    @Test
    @DisplayName("13% 含税单价 113 除税得 100")
    void testExcludeTax_13Percent()
    {
        BigDecimal result = engine.toExclTax(MAT, RATE_13, new BigDecimal("113.0000"));
        assertEquals(0, new BigDecimal("100.0000").compareTo(result), "113 除以 1.13 应等于 100");
    }

    @Test
    @DisplayName("6% 含税单价 106 除税得 100")
    void testExcludeTax_6Percent()
    {
        BigDecimal result = engine.toExclTax(MAT, RATE_6, new BigDecimal("106.0000"));
        assertEquals(0, new BigDecimal("100.0000").compareTo(result));
    }

    @Test
    @DisplayName("除税结果固定 4 位小数，保留精度")
    void testExcludeTax_FourDecimalScale()
    {
        BigDecimal result = engine.toExclTax(MAT, RATE_13, new BigDecimal("100.0000"));
        assertEquals(4, result.scale(), "unit_cost 必须为 4 位小数");
    }

    @Test
    @DisplayName("零税率不做除法，直接取原值")
    void testExcludeTax_ZeroRate()
    {
        BigDecimal result = engine.toExclTax(MAT, BigDecimal.ZERO, new BigDecimal("88.8888"));
        assertEquals(0, new BigDecimal("88.8888").compareTo(result), "零税率应原样返回");
    }

    @Test
    @DisplayName("含税单价为 0 时返回 0")
    void testExcludeTax_ZeroPrice()
    {
        BigDecimal result = engine.toExclTax(MAT, RATE_13, BigDecimal.ZERO);
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }

    @Test
    @DisplayName("未维护税率档案且未显式传税率时拒绝计算，抛 10008")
    void testExcludeTax_TaxNotFound() throws Exception
    {
        FinCostEngine local = new FinCostEngine();
        injectInto(local, new EmptyMapper());

        assertThrows(RuntimeException.class,
                () -> local.toExclTax(MAT, null, new BigDecimal("100")),
                "税率档案缺失时应拒绝计算，避免默认 13% 误算");
    }

    @Test
    @DisplayName("入库校验：偏差超过 1 元阈值时抛异常")
    void testCheckTaxDiff_Exceeded()
    {
        BigDecimal qty = new BigDecimal("10");
        BigDecimal unitCost = new BigDecimal("99.0000");
        BigDecimal invoiceAmt = new BigDecimal("1000.00");
        assertThrows(RuntimeException.class,
                () -> engine.checkTaxDiff(MAT, qty, unitCost, invoiceAmt),
                "计算 990.00 与发票 1000.00 差 10 元，应超阈值抛异常");
    }

    @Test
    @DisplayName("入库校验：偏差在 1 元内应通过")
    void testCheckTaxDiff_Pass()
    {
        BigDecimal qty = new BigDecimal("10");
        BigDecimal unitCost = new BigDecimal("100.0000");
        BigDecimal invoiceAmt = new BigDecimal("1000.30");
        engine.checkTaxDiff(MAT, qty, unitCost, invoiceAmt);
    }

    @Test
    @DisplayName("入库校验：发票金额为空时跳过")
    void testCheckTaxDiff_SkipWhenInvoiceNull()
    {
        engine.checkTaxDiff(MAT, new BigDecimal("10"), new BigDecimal("1.0000"), null);
    }
}
