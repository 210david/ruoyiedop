package com.ruoyi.pms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.PmsInvoice;
import com.ruoyi.pms.domain.PmsInvoiceDetail;
import com.ruoyi.pms.domain.PmsInvoiceAuditLog;
import com.ruoyi.pms.domain.PmsInvoicePayment;
import com.ruoyi.pms.domain.PmsReceive;
import com.ruoyi.pms.domain.PmsReceiveDetail;
import com.ruoyi.pms.domain.PmsPurchaseOrder;
import com.ruoyi.pms.domain.PmsContract;
import com.ruoyi.pms.domain.dto.InvoicePaymentDTO;
import com.ruoyi.pms.mapper.PmsInvoiceMapper;
import com.ruoyi.pms.mapper.PmsInvoiceAuditLogMapper;
import com.ruoyi.pms.mapper.PmsInvoicePaymentMapper;
import com.ruoyi.pms.mapper.PmsReceiveMapper;
import com.ruoyi.pms.mapper.PmsContractMapper;
import com.ruoyi.pms.mapper.PmsPurchaseOrderMapper;
import com.ruoyi.pms.service.IPmsInvoiceService;
import com.ruoyi.pms.service.IPmsPurchaseOrderService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 发票结算 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsInvoiceServiceImpl implements IPmsInvoiceService
{
    @Autowired
    private PmsInvoiceMapper pmsInvoiceMapper;

    @Autowired
    private PmsInvoiceAuditLogMapper pmsInvoiceAuditLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private PmsReceiveMapper pmsReceiveMapper;

    @Autowired
    private IPmsPurchaseOrderService pmsPurchaseOrderService;

    @Autowired
    private PmsInvoicePaymentMapper pmsInvoicePaymentMapper;

    @Autowired
    private PmsContractMapper pmsContractMapper;

    @Autowired
    private PmsPurchaseOrderMapper pmsPurchaseOrderMapper;

    @Override
    public List<PmsInvoice> selectInvoiceList(PmsInvoice invoice)
    {
        return pmsInvoiceMapper.selectInvoiceList(invoice);
    }

    @Override
    public PmsInvoice selectInvoiceById(Long invoiceId)
    {
        PmsInvoice invoice = pmsInvoiceMapper.selectInvoiceById(invoiceId);
        if (invoice != null)
        {
            invoice.setDetailList(pmsInvoiceMapper.selectInvoiceDetailByInvoiceId(invoiceId));
            invoice.setAuditLogList(pmsInvoiceAuditLogMapper.selectAuditLogByInvoiceId(invoiceId));
            invoice.setPaymentLogList(pmsInvoicePaymentMapper.selectPaymentByInvoiceId(invoiceId));
        }
        return invoice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInvoice(PmsInvoice invoice)
    {
        if (StringUtils.isEmpty(invoice.getInvoiceNo()))
        {
            invoice.setInvoiceNo(mkNumberRuleService.generateNumber("pms_invoice"));
        }
        invoice.setDelFlag("0");
        if (invoice.getStatus() == null)
        {
            invoice.setStatus("0");
        }
        if (invoice.getPaymentAmount() == null)
        {
            invoice.setPaymentAmount(BigDecimal.ZERO);
        }
        // 计算明细汇总
        BigDecimal invoiceAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (invoice.getDetailList() != null)
        {
            for (PmsInvoiceDetail d : invoice.getDetailList())
            {
                if (d.getAmount() != null)
                {
                    invoiceAmount = invoiceAmount.add(d.getAmount());
                }
                else if (d.getQty() != null && d.getUnitPrice() != null)
                {
                    d.setAmount(d.getQty().multiply(d.getUnitPrice()));
                    invoiceAmount = invoiceAmount.add(d.getAmount());
                }
                if (d.getTaxAmount() != null)
                {
                    taxAmount = taxAmount.add(d.getTaxAmount());
                }
                d.setDelFlag("0");
            }
        }
        if (invoice.getInvoiceAmount() == null)
        {
            invoice.setInvoiceAmount(invoiceAmount);
        }
        if (invoice.getTaxAmount() == null)
        {
            invoice.setTaxAmount(taxAmount);
        }
        // 计算价税合计
        if (invoice.getTotalAmount() == null && invoice.getInvoiceAmount() != null && invoice.getTaxAmount() != null)
        {
            invoice.setTotalAmount(invoice.getInvoiceAmount().add(invoice.getTaxAmount()));
        }
        invoice.setCreateBy(SecurityUtils.getUsername());
        int rows = pmsInvoiceMapper.insertInvoice(invoice);
        // 插入明细
        if (invoice.getDetailList() != null)
        {
            for (PmsInvoiceDetail d : invoice.getDetailList())
            {
                d.setInvoiceId(invoice.getInvoiceId());
                d.setDelFlag("0");
                pmsInvoiceMapper.insertInvoiceDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInvoice(PmsInvoice invoice)
    {
        // 驳回状态修改后重置为待审核，清空审核信息（审核日志保留）
        PmsInvoice existing = pmsInvoiceMapper.selectInvoiceById(invoice.getInvoiceId());
        if (existing != null && "5".equals(existing.getStatus()))
        {
            invoice.setStatus("1");
            invoice.setAuditBy(null);
            invoice.setAuditTime(null);
            invoice.setAuditOpinion(null);
        }
        // 删除旧明细，重新插入
        pmsInvoiceMapper.deleteInvoiceDetailByInvoiceId(invoice.getInvoiceId());
        BigDecimal invoiceAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (invoice.getDetailList() != null)
        {
            for (PmsInvoiceDetail d : invoice.getDetailList())
            {
                d.setInvoiceId(invoice.getInvoiceId());
                if (d.getAmount() != null)
                {
                    invoiceAmount = invoiceAmount.add(d.getAmount());
                }
                else if (d.getQty() != null && d.getUnitPrice() != null)
                {
                    d.setAmount(d.getQty().multiply(d.getUnitPrice()));
                    invoiceAmount = invoiceAmount.add(d.getAmount());
                }
                if (d.getTaxAmount() != null)
                {
                    taxAmount = taxAmount.add(d.getTaxAmount());
                }
                d.setDelFlag("0");
                pmsInvoiceMapper.insertInvoiceDetail(d);
            }
        }
        // 仅在有明细时才用明细合计覆写主表金额，无明细时保留表单传入的值
        if (invoice.getDetailList() != null && !invoice.getDetailList().isEmpty())
        {
            if (invoice.getInvoiceAmount() != null)
            {
                invoice.setInvoiceAmount(invoiceAmount);
            }
            if (invoice.getTaxAmount() != null)
            {
                invoice.setTaxAmount(taxAmount);
            }
        }
        // 计算价税合计
        if (invoice.getTotalAmount() == null && invoice.getInvoiceAmount() != null && invoice.getTaxAmount() != null)
        {
            invoice.setTotalAmount(invoice.getInvoiceAmount().add(invoice.getTaxAmount()));
        }
        invoice.setUpdateBy(SecurityUtils.getUsername());
        return pmsInvoiceMapper.updateInvoice(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInvoiceByIds(Long[] invoiceIds)
    {
        return pmsInvoiceMapper.deleteInvoiceByIds(invoiceIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitInvoice(Long invoiceId)
    {
        PmsInvoice invoice = pmsInvoiceMapper.selectInvoiceById(invoiceId);
        if (invoice == null)
        {
            throw new ServiceException("发票不存在");
        }
        if (!"0".equals(invoice.getStatus()) && !"5".equals(invoice.getStatus()))
        {
            throw new ServiceException("只有草稿或已驳回状态的发票才能提交审核");
        }
        invoice.setStatus("1"); // 待审核
        invoice.setUpdateBy(SecurityUtils.getUsername());
        return pmsInvoiceMapper.updateInvoice(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditInvoice(Long invoiceId, String status, String auditOpinion)
    {
        PmsInvoice invoice = pmsInvoiceMapper.selectInvoiceById(invoiceId);
        if (invoice == null)
        {
            throw new ServiceException("发票不存在");
        }
        if (!"0".equals(invoice.getStatus()) && !"1".equals(invoice.getStatus()))
        {
            throw new ServiceException("只有草稿或待审核状态的发票才能审核");
        }
        invoice.setStatus(status);
        invoice.setAuditBy(SecurityUtils.getUsername());
        invoice.setAuditTime(new Date());
        invoice.setAuditOpinion(auditOpinion);
        invoice.setUpdateBy(SecurityUtils.getUsername());
        int rows = pmsInvoiceMapper.updateInvoice(invoice);

        // 写入审核日志（永久留存）
        PmsInvoiceAuditLog auditLog = new PmsInvoiceAuditLog();
        auditLog.setInvoiceId(invoiceId);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark(auditOpinion);
        // auditAction: 1=通过 2=驳回
        if ("2".equals(status))
        {
            auditLog.setAuditAction("1");
        }
        else
        {
            auditLog.setAuditAction("2");
        }
        pmsInvoiceAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int payInvoice(InvoicePaymentDTO dto)
    {
        PmsInvoice invoice = pmsInvoiceMapper.selectInvoiceById(dto.getInvoiceId());
        if (invoice == null)
        {
            throw new ServiceException("发票不存在");
        }
        if (!"2".equals(invoice.getStatus()) && !"3".equals(invoice.getStatus()) && !"6".equals(invoice.getStatus()))
        {
            throw new ServiceException("只有已审核或部分付款状态的发票才能付款");
        }
        if (dto.getPaymentAmount() == null || dto.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("付款金额必须大于0");
        }

        // 计算已付金额和未付金额
        BigDecimal currentPaid = invoice.getPaymentAmount() != null ? invoice.getPaymentAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal unpaid = totalAmount.subtract(currentPaid);
        if (dto.getPaymentAmount().compareTo(unpaid) > 0)
        {
            throw new ServiceException("本次付款金额不能超过未付金额（" + unpaid.toPlainString() + "元）");
        }

        // 1. 插入付款记录
        PmsInvoicePayment payment = new PmsInvoicePayment();
        payment.setPaymentNo(mkNumberRuleService.generateNumber("pms_payment"));
        payment.setInvoiceId(dto.getInvoiceId());
        payment.setContractId(invoice.getContractId());
        payment.setOrderId(invoice.getOrderId());
        payment.setSupplierId(invoice.getSupplierId());
        payment.setSupplierName(invoice.getSupplierName());
        payment.setPaymentAmount(dto.getPaymentAmount());
        payment.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : new Date());
        payment.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "0");
        payment.setBankName(dto.getBankName());
        payment.setBankAccount(dto.getBankAccount());
        payment.setPayer(StringUtils.isNotEmpty(dto.getPayer()) ? dto.getPayer() : SecurityUtils.getUsername());
        payment.setAttachment(dto.getAttachment());
        payment.setRemark(dto.getRemark());
        payment.setCreateBy(SecurityUtils.getUsername());
        pmsInvoicePaymentMapper.insertPayment(payment);

        // 2. 累加更新发票已付金额
        BigDecimal newPaidAmount = currentPaid.add(dto.getPaymentAmount());
        invoice.setPaymentAmount(newPaidAmount);
        invoice.setPayAmount(dto.getPaymentAmount());
        invoice.setPaymentDate(dto.getPaymentDate() != null ? dto.getPaymentDate() : new Date());
        invoice.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "0");
        if (dto.getBankName() != null) { invoice.setBankName(dto.getBankName()); }
        if (dto.getBankAccount() != null) { invoice.setBankAccount(dto.getBankAccount()); }
        // 付清时状态改为已付款，部分付款时改为部分付款
        if (newPaidAmount.compareTo(totalAmount) >= 0)
        {
            invoice.setStatus("3");
        }
        else
        {
            invoice.setStatus("6");
        }
        invoice.setUpdateBy(SecurityUtils.getUsername());
        pmsInvoiceMapper.updateInvoice(invoice);

        // 3. 同步更新合同已付金额
        if (invoice.getContractId() != null)
        {
            BigDecimal contractPaid = pmsInvoiceMapper.sumPaidAmountByContractId(invoice.getContractId());
            PmsContract contractUpdate = new PmsContract();
            contractUpdate.setContractId(invoice.getContractId());
            contractUpdate.setPaidAmount(contractPaid != null ? contractPaid : BigDecimal.ZERO);
            contractUpdate.setUpdateBy(SecurityUtils.getUsername());
            pmsContractMapper.updateContract(contractUpdate);
        }

        // 4. 同步更新订单已付金额
        if (invoice.getOrderId() != null)
        {
            BigDecimal orderPaid = pmsInvoiceMapper.sumPaidAmountByOrderId(invoice.getOrderId());
            PmsPurchaseOrder orderUpdate = new PmsPurchaseOrder();
            orderUpdate.setOrderId(invoice.getOrderId());
            orderUpdate.setPaidAmount(orderPaid != null ? orderPaid : BigDecimal.ZERO);
            orderUpdate.setUpdateBy(SecurityUtils.getUsername());
            pmsPurchaseOrderMapper.updatePurchaseOrder(orderUpdate);
        }

        return 1;
    }

    @Override
    public Map<String, Object> threeWayMatch(Long invoiceId)
    {
        PmsInvoice invoice = pmsInvoiceMapper.selectInvoiceById(invoiceId);
        if (invoice == null)
        {
            throw new ServiceException("发票不存在");
        }
        if (invoice.getOrderId() == null)
        {
            throw new ServiceException("发票未关联采购订单，无法进行三方匹配");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("invoiceNo", invoice.getInvoiceNo());
        result.put("orderNo", invoice.getOrderNo());

        // 1. 采购订单金额
        PmsPurchaseOrder order = pmsPurchaseOrderService.selectPurchaseOrderById(invoice.getOrderId());
        BigDecimal orderAmount = order != null && order.getTotalAmount() != null
                ? order.getTotalAmount() : BigDecimal.ZERO;
        result.put("orderAmount", orderAmount);

        // 2. 收货验收合格金额（已验收 status=2 或部分验收 status=1 的收货单）
        PmsReceive queryReceive = new PmsReceive();
        queryReceive.setOrderId(invoice.getOrderId());
        List<PmsReceive> allReceiveList = pmsReceiveMapper.selectReceiveList(queryReceive);
        List<PmsReceive> receiveList = new ArrayList<>();
        if (allReceiveList != null)
        {
            for (PmsReceive recv : allReceiveList)
            {
                if ("1".equals(recv.getStatus()) || "2".equals(recv.getStatus()))
                {
                    receiveList.add(recv);
                }
            }
        }
        BigDecimal receivedAmount = BigDecimal.ZERO;
        List<Map<String, Object>> receiveDetails = new ArrayList<>();
        if (receiveList != null)
        {
            for (PmsReceive recv : receiveList)
            {
                List<PmsReceiveDetail> details = pmsReceiveMapper.selectReceiveDetailByReceiveId(recv.getReceiveId());
                if (details != null)
                {
                    for (PmsReceiveDetail d : details)
                    {
                        if (d.getQualifiedQty() != null && d.getQualifiedQty().compareTo(BigDecimal.ZERO) > 0)
                        {
                            BigDecimal lineAmount = d.getQualifiedQty().multiply(
                                    d.getUnitPrice() != null ? d.getUnitPrice() : BigDecimal.ZERO);
                            receivedAmount = receivedAmount.add(lineAmount);
                            Map<String, Object> line = new HashMap<>();
                            line.put("receiveNo", recv.getReceiveNo());
                            line.put("materialCode", d.getMaterialCode());
                            line.put("materialName", d.getMaterialName());
                            line.put("qualifiedQty", d.getQualifiedQty());
                            line.put("unitPrice", d.getUnitPrice());
                            line.put("amount", lineAmount);
                            receiveDetails.add(line);
                        }
                    }
                }
            }
        }
        result.put("receivedAmount", receivedAmount);
        result.put("receiveDetails", receiveDetails);

        // 3. 发票金额
        BigDecimal invoiceAmount = invoice.getTotalAmount() != null
                ? invoice.getTotalAmount() : BigDecimal.ZERO;
        result.put("invoiceAmount", invoiceAmount);

        // 4. 匹配结果
        boolean orderMatch = orderAmount.compareTo(invoiceAmount) == 0;
        boolean receiveMatch = receivedAmount.compareTo(invoiceAmount) == 0;
        String matchResult;
        if (orderMatch && receiveMatch)
        {
            matchResult = "匹配成功";
        }
        else if (!orderMatch && !receiveMatch)
        {
            matchResult = "三方均不一致";
        }
        else if (!orderMatch)
        {
            matchResult = "订单与发票金额不一致";
        }
        else
        {
            matchResult = "收货与发票金额不一致";
        }
        result.put("matchResult", matchResult);
        result.put("orderMatch", orderMatch);
        result.put("receiveMatch", receiveMatch);

        // 5. 差异
        result.put("orderInvoiceDiff", orderAmount.subtract(invoiceAmount));
        result.put("receiveInvoiceDiff", receivedAmount.subtract(invoiceAmount));

        return result;
    }

    @Override
    public Map<String, Object> recognizeInvoice(org.springframework.web.multipart.MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty()) { throw new ServiceException("上传文件不能为空"); }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) { throw new ServiceException("仅支持PDF格式发票文件"); }

        String text;
        try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes()))
        {
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            stripper.setSortByPosition(true);
            text = stripper.getText(document);
        }
        catch (Exception e) { throw new ServiceException("PDF文件解析失败：" + e.getMessage()); }

        if (text == null || text.trim().isEmpty()) { throw new ServiceException("无法从PDF中提取文本，可能是扫描件或图片格式发票"); }

        Map<String, Object> result = new HashMap<>();

        if (text.contains("增值税专用发票")) { result.put("invoiceType", "0"); }
        else if (text.contains("普通发票")) { result.put("invoiceType", "1"); }
        else if (text.contains("电子发票")) { result.put("invoiceType", "2"); }

        java.util.regex.Pattern invoiceNoPattern = java.util.regex.Pattern.compile("发票号码[：:\\s]*([0-9]{20})");
        java.util.regex.Matcher m = invoiceNoPattern.matcher(text);
        if (m.find()) { result.put("invoiceNumber", m.group(1)); }

        java.util.regex.Pattern datePattern = java.util.regex.Pattern.compile("开票日期[：:\\s]*([0-9]{4}年[0-9]{2}月[0-9]{2}日)");
        m = datePattern.matcher(text);
        if (m.find()) { result.put("invoiceDate", m.group(1).replace("年", "-").replace("月", "-").replace("日", "")); }
        else {
            java.util.regex.Pattern datePattern2 = java.util.regex.Pattern.compile("开票日期[：:\\s]*([0-9]{4}-[0-9]{2}-[0-9]{2})");
            m = datePattern2.matcher(text);
            if (m.find()) { result.put("invoiceDate", m.group(1)); }
        }

        BigDecimal excludeTaxAmount = null;
        java.util.regex.Pattern amountPattern = java.util.regex.Pattern.compile("合.*?计.*?¥?\\s*([0-9]+(?:\\.[0-9]+)?)\\s+¥?\\s*([0-9]+(?:\\.[0-9]+)?)");
        m = amountPattern.matcher(text);
        if (m.find()) { excludeTaxAmount = new BigDecimal(m.group(1)); result.put("taxAmount", new BigDecimal(m.group(2))); }
        else {
            java.util.regex.Pattern amountOnlyPattern = java.util.regex.Pattern.compile("合.*?计.*?¥?\\s*([0-9]+(?:\\.[0-9]+)?)");
            m = amountOnlyPattern.matcher(text);
            if (m.find()) { excludeTaxAmount = new BigDecimal(m.group(1)); }
            java.util.regex.Pattern taxOnlyPattern = java.util.regex.Pattern.compile("合.*?计.*?¥?([0-9]+(?:\\.[0-9]+)?)\\s*$", java.util.regex.Pattern.MULTILINE);
            m = taxOnlyPattern.matcher(text);
            if (m.find()) { result.put("taxAmount", new BigDecimal(m.group(1))); }
        }

        java.util.regex.Pattern totalPattern = java.util.regex.Pattern.compile("价税合计.*?[（(]小写[）)]\\s*¥?([0-9]+(?:\\.[0-9]+)?)");
        m = totalPattern.matcher(text);
        if (m.find()) { result.put("totalAmount", new BigDecimal(m.group(1))); }
        else {
            java.util.regex.Pattern totalPattern2 = java.util.regex.Pattern.compile("价税合计[^0-9]*¥?([0-9]+(?:\\.[0-9]{1,2})?)");
            m = totalPattern2.matcher(text);
            if (m.find()) { result.put("totalAmount", new BigDecimal(m.group(1))); }
        }

        java.util.regex.Pattern buyerNamePattern = java.util.regex.Pattern.compile("名称[：:\\s]*([\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5a-zA-Z0-9（）()\\-·]{1,80})");
        m = buyerNamePattern.matcher(text);
        if (m.find()) { result.put("invoiceTitle", m.group(1).trim()); }

        java.util.regex.Pattern taxNoPattern = java.util.regex.Pattern.compile("纳税人识别号[：:\\s]*([A-Z0-9]{15,20})");
        m = taxNoPattern.matcher(text);
        if (m.find()) { result.put("taxNumber", m.group(1)); }

        // 税率 - 从文本中提取百分比
        java.util.regex.Pattern taxRatePattern = java.util.regex.Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)\\s*%");
        m = taxRatePattern.matcher(text);
        if (m.find()) { result.put("taxRate", new BigDecimal(m.group(1))); }

        if (excludeTaxAmount != null) { result.put("invoiceAmount", excludeTaxAmount); }

        // 如果没识别到税率，但有不含税金额和税额，计算税率
        if (!result.containsKey("taxRate") && excludeTaxAmount != null && result.containsKey("taxAmount"))
        {
            BigDecimal taxAmt = (BigDecimal) result.get("taxAmount");
            if (excludeTaxAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal rate = taxAmt.multiply(new BigDecimal("100"))
                    .divide(excludeTaxAmount, 2, java.math.RoundingMode.HALF_UP);
                result.put("taxRate", rate);
            }
        }

        return result;
    }
}
