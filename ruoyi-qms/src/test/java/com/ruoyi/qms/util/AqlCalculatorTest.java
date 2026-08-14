package com.ruoyi.qms.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AqlCalculator 单元测试
 * 全面验证 GB/T 2828.1 标准的样本量(n)、接收数(Ac)、拒收数(Re)计算逻辑
 * 覆盖全部5个AQL等级 × 全部15个字码(A~Q) + 边界场景 + 多检验水平
 */
@DisplayName("AQL抽样计算 — GB/T 2828.1 标准验证")
class AqlCalculatorTest
{
    /** 辅助：断言计算结果（默认检验水平II） */
    private void assertResult(int batchQty, String aqlLevel, int expN, int expAc, int expRe)
    {
        int[] result = AqlCalculator.calculate(new BigDecimal(batchQty), aqlLevel);
        assertNotNull(result, "结果不应为null");
        assertEquals(expN, result[0], "批量=" + batchQty + ", AQL=" + aqlLevel + " → 样本量(n)不符");
        assertEquals(expAc, result[1], "批量=" + batchQty + ", AQL=" + aqlLevel + " → 接收数(Ac)不符");
        assertEquals(expRe, result[2], "批量=" + batchQty + ", AQL=" + aqlLevel + " → 拒收数(Re)不符");
    }

    /** 辅助：断言计算结果（指定检验水平） */
    private void assertResult(int batchQty, String aqlLevel, String inspectLevel, int expN, int expAc, int expRe)
    {
        int[] result = AqlCalculator.calculate(new BigDecimal(batchQty), aqlLevel, inspectLevel);
        assertNotNull(result, "结果不应为null");
        assertEquals(expN, result[0], "批量=" + batchQty + ", AQL=" + aqlLevel + ", 水平=" + inspectLevel + " → 样本量(n)不符");
        assertEquals(expAc, result[1], "批量=" + batchQty + ", AQL=" + aqlLevel + ", 水平=" + inspectLevel + " → 接收数(Ac)不符");
        assertEquals(expRe, result[2], "批量=" + batchQty + ", AQL=" + aqlLevel + ", 水平=" + inspectLevel + " → 拒收数(Re)不符");
    }

    // ==================== 用户原始场景（核心修复验证） ====================
    @Test
    @DisplayName("用户场景：OQC, 送检数量=10, AQL=2.5%, 检验水平=S-4 → n=3, Ac=0, Re=1")
    void testUserScenario_Qty10_Aql25_S4()
    {
        // 批量=10, S-4: 9~15 → 字码B, n=3
        // AQL 2.5% 表中无字码B条目，向下查找→E(13), 但13>10(批量),
        // 所以保持字码B的样本量n=3, 使用E的Ac/Re=0/1
        assertResult(10, "2.5", "S-4", 3, 0, 1);
    }

    @Test
    @DisplayName("对比：送检数量=10, AQL=2.5%, 检验水平=II → n=10(100%检验), Ac=0, Re=1")
    void testUserScenario_Qty10_Aql25_LevelII()
    {
        // 批量=10, Level II: 9~15 → 字码B, n=3
        // AQL 2.5% 表中无字码B条目，向下查找→E(13), 13>10(批量),
        // 保持n=3, 但 n=3 < batchN=10, 所以不触发100%检验
        // 结果: n=3, Ac=0, Re=1
        assertResult(10, "2.5", "II", 3, 0, 1);
    }

    // ==================== S-4 检验水平验证 ====================
    @Test
    @DisplayName("S-4 检验水平 — 各批量区间字码验证")
    void testS4_InspectLevel()
    {
        // S-4: 1-8→A(n=2), 9-15→B(n=3), 16-25→C(n=5), 26-50→D(n=8)
        // AQL 4.0% 有D条目: Ac=0, Re=1
        assertResult(5,   "4.0", "S-4", 2, 0, 1);   // 字码A, ↓→D(8), 8>5 → n=2
        assertResult(10,  "4.0", "S-4", 3, 0, 1);   // 字码B, ↓→D(8), 8<10 → n=8, Ac=0,Re=1
        assertResult(20,  "4.0", "S-4", 5, 0, 1);   // 字码C, ↓→D(8), 8<20 → n=8
        assertResult(30,  "4.0", "S-4", 8, 0, 1);   // 字码D, 直接命中
        assertResult(60,  "4.0", "S-4", 13, 1, 2);  // 字码E, 直接命中
        assertResult(100, "4.0", "S-4", 20, 2, 3);  // 字码F, 直接命中
    }

    @Test
    @DisplayName("S-4 检验水平 — AQL 2.5% 验证")
    void testS4_Aql25()
    {
        // S-4: 9-15→B(n=3), AQL2.5%无B, ↓→E(13), 13>10 → 保持n=3, Ac=0,Re=1
        assertResult(10,  "2.5", "S-4", 3, 0, 1);
        // S-4: 16-25→C(n=5), AQL2.5%无C, ↓→E(13), 13<25 → n=13
        assertResult(20,  "2.5", "S-4", 13, 0, 1);
        // S-4: 26-50→D(n=8), AQL2.5%无D, ↓→E(13), 13<50 → n=13
        assertResult(50,  "2.5", "S-4", 13, 0, 1);
        // S-4: 51-90→E(n=13), AQL2.5%直接命中E: Ac=0,Re=1
        assertResult(90,  "2.5", "S-4", 13, 0, 1);
        // S-4: 91-150→F(n=20), AQL2.5%直接命中F: Ac=1,Re=2
        assertResult(150, "2.5", "S-4", 20, 1, 2);
        // S-4: 151-280→G(n=32), AQL2.5%直接命中G: Ac=2,Re=3
        assertResult(280, "2.5", "S-4", 32, 2, 3);
    }

    // ==================== AQL 0.65% (Level II) ====================
    @Test
    @DisplayName("AQL 0.65% — 全字码验证")
    void testAql065()
    {
        // 字码 A(1-8): ↓→G(32), n≥批量时100%检验
        assertResult(8,      "0.65", 8,    0, 1);  // n=32≥8 → 100%检验
        // 字码 B(9-15): ↓→G(32), 32≥15 → 100%检验
        assertResult(15,     "0.65", 15,   0, 1);
        // 字码 C(16-25): ↓→G(32), 32≥25 → 100%检验
        assertResult(25,     "0.65", 25,   0, 1);
        // 字码 D(26-50): ↓→G(32)
        assertResult(50,     "0.65", 32,   0, 1);
        // 字码 E(51-90): ↓→G(32)
        assertResult(90,     "0.65", 32,   0, 1);
        // 字码 F(91-150): ↓→G(32)
        assertResult(150,    "0.65", 32,   0, 1);
        // 字码 G(151-280): 直接值
        assertResult(280,    "0.65", 32,   0, 1);
        // 字码 H(281-500)
        assertResult(500,    "0.65", 50,   1, 2);
        // 字码 J(501-1200)
        assertResult(1200,   "0.65", 80,   1, 2);
        // 字码 K(1201-3200)
        assertResult(3200,   "0.65", 125,  2, 3);
        // 字码 L(3201-10000)
        assertResult(10000,  "0.65", 200,  3, 4);
        // 字码 M(10001-35000)
        assertResult(35000,  "0.65", 315,  5, 6);
        // 字码 N(35001-150000)
        assertResult(150000, "0.65", 500,  7, 8);
        // 字码 P(150001-500000)
        assertResult(500000, "0.65", 800,  10, 11);
        // 字码 Q(>500000)
        assertResult(600000, "0.65", 1250, 14, 15);
    }

    // ==================== AQL 1.0% (Level II) ====================
    @Test
    @DisplayName("AQL 1.0% — 全字码验证")
    void testAql10()
    {
        // 字码 A~F: ↓→G(32)
        assertResult(8,      "1.0", 8,    1, 2);  // n=32≥8 → 100%检验
        // 字码 B(9-15): ↓→G(32), 32≥15 → 100%检验
        assertResult(15,     "1.0", 15,   1, 2);
        // 字码 C(16-25): ↓→G(32), 32≥25 → 100%检验
        assertResult(25,     "1.0", 25,   1, 2);
        assertResult(50,     "1.0", 32,   1, 2);
        assertResult(90,     "1.0", 32,   1, 2);
        assertResult(150,    "1.0", 32,   1, 2);
        // 字码 G(151-280): 直接值
        assertResult(280,    "1.0", 32,   1, 2);
        // 字码 H
        assertResult(500,    "1.0", 50,   1, 2);
        // 字码 J
        assertResult(1200,   "1.0", 80,   2, 3);
        // 字码 K
        assertResult(3200,   "1.0", 125,  3, 4);
        // 字码 L
        assertResult(10000,  "1.0", 200,  5, 6);
        // 字码 M
        assertResult(35000,  "1.0", 315,  7, 8);
        // 字码 N
        assertResult(150000, "1.0", 500,  10, 11);
        // 字码 P
        assertResult(500000, "1.0", 800,  14, 15);
        // 字码 Q
        assertResult(600000, "1.0", 1250, 21, 22);
    }

    // ==================== AQL 1.5% (Level II) ====================
    @Test
    @DisplayName("AQL 1.5% — 全字码验证（含↑箭头）")
    void testAql15()
    {
        // 字码 A~E: ↓→F(20)
        assertResult(8,      "1.5", 8,    0, 1);  // n=20≥8 → 100%检验
        assertResult(15,     "1.5", 15,   0, 1);  // n=20≥15 → 100%检验
        assertResult(25,     "1.5", 20,   0, 1);
        assertResult(50,     "1.5", 20,   0, 1);
        assertResult(90,     "1.5", 20,   0, 1);
        // 字码 F(91-150): 直接值
        assertResult(150,    "1.5", 20,   0, 1);
        // 字码 G
        assertResult(280,    "1.5", 32,   1, 2);
        // 字码 H
        assertResult(500,    "1.5", 50,   2, 3);
        // 字码 J
        assertResult(1200,   "1.5", 80,   3, 4);
        // 字码 K
        assertResult(3200,   "1.5", 125,  5, 6);
        // 字码 L
        assertResult(10000,  "1.5", 200,  7, 8);
        // 字码 M
        assertResult(35000,  "1.5", 315,  10, 11);
        // 字码 N
        assertResult(150000, "1.5", 500,  14, 15);
        // 字码 P
        assertResult(500000, "1.5", 800,  21, 22);
        // 字码 Q: ↑→P(800)
        assertResult(600000, "1.5", 800,  21, 22);
    }

    // ==================== AQL 2.5% (Level II) ====================
    @Test
    @DisplayName("AQL 2.5% — 全字码验证（含↑箭头）")
    void testAql25()
    {
        // 字码 A~D: ↓→E(13)
        assertResult(8,      "2.5", 8,    0, 1);  // n=13≥8 → 100%检验
        // 字码 B(9-15): ↓→E(13), 13<15 → n=13 (不触发100%检验)
        assertResult(15,     "2.5", 13,   0, 1);
        assertResult(25,     "2.5", 13,   0, 1);
        assertResult(50,     "2.5", 13,   0, 1);
        // 字码 E(51-90): 直接值
        assertResult(90,     "2.5", 13,   0, 1);
        // 字码 F
        assertResult(150,    "2.5", 20,   1, 2);
        // 字码 G
        assertResult(280,    "2.5", 32,   2, 3);
        // 字码 H
        assertResult(500,    "2.5", 50,   3, 4);
        // 字码 J
        assertResult(1200,   "2.5", 80,   5, 6);
        // 字码 K
        assertResult(3200,   "2.5", 125,  7, 8);
        // 字码 L
        assertResult(10000,  "2.5", 200,  10, 11);
        // 字码 M
        assertResult(35000,  "2.5", 315,  14, 15);
        // 字码 N
        assertResult(150000, "2.5", 500,  21, 22);
        // 字码 P: ↑→N(500)
        assertResult(500000, "2.5", 500,  21, 22);
        // 字码 Q: ↑→N(500)
        assertResult(600000, "2.5", 500,  21, 22);
    }

    // ==================== AQL 4.0% (Level II) ====================
    @Test
    @DisplayName("AQL 4.0% — 全字码验证（含↑箭头）")
    void testAql40()
    {
        // 字码 A~C: ↓→D(8)
        assertResult(8,      "4.0", 8,    0, 1);  // n=8≥8 → 100%检验
        assertResult(15,     "4.0", 8,    0, 1);
        assertResult(25,     "4.0", 8,    0, 1);
        // 字码 D(26-50): 直接值 0,1
        assertResult(50,     "4.0", 8,    0, 1);
        // 字码 E(51-90): 1,2
        assertResult(90,     "4.0", 13,   1, 2);
        // 字码 F: 2,3
        assertResult(150,    "4.0", 20,   2, 3);
        // 字码 G: 3,4
        assertResult(280,    "4.0", 32,   3, 4);
        // 字码 H
        assertResult(500,    "4.0", 50,   5, 6);
        // 字码 J
        assertResult(1200,   "4.0", 80,   7, 8);
        // 字码 K
        assertResult(3200,   "4.0", 125,  10, 11);
        // 字码 L
        assertResult(10000,  "4.0", 200,  14, 15);
        // 字码 M
        assertResult(35000,  "4.0", 315,  21, 22);
        // 字码 N: ↑→M(315)
        assertResult(150000, "4.0", 315,  21, 22);
        // 字码 P: ↑→M(315)
        assertResult(500000, "4.0", 315,  21, 22);
        // 字码 Q: ↑→M(315)
        assertResult(600000, "4.0", 315,  21, 22);
    }

    // ==================== 边界场景 ====================
    @Test
    @DisplayName("边界：批量=1 → 100%检验")
    void testEdgeCase_Batch1()
    {
        assertResult(1, "2.5", 1, 0, 1);
        assertResult(1, "4.0", 1, 0, 1);
        assertResult(1, "0.65", 1, 0, 1);
    }

    @Test
    @DisplayName("边界：批量=2 → 100%检验（n=2）")
    void testEdgeCase_Batch2()
    {
        assertResult(2, "2.5", 2, 0, 1);
        assertResult(2, "4.0", 2, 0, 1);
    }

    @Test
    @DisplayName("边界：批量=100, AQL=2.5% → 字码F, n=20, Ac=1, Re=2")
    void testEdgeCase_Batch100_Aql25()
    {
        assertResult(100, "2.5", 20, 1, 2);
    }

    @Test
    @DisplayName("边界：批量=100, AQL=4.0% → 字码F, n=20, Ac=2, Re=3")
    void testEdgeCase_Batch100_Aql40()
    {
        assertResult(100, "4.0", 20, 2, 3);
    }

    // ==================== 字码映射验证 ====================
    @Test
    @DisplayName("字码映射：Level II 边界值验证")
    void testCodeLetter_Boundaries()
    {
        assertEquals("A", AqlCalculator.getCodeLetter(new BigDecimal("8")));
        assertEquals("B", AqlCalculator.getCodeLetter(new BigDecimal("9")));
        assertEquals("B", AqlCalculator.getCodeLetter(new BigDecimal("15")));
        assertEquals("C", AqlCalculator.getCodeLetter(new BigDecimal("16")));
        assertEquals("C", AqlCalculator.getCodeLetter(new BigDecimal("25")));
        assertEquals("D", AqlCalculator.getCodeLetter(new BigDecimal("26")));
        assertEquals("D", AqlCalculator.getCodeLetter(new BigDecimal("50")));
        assertEquals("E", AqlCalculator.getCodeLetter(new BigDecimal("51")));
        assertEquals("E", AqlCalculator.getCodeLetter(new BigDecimal("90")));
        assertEquals("F", AqlCalculator.getCodeLetter(new BigDecimal("91")));
        assertEquals("F", AqlCalculator.getCodeLetter(new BigDecimal("150")));
        assertEquals("G", AqlCalculator.getCodeLetter(new BigDecimal("151")));
        assertEquals("G", AqlCalculator.getCodeLetter(new BigDecimal("280")));
        assertEquals("H", AqlCalculator.getCodeLetter(new BigDecimal("281")));
        assertEquals("H", AqlCalculator.getCodeLetter(new BigDecimal("500")));
        assertEquals("J", AqlCalculator.getCodeLetter(new BigDecimal("501")));
        assertEquals("J", AqlCalculator.getCodeLetter(new BigDecimal("1200")));
        assertEquals("K", AqlCalculator.getCodeLetter(new BigDecimal("1201")));
        assertEquals("K", AqlCalculator.getCodeLetter(new BigDecimal("3200")));
        assertEquals("L", AqlCalculator.getCodeLetter(new BigDecimal("3201")));
        assertEquals("L", AqlCalculator.getCodeLetter(new BigDecimal("10000")));
        assertEquals("M", AqlCalculator.getCodeLetter(new BigDecimal("10001")));
        assertEquals("M", AqlCalculator.getCodeLetter(new BigDecimal("35000")));
        assertEquals("N", AqlCalculator.getCodeLetter(new BigDecimal("35001")));
        assertEquals("N", AqlCalculator.getCodeLetter(new BigDecimal("150000")));
        assertEquals("P", AqlCalculator.getCodeLetter(new BigDecimal("150001")));
        assertEquals("P", AqlCalculator.getCodeLetter(new BigDecimal("500000")));
        assertEquals("Q", AqlCalculator.getCodeLetter(new BigDecimal("500001")));
    }

    @Test
    @DisplayName("字码映射：S-4 边界值验证")
    void testCodeLetter_S4_Boundaries()
    {
        assertEquals("A", AqlCalculator.getCodeLetter(new BigDecimal("8"), "S-4"));
        assertEquals("B", AqlCalculator.getCodeLetter(new BigDecimal("9"), "S-4"));
        assertEquals("B", AqlCalculator.getCodeLetter(new BigDecimal("15"), "S-4"));
        assertEquals("C", AqlCalculator.getCodeLetter(new BigDecimal("16"), "S-4"));
        assertEquals("C", AqlCalculator.getCodeLetter(new BigDecimal("25"), "S-4"));
        assertEquals("D", AqlCalculator.getCodeLetter(new BigDecimal("26"), "S-4"));
        assertEquals("D", AqlCalculator.getCodeLetter(new BigDecimal("50"), "S-4"));
        assertEquals("E", AqlCalculator.getCodeLetter(new BigDecimal("51"), "S-4"));
        assertEquals("E", AqlCalculator.getCodeLetter(new BigDecimal("90"), "S-4"));
        assertEquals("F", AqlCalculator.getCodeLetter(new BigDecimal("91"), "S-4"));
        assertEquals("F", AqlCalculator.getCodeLetter(new BigDecimal("150"), "S-4"));
        assertEquals("G", AqlCalculator.getCodeLetter(new BigDecimal("151"), "S-4"));
        assertEquals("G", AqlCalculator.getCodeLetter(new BigDecimal("280"), "S-4"));
        assertEquals("H", AqlCalculator.getCodeLetter(new BigDecimal("281"), "S-4"));
        assertEquals("J", AqlCalculator.getCodeLetter(new BigDecimal("401"), "S-4"));
    }

    @Test
    @DisplayName("字码映射：Level I 边界值验证")
    void testCodeLetter_Level1_Boundaries()
    {
        // Level I: 1-15→A, 16-25→B, 26-50→C, 51-90→C, 91-150→D
        assertEquals("A", AqlCalculator.getCodeLetter(new BigDecimal("15"), "I"));
        assertEquals("B", AqlCalculator.getCodeLetter(new BigDecimal("16"), "I"));
        assertEquals("C", AqlCalculator.getCodeLetter(new BigDecimal("26"), "I"));
        assertEquals("C", AqlCalculator.getCodeLetter(new BigDecimal("90"), "I"));
        assertEquals("D", AqlCalculator.getCodeLetter(new BigDecimal("91"), "I"));
        assertEquals("D", AqlCalculator.getCodeLetter(new BigDecimal("280"), "I"));
        assertEquals("E", AqlCalculator.getCodeLetter(new BigDecimal("281"), "I"));
    }

    @Test
    @DisplayName("字码映射：Level III 边界值验证")
    void testCodeLetter_Level3_Boundaries()
    {
        // Level III: 1-8→C, 9-15→D, 16-25→E, 26-50→F
        assertEquals("C", AqlCalculator.getCodeLetter(new BigDecimal("8"), "III"));
        assertEquals("D", AqlCalculator.getCodeLetter(new BigDecimal("9"), "III"));
        assertEquals("D", AqlCalculator.getCodeLetter(new BigDecimal("15"), "III"));
        assertEquals("E", AqlCalculator.getCodeLetter(new BigDecimal("16"), "III"));
        assertEquals("F", AqlCalculator.getCodeLetter(new BigDecimal("26"), "III"));
    }

    // ==================== 检验水平输入兼容性 ====================
    @Test
    @DisplayName("检验水平输入兼容性：大小写不敏感")
    void testInspectLevelInputCompatibility()
    {
        // 各种输入格式都应该能识别
        String code1 = AqlCalculator.getCodeLetter(new BigDecimal("10"), "S-4");
        String code2 = AqlCalculator.getCodeLetter(new BigDecimal("10"), "S4");
        String code3 = AqlCalculator.getCodeLetter(new BigDecimal("10"), "s-4");
        assertEquals(code1, code2, "S-4 和 S4 应等价");
        assertEquals(code1, code3, "S-4 和 s-4 应等价");

        // 数字形式（检验严格度 1=正常，但这里作为检验水平传入会被识别为 Level I）
        // 注意：1/I → Level I, 2/II → Level II, 3/III → Level III
        String codeLevel1a = AqlCalculator.getCodeLetter(new BigDecimal("280"), "I");
        String codeLevel1b = AqlCalculator.getCodeLetter(new BigDecimal("280"), "1");
        assertEquals(codeLevel1a, codeLevel1b, "I 和 1 应等价");

        // null 和空串默认 II
        String codeDefault = AqlCalculator.getCodeLetter(new BigDecimal("10"), null);
        String codeII = AqlCalculator.getCodeLetter(new BigDecimal("10"), "II");
        assertEquals(codeDefault, codeII, "null 应默认为 II");
    }

    // ==================== 判定逻辑验证 ====================
    @Test
    @DisplayName("批量判定：defectCount ≤ Ac → 合格")
    void testJudgeBatch()
    {
        assertTrue(AqlCalculator.judgeBatch(0, 0, 1), "0个不合格 ≤ Ac=0 → 合格");
        assertTrue(AqlCalculator.judgeBatch(1, 1, 2), "1个不合格 ≤ Ac=1 → 合格");
        assertTrue(AqlCalculator.judgeBatch(2, 2, 3), "2个不合格 ≤ Ac=2 → 合格");
        assertFalse(AqlCalculator.judgeBatch(1, 0, 1), "1个不合格 > Ac=0 → 不合格");
        assertFalse(AqlCalculator.judgeBatch(3, 2, 3), "3个不合格 > Ac=2 → 不合格");
    }
}
