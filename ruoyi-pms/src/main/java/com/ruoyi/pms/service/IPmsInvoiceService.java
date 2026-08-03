package com.ruoyi.pms.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.pms.domain.PmsInvoice;

/**
 * 发票结算 Service接口
 *
 * @author ruoyi
 */
public interface IPmsInvoiceService
{
    public List<PmsInvoice> selectInvoiceList(PmsInvoice invoice);
    public PmsInvoice selectInvoiceById(Long invoiceId);
    public int insertInvoice(PmsInvoice invoice);
    public int updateInvoice(PmsInvoice invoice);
    public int deleteInvoiceByIds(Long[] invoiceIds);

    /** 提交审核 */
    public int submitInvoice(Long invoiceId);

    /** 审核发票 */
    public int auditInvoice(Long invoiceId, String status, String auditOpinion);

    /** 付款登记（支持多次付款） */
    public int payInvoice(com.ruoyi.pms.domain.dto.InvoicePaymentDTO paymentDTO);

    /**
     * 三方匹配：采购订单、收货验收、发票金额比对
     * @param invoiceId 发票ID
     * @return 匹配结果（orderAmount, receivedAmount, invoiceAmount, matchResult, details）
     */
    public Map<String, Object> threeWayMatch(Long invoiceId);

    /** 发票识别（PDF OCR） */
    public Map<String, Object> recognizeInvoice(org.springframework.web.multipart.MultipartFile file) throws Exception;
}
