package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmInvoice;

/**
 * 项目开票记录Service接口
 */
public interface IPjmInvoiceService
{
    public List<PjmInvoice> selectInvoiceList(PjmInvoice pjmInvoice);

    public PjmInvoice selectInvoiceById(Long invoiceId);

    public int insertInvoice(PjmInvoice pjmInvoice);

    public int updateInvoice(PjmInvoice pjmInvoice);

    public int deleteInvoiceByIds(Long[] invoiceIds);

    /** 开票确认（待开票 -> 已开票） */
    public int issueInvoice(Long invoiceId);

    /** 开票作废（已开票 -> 已作废） */
    public int voidInvoice(Long invoiceId);
}
