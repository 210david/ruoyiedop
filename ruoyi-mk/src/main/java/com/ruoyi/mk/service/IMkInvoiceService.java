package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.mk.domain.MkInvoice;

/**
 * 发票 Service接口
 *
 * @author ruoyi
 */
public interface IMkInvoiceService
{
    List<MkInvoice> selectInvoiceList(MkInvoice invoice);
    MkInvoice selectInvoiceById(Long invoiceId);
    int insertInvoice(MkInvoice invoice);
    int updateInvoice(MkInvoice invoice);
    int deleteInvoiceByIds(Long[] invoiceIds);
    /** 开票 */
    int issueInvoice(Long invoiceId);
    /** 作废发票 */
    int voidInvoice(Long invoiceId);
    /** 发票识别（PDF OCR） */
    Map<String, Object> recognizeInvoice(MultipartFile file) throws Exception;
}
