package com.ruoyi.fin.service.impl;

import com.ruoyi.fin.domain.FinVoucherEntry;
import com.ruoyi.fin.domain.dto.VoucherDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 凭证平衡逻辑单元测试
 *
 * <p>【P0-5 核心】借贷平衡是记账的基本约束，必须 100% 覆盖。</p>
 *
 * <p>实现说明：fillBalance 为私有方法，用反射调用，
 * 避免为测试改动生产代码可见性。</p>
 *
 * @author ruoyi
 */
class VoucherBalanceTest
{
    /**
     * 反射调用 fillBalance 计算借贷合计。
     */
    private VoucherDTO balanceOf(List<FinVoucherEntry> entries) throws Exception
    {
        FinVoucherServiceImpl svc = new FinVoucherServiceImpl();
        Method m = FinVoucherServiceImpl.class.getDeclaredMethod("fillBalance", VoucherDTO.class, List.class);
        m.setAccessible(true);
        VoucherDTO dto = new VoucherDTO();
        dto.setEntries(entries);
        m.invoke(svc, dto, entries);
        return dto;
    }

    private static FinVoucherEntry entry(String dir, String amount)
    {
        FinVoucherEntry e = new FinVoucherEntry();
        e.setDirection(dir);
        e.setAmount(new BigDecimal(amount));
        return e;
    }

    private static FinVoucherEntry debit(String amt)
    {
        return entry(FinVoucherEntry.DIR_DEBIT, amt);
    }

    private static FinVoucherEntry credit(String amt)
    {
        return entry(FinVoucherEntry.DIR_CREDIT, amt);
    }

    @Test
    @DisplayName("借贷相等时平衡：借100 贷100")
    void testBalanced_Simple() throws Exception
    {
        VoucherDTO dto = balanceOf(new ArrayList<>(Arrays.asList(debit("100.00"), credit("100.00"))));
        assertTrue(dto.isBalanced(), "100 借 100 贷应平衡");
        assertEquals(0, dto.getDiff().compareTo(BigDecimal.ZERO), "差额应为 0");
    }

    @Test
    @DisplayName("借贷不等时不平衡：借100 贷80")
    void testUnbalanced_Diff20() throws Exception
    {
        VoucherDTO dto = balanceOf(new ArrayList<>(Arrays.asList(debit("100.00"), credit("80.00"))));
        assertFalse(dto.isBalanced(), "100 借 80 贷应不平衡");
        assertEquals(0, new BigDecimal("20.00").compareTo(dto.getDiff()), "差额应为 20");
    }

    @Test
    @DisplayName("多借多贷汇总正确：借100+200 贷150+150")
    void testBalanced_MultiEntry() throws Exception
    {
        VoucherDTO dto = balanceOf(new ArrayList<>(Arrays.asList(
                debit("100.00"), debit("200.00"),
                credit("150.00"), credit("150.00"))));
        assertTrue(dto.isBalanced(), "100+200 借 150+150 贷应平衡");
        assertEquals(0, new BigDecimal("300.00").compareTo(dto.getDebitTotal()), "借方合计应为 300");
        assertEquals(0, new BigDecimal("300.00").compareTo(dto.getCreditTotal()), "贷方合计应为 300");
    }

    @Test
    @DisplayName("贷方大于借方时差额为负")
    void testUnbalanced_CreditExceeds() throws Exception
    {
        VoucherDTO dto = balanceOf(new ArrayList<>(Arrays.asList(debit("50.00"), credit("80.00"))));
        assertFalse(dto.isBalanced(), "贷方大于借方应不平衡");
        assertEquals(0, new BigDecimal("-30.00").compareTo(dto.getDiff()), "差额应为 -30");
    }

    @Test
    @DisplayName("金额为 null 的分录被忽略，不影响平衡判定")
    void testNullAmountIgnored() throws Exception
    {
        List<FinVoucherEntry> list = new ArrayList<>();
        list.add(debit("100.00"));
        list.add(credit("100.00"));
        FinVoucherEntry empty = credit("0.00");
        empty.setAmount(null);
        list.add(empty);
        VoucherDTO dto = balanceOf(list);
        assertTrue(dto.isBalanced(), "空金额行应被忽略，仍视为平衡");
    }

    @Test
    @DisplayName("全为借方必然不平衡（缺贷方）")
    void testAllDebit_Unbalanced() throws Exception
    {
        VoucherDTO dto = balanceOf(new ArrayList<>(Arrays.asList(debit("100.00"), debit("50.00"))));
        assertFalse(dto.isBalanced(), "只有借方应不平衡");
        assertEquals(0, new BigDecimal("150.00").compareTo(dto.getDebitTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getCreditTotal()), "贷方合计应为 0");
    }

    @Test
    @DisplayName("小数精度：0.1+0.2 与 0.3 应平衡")
    void testBigDecimalPrecision() throws Exception
    {
        List<FinVoucherEntry> list = new ArrayList<>(Arrays.asList(
                debit("0.1"), debit("0.2"), credit("0.3")));
        VoucherDTO dto = balanceOf(list);
        assertTrue(dto.isBalanced(), "BigDecimal 逐行累加，0.1+0.2 应精确等于 0.3");
    }

    @Test
    @DisplayName("空分录列表：合计为0且视为平衡（后续由 NoEntry 规则拦截）")
    void testEmptyEntries()
    throws Exception
    {
        VoucherDTO dto = balanceOf(new ArrayList<>());
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getDebitTotal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(dto.getCreditTotal()));
        assertTrue(dto.isBalanced(), "空列表差额为0");
    }
}