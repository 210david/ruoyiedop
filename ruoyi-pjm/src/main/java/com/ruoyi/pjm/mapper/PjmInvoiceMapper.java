package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmInvoice;

/**
 * 项目开票记录Mapper接口
 */
public interface PjmInvoiceMapper
{
    public List<PjmInvoice> selectInvoiceList(PjmInvoice pjmInvoice);

    public PjmInvoice selectInvoiceById(Long invoiceId);

    public int insertInvoice(PjmInvoice pjmInvoice);

    public int updateInvoice(PjmInvoice pjmInvoice);

    public int deleteInvoiceByIds(Long[] invoiceIds);

    /** 开票确认（待开票 -> 已开票） */
    public int issueInvoice(PjmInvoice pjmInvoice);

    /** 开票作废（已开票 -> 已作废） */
    public int voidInvoice(PjmInvoice pjmInvoice);
}
