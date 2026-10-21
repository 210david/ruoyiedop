package com.ruoyi.fin.domain.dto;

import com.ruoyi.fin.domain.FinVoucher;
import com.ruoyi.fin.domain.FinVoucherEntry;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 凭证录入 DTO（凭证主表 + 分录明细的聚合体）
 *
 * <p>【P0-5 借贷平衡】借贷合计与差额在 Service 层计算后回填，
 * 前端展示用；保存时以 Service 校验结果为准，不信任前端传值。</p>
 *
 * <p>【P0-4 期间】period 由 voucherDate 在 Service 派生，不接受前端传值。</p>
 *
 * @author ruoyi
 */
public class VoucherDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 凭证主表 */
    private FinVoucher voucher;

    /** 分录明细 */
    private List<FinVoucherEntry> entries;

    /** 借方合计（服务端回填） */
    private BigDecimal debitTotal = BigDecimal.ZERO;

    /** 贷方合计（服务端回填） */
    private BigDecimal creditTotal = BigDecimal.ZERO;

    /** 差额（借-贷，服务端回填） */
    private BigDecimal diff = BigDecimal.ZERO;

    /** 是否平衡（服务端回填） */
    private boolean balanced = true;

    public FinVoucher getVoucher() { return voucher; }
    public void setVoucher(FinVoucher voucher) { this.voucher = voucher; }

    public List<FinVoucherEntry> getEntries() { return entries; }
    public void setEntries(List<FinVoucherEntry> entries) { this.entries = entries; }

    public BigDecimal getDebitTotal() { return debitTotal; }
    public void setDebitTotal(BigDecimal debitTotal) { this.debitTotal = debitTotal; }

    public BigDecimal getCreditTotal() { return creditTotal; }
    public void setCreditTotal(BigDecimal creditTotal) { this.creditTotal = creditTotal; }

    public BigDecimal getDiff() { return diff; }
    public void setDiff(BigDecimal diff) { this.diff = diff; }

    public boolean isBalanced() { return balanced; }
    public void setBalanced(boolean balanced) { this.balanced = balanced; }
}