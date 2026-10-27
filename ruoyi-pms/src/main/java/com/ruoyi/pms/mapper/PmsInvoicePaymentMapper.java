package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsInvoicePayment;

/**
 * 发票付款记录 Mapper
 *
 * @author ruoyi
 */
public interface PmsInvoicePaymentMapper
{
    /** 根据发票ID查询付款记录 */
    public List<PmsInvoicePayment> selectPaymentByInvoiceId(Long invoiceId);

    /** 新增付款记录 */
    public int insertPayment(PmsInvoicePayment payment);

    /** 根据发票ID汇总已付金额 */
    public java.math.BigDecimal sumPaidByInvoiceId(Long invoiceId);
}
