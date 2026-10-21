package com.ruoyi.fin.engine;

import com.ruoyi.fin.domain.FinSubject;
import com.ruoyi.fin.domain.FinVoucherEntry;
import com.ruoyi.fin.domain.FinVoucherRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 凭证规则引擎单元测试
 *
 * <p><b>重点验证借贷平衡拦截</b>—— 2026-10-09 实测发现 fin_voucher_rule
 * 中采购发票的 3 条规则方向全反，若无此拦截，引擎会生成借贷颠倒的凭证
 * 并流入账簿，污染后续所有报表。</p>
 *
 * @author ruoyi
 */
class FinVoucherEngineTest
{
    private final FinVoucherEngine engine = new FinVoucherEngine();

    private static BigDecimal bd(String s)
    {
        return new BigDecimal(s);
    }

    private static FinVoucherRule rule(String key, String account, String dir, String expr)
    {
        FinVoucherRule r = new FinVoucherRule();
        r.setRuleKey(key);
        r.setRuleName(key);
        r.setAccountCode(account);
        r.setDirection(dir);
        r.setAmountExpr(expr);
        return r;
    }

    private static Map<String, BigDecimal> amounts(String total, String excl, String tax)
    {
        Map<String, BigDecimal> m = new HashMap<>();
        m.put("total_amount", bd(total));
        m.put("amount_excl_tax", bd(excl));
        m.put("tax_amount", bd(tax));
        return m;
    }


    /**
     * 构造真实科目方向的 FinSubject（模拟 fin_subject 表的登记）。
     * direction 为科目本身的余额方向：DEBIT=资产/成本/费用，CREDIT=负债/权益/收入。
     */
    private static FinSubject subject(String code, String name, String dir)
    {
        FinSubject s = new FinSubject();
        s.setSubjectCode(code);
        s.setSubjectName(name);
        s.setDirection(dir);
        return s;
    }

    /** 真实科目方向映射（与 fin_subject 表一致） */
    private static Map<String, FinSubject> realSubjects()
    {
        Map<String, FinSubject> m = new HashMap<>();
        m.put("1001", subject("1001", "库存现金", "DEBIT"));
        m.put("1002", subject("1002", "银行存款", "DEBIT"));
        m.put("1122", subject("1122", "应收账款", "DEBIT"));
        m.put("1403", subject("1403", "原材料", "DEBIT"));
        m.put("2202", subject("2202", "应付账款", "CREDIT"));
        m.put("2211", subject("2211", "应付职工薪酬", "CREDIT"));
        m.put("22210101", subject("22210101", "进项税额", "DEBIT"));
        m.put("6001", subject("6001", "主营业务收入", "CREDIT"));
        return m;
    }

    // ==================== 正常场景 ====================

    @Test
    @DisplayName("采购发票规则：借原材料+进项税 / 贷应付账款，平衡")
    void testPurchaseInvoice_Balanced()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        rules.add(rule("mat", "1403", "DEBIT", "amount_excl_tax"));
        rules.add(rule("tax", "22210101", "DEBIT", "tax_amount"));
        rules.add(rule("ap", "2202", "CREDIT", "total_amount"));

        // 884955.75 + 130000.00 = 1014955.75
        List<FinVoucherEntry> entries =
                engine.buildEntries(rules, amounts("1014955.75", "884955.75", "130000.00"), realSubjects());

        assertEquals(3, entries.size(), "应生成 3 行分录");
        BigDecimal debit = entries.stream()
                .filter(e -> "DEBIT".equals(e.getDirection()))
                .map(FinVoucherEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal credit = entries.stream()
                .filter(e -> "CREDIT".equals(e.getDirection()))
                .map(FinVoucherEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, debit.subtract(credit).compareTo(BigDecimal.ZERO), "借贷须平衡");
        assertEquals(0, bd("1014955.75").compareTo(credit), "贷方应为价税合计");
    }

    @Test
    @DisplayName("销售发货规则：借应收账款 / 贷主营业务收入，平衡")
    void testShipment_Balanced()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        rules.add(rule("ar", "1122", "DEBIT", "total_amount"));
        rules.add(rule("rev", "6001", "CREDIT", "total_amount"));

        List<FinVoucherEntry> entries =
                engine.buildEntries(rules, amounts("1000.00", "1000.00", "0"), realSubjects());
        assertEquals(2, entries.size());
        assertEquals(0, bd("1000.00").compareTo(entries.get(0).getAmount()));
    }

    @Test
    @DisplayName("金额为0的分录跳过（如免税无进项税）")
    void testZeroAmountSkipped()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        rules.add(rule("mat", "1403", "DEBIT", "amount_excl_tax"));
        rules.add(rule("tax", "22210101", "DEBIT", "tax_amount"));   // 税额为 0
        rules.add(rule("ap", "2202", "CREDIT", "total_amount"));

        List<FinVoucherEntry> entries =
                engine.buildEntries(rules, amounts("100.00", "100.00", "0.00"), realSubjects());
        assertEquals(2, entries.size(), "税额为0的进项税行应跳过");
    }

    @Test
    @DisplayName("规则组金额全为0时返回空列表")
    void testAllZeroAmount()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        rules.add(rule("ar", "1122", "DEBIT", "total_amount"));
        rules.add(rule("rev", "6001", "CREDIT", "total_amount"));

        List<FinVoucherEntry> entries =
                engine.buildEntries(rules, amounts("0.00", "0.00", "0.00"), realSubjects());
        assertTrue(entries.isEmpty(), "全 0 金额应返回空列表");
    }

    // ==================== 核心防线：借贷不平必须拦截 ====================

    @Test
    @DisplayName("【核心】规则方向配反时必须拒绝生成（2026-10-09 实际数据缺陷的回归防护）")
    void testUnbalancedDirection_Rejected()
    {
        // 这正是修复前的真实配置：应付账款记借、原材料记贷
        List<FinVoucherRule> rules = new ArrayList<>();
        rules.add(rule("ap", "2202", "DEBIT", "total_amount"));
        rules.add(rule("mat", "1403", "CREDIT", "amount_excl_tax"));
        rules.add(rule("tax", "22210101", "CREDIT", "tax_amount"));

        // 必须抛异常，而不是生成错账
        assertThrows(RuntimeException.class,
                () -> engine.buildEntries(rules, amounts("1014955.75", "884955.75", "130000.00"), realSubjects()),
                "借贷不平时必须拒绝生成凭证");
    }

    @Test
    @DisplayName("【核心】缺少对应分录导致不平，必须拒绝")
    void testMissingEntry_Rejected()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        // 只有借方没有贷方
        rules.add(rule("mat", "1403", "DEBIT", "total_amount"));

        assertThrows(RuntimeException.class,
                () -> engine.buildEntries(rules, amounts("100.00", "100.00", "0"), realSubjects()),
                "单边凭证必须拒绝");
    }

    @Test
    @DisplayName("【核心】金额表达式错误导致不平，必须拒绝")
    void testWrongExpression_Rejected()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        // 借方用了不含税，贷方用了价税合计，差额是税额 → 不平
        rules.add(rule("mat", "1403", "DEBIT", "amount_excl_tax"));
        rules.add(rule("ap", "2202", "CREDIT", "total_amount"));

        assertThrows(RuntimeException.class,
                () -> engine.buildEntries(rules, amounts("1014955.75", "884955.75", "130000.00"), realSubjects()),
                "表达式配错导致的不平必须拦截");
    }

    @Test
    @DisplayName("空规则列表返回空分录，不报错")
    void testEmptyRules()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        assertTrue(engine.buildEntries(rules, amounts("100", "100", "0"), realSubjects()).isEmpty());
    }

    @Test
    @DisplayName("null 规则列表返回空分录，不 NPE")
    void testNullRules()
    {
        assertTrue(engine.buildEntries(null, amounts("100", "100", "0"), null).isEmpty());
    }

    @Test
    @DisplayName("分录行号连续编号，从1 开始")
    void testLineNoSequential()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        rules.add(rule("mat", "1403", "DEBIT", "amount_excl_tax"));
        rules.add(rule("tax", "22210101", "DEBIT", "tax_amount"));
        rules.add(rule("ap", "2202", "CREDIT", "total_amount"));

        List<FinVoucherEntry> entries =
                engine.buildEntries(rules, amounts("1014955.75", "884955.75", "130000.00"), realSubjects());
        assertEquals(1, entries.get(0).getLineNo());
        assertEquals(2, entries.get(1).getLineNo());
        assertEquals(3, entries.get(2).getLineNo());
    }

    @Test
    @DisplayName("科目名称映射被正确回填")
    void testSubjectNameFilled()
    {
        List<FinVoucherRule> rules = new ArrayList<>();
        rules.add(rule("mat", "1403", "DEBIT", "amount_excl_tax"));
        rules.add(rule("ap", "2202", "CREDIT", "amount_excl_tax"));

        List<FinVoucherEntry> entries =
                engine.buildEntries(rules, amounts("100", "100", "0"), realSubjects());
        assertEquals("原材料", entries.get(0).getAccountName());
        assertEquals("应付账款", entries.get(1).getAccountName());
    }

    @Test
    @DisplayName("币种默认为 CNY，汇率与税额默认0")
    void testDefaults()
    {
        // 合法场景：借 应收账款 / 贷 主营业务收入
        List<FinVoucherRule> rules = new ArrayList<>();
   rules.add(rule("a", "1122", "DEBIT", "total_amount"));
   rules.add(rule("b", "6001", "CREDIT", "total_amount"));

        List<FinVoucherEntry> entries = engine.buildEntries(rules, amounts("100", "100", "0"), realSubjects());
        assertEquals("CNY", entries.get(0).getCurrency());
        assertEquals(0, BigDecimal.ONE.compareTo(entries.get(0).getExchangeRate()), "默认汇率 1");
        assertEquals(0, BigDecimal.ZERO.compareTo(entries.get(0).getTaxAmount()), "默认税额 0");
    }
}