package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkInvoice;

/**
 * 发票 Mapper
 *
 * @author ruoyi
 */
public interface MkInvoiceMapper
{
    List<MkInvoice> selectInvoiceList(MkInvoice invoice);
    MkInvoice selectInvoiceById(Long invoiceId);
    int insertInvoice(MkInvoice invoice);
    int updateInvoice(MkInvoice invoice);
    int deleteInvoiceByIds(Long[] invoiceIds);
}
