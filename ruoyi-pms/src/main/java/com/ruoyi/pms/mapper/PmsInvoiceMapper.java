package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsInvoice;
import com.ruoyi.pms.domain.PmsInvoiceDetail;

/**
 * 发票结算 Mapper
 *
 * @author ruoyi
 */
public interface PmsInvoiceMapper
{
    public List<PmsInvoice> selectInvoiceList(PmsInvoice invoice);
    public PmsInvoice selectInvoiceById(Long invoiceId);
    public int insertInvoice(PmsInvoice invoice);
    public int updateInvoice(PmsInvoice invoice);
    public int deleteInvoiceByIds(Long[] invoiceIds);

    /** 查询发票明细列表 */
    public List<PmsInvoiceDetail> selectInvoiceDetailByInvoiceId(Long invoiceId);

    /** 新增发票明细 */
    public int insertInvoiceDetail(PmsInvoiceDetail detail);

    /** 删除发票明细 */
    public int deleteInvoiceDetailByInvoiceId(Long invoiceId);

    /** 按合同聚合已付金额 */
    public java.math.BigDecimal sumPaidAmountByContractId(Long contractId);

    /** 按订单聚合已付金额 */
    public java.math.BigDecimal sumPaidAmountByOrderId(Long orderId);
}
