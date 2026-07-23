package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkInvoice;
import com.ruoyi.mk.mapper.MkInvoiceMapper;
import com.ruoyi.mk.service.IMkInvoiceService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 发票 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkInvoiceServiceImpl implements IMkInvoiceService
{
    @Autowired
    private MkInvoiceMapper mkInvoiceMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkInvoice> selectInvoiceList(MkInvoice invoice)
    {
        return mkInvoiceMapper.selectInvoiceList(invoice);
    }

    @Override
    public MkInvoice selectInvoiceById(Long invoiceId)
    {
        return mkInvoiceMapper.selectInvoiceById(invoiceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInvoice(MkInvoice invoice)
    {
        if (invoice.getInvoiceStatus() == null)
        {
            invoice.setInvoiceStatus("1");
        }
        if (invoice.getTaxRate() == null)
        {
            invoice.setTaxRate(new BigDecimal("13.00"));
        }
        // 计算税额
        if (invoice.getInvoiceAmount() != null && invoice.getTaxRate() != null)
        {
            BigDecimal taxAmount = invoice.getInvoiceAmount()
                .multiply(invoice.getTaxRate())
                .divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
            invoice.setTaxAmount(taxAmount);
        }
        // 自动生成发票编号
        if (invoice.getInvoiceNo() == null || invoice.getInvoiceNo().isEmpty())
        {
            invoice.setInvoiceNo(mkNumberRuleService.generateNumber("invoice"));
        }
        return mkInvoiceMapper.insertInvoice(invoice);
    }

    @Override
    public int updateInvoice(MkInvoice invoice)
    {
        return mkInvoiceMapper.updateInvoice(invoice);
    }

    @Override
    public int deleteInvoiceByIds(Long[] invoiceIds)
    {
        return mkInvoiceMapper.deleteInvoiceByIds(invoiceIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int issueInvoice(Long invoiceId)
    {
        MkInvoice invoice = mkInvoiceMapper.selectInvoiceById(invoiceId);
        if (invoice == null)
        {
            throw new ServiceException("发票不存在");
        }
        if (!"0".equals(invoice.getInvoiceStatus()))
        {
            throw new ServiceException("只有待开票状态的发票才能开票");
        }
        invoice.setInvoiceStatus("1");
        invoice.setInvoiceDate(new Date());
        invoice.setUpdateBy(SecurityUtils.getUsername());
        return mkInvoiceMapper.updateInvoice(invoice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidInvoice(Long invoiceId)
    {
        MkInvoice invoice = mkInvoiceMapper.selectInvoiceById(invoiceId);
        if (invoice == null)
        {
            throw new ServiceException("发票不存在");
        }
        if (!"1".equals(invoice.getInvoiceStatus()))
        {
            throw new ServiceException("只有已开票状态的发票才能作废");
        }
        invoice.setInvoiceStatus("2");
        invoice.setUpdateBy(SecurityUtils.getUsername());
        return mkInvoiceMapper.updateInvoice(invoice);
    }

    @Override
    public Map<String, Object> recognizeInvoice(MultipartFile file) throws Exception
    {
        // 校验文件
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("上传文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf"))
        {
            throw new ServiceException("仅支持PDF格式发票文件");
        }

        // 使用 PDFBox 提取PDF文本
        String text;
        try (PDDocument document = Loader.loadPDF(file.getBytes()))
        {
            PDFTextStripper stripper = new PDFTextStripper();
            // 按照PDF原始顺序提取文本
            stripper.setSortByPosition(true);
            text = stripper.getText(document);
        }
        catch (Exception e)
        {
            throw new ServiceException("PDF文件解析失败：" + e.getMessage());
        }

        if (text == null || text.trim().isEmpty())
        {
            throw new ServiceException("无法从PDF中提取文本，可能是扫描件或图片格式发票");
        }

        Map<String, Object> result = new HashMap<>();

        // 统一匹配全角冒号和半角冒号
        // [：:\s]* 匹配全角冒号、半角冒号、空格等

        // 0. 发票类型 - 根据发票标题判断
        if (text.contains("增值税专用发票"))
        {
            result.put("invoiceType", "0"); // 增值税专用发票
        }
        else if (text.contains("普通发票"))
        {
            result.put("invoiceType", "1"); // 增值税普通发票
        }
        else if (text.contains("电子发票"))
        {
            result.put("invoiceType", "2"); // 电子发票
        }

        // 1. 发票号码 - 匹配20位数字
        Pattern invoiceNoPattern = Pattern.compile("发票号码[：:\\s]*([0-9]{20})");
        Matcher m = invoiceNoPattern.matcher(text);
        if (m.find())
        {
            result.put("invoiceNo", m.group(1));
        }

        // 2. 开票日期 - 支持 2024年01月15日 或 2024-01-15 格式
        Pattern datePattern = Pattern.compile("开票日期[：:\\s]*([0-9]{4}年[0-9]{2}月[0-9]{2}日)");
        m = datePattern.matcher(text);
        if (m.find())
        {
            String dateStr = m.group(1).replace("年", "-").replace("月", "-").replace("日", "");
            result.put("invoiceDate", dateStr);
        }
        else
        {
            Pattern datePattern2 = Pattern.compile("开票日期[：:\\s]*([0-9]{4}-[0-9]{2}-[0-9]{2})");
            m = datePattern2.matcher(text);
            if (m.find())
            {
                result.put("invoiceDate", m.group(1));
            }
        }

        // 3. 金额（不含税）和税额 - 从"合计"行提取
        // 真实格式: "合 计 ¥136.63 ¥1.37" 或 "合        计 ¥1229.06 ¥73.74"
        // 合与计之间可能有多种空白字符（含全角空格\u3000、不间断空格\u00A0等）
        // 使用 .*? 匹配合与计之间的任意字符（不跨行）
        BigDecimal excludeTaxAmount = null;
        Pattern amountPattern = Pattern.compile("合.*?计.*?¥?\\s*([0-9]+(?:\\.[0-9]+)?)\\s+¥?\\s*([0-9]+(?:\\.[0-9]+)?)");
        m = amountPattern.matcher(text);
        if (m.find())
        {
            excludeTaxAmount = new BigDecimal(m.group(1));
            result.put("taxAmount", new BigDecimal(m.group(2)));
        }
        else
        {
            // 备选：单独匹配金额和税额
            Pattern amountOnlyPattern = Pattern.compile("合.*?计.*?¥?\\s*([0-9]+(?:\\.[0-9]+)?)");
            m = amountOnlyPattern.matcher(text);
            if (m.find())
            {
                excludeTaxAmount = new BigDecimal(m.group(1));
            }
            // 税额: 合计行的最后一个金额
            Pattern taxOnlyPattern = Pattern.compile("合.*?计.*?¥?([0-9]+(?:\\.[0-9]+)?)\\s*$", Pattern.MULTILINE);
            m = taxOnlyPattern.matcher(text);
            if (m.find())
            {
                result.put("taxAmount", new BigDecimal(m.group(1)));
            }
        }

        // 4. 价税合计 - 从"价税合计"行提取小写金额
        // 真实格式: "价税合计（大写） 壹佰叁拾捌圆整 （小写）¥138.00"
        Pattern totalPattern = Pattern.compile("价税合计.*?[（(]小写[）)]\\s*¥?([0-9]+(?:\\.[0-9]+)?)");
        m = totalPattern.matcher(text);
        if (m.find())
        {
            result.put("totalAmount", new BigDecimal(m.group(1)));
        }
        else
        {
            // 备选：价税合计后最后一个数字
            Pattern totalPattern2 = Pattern.compile("价税合计[^0-9]*¥?([0-9]+(?:\\.[0-9]{1,2})?)");
            m = totalPattern2.matcher(text);
            if (m.find())
            {
                result.put("totalAmount", new BigDecimal(m.group(1)));
            }
        }

        // 5. 购买方名称（发票抬头）- 取第一个"名称"后的内容
        // 真实格式: "购 名称：武汉智邦软件技术有限公司 销 名称：沾益龙华..."
        Pattern buyerNamePattern = Pattern.compile("名称[：:\\s]*([\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5a-zA-Z0-9（）()\\-·]{1,80})");
        m = buyerNamePattern.matcher(text);
        if (m.find())
        {
            result.put("invoiceTitle", m.group(1).trim());
        }

        // 6. 购买方纳税人识别号
        // 真实格式: "统一社会信用代码/纳税人识别号：914201113033802668"
        Pattern taxNoPattern = Pattern.compile("纳税人识别号[：:\\s]*([A-Z0-9]{15,20})");
        m = taxNoPattern.matcher(text);
        if (m.find())
        {
            result.put("taxNo", m.group(1));
        }

        // 7. 税率 - 从明细行提取百分比
        // 真实格式: 项目行中有 "1%" 或 "6%" 或 "3%"
        Pattern taxRatePattern = Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)\\s*%");
        m = taxRatePattern.matcher(text);
        if (m.find())
        {
            result.put("taxRate", new BigDecimal(m.group(1)));
        }

        // 开票金额 = 价税合计（含税总额）
        // 如果识别到价税合计，用价税合计作为开票金额
        if (result.containsKey("totalAmount"))
        {
            result.put("invoiceAmount", result.get("totalAmount"));
        }
        else if (excludeTaxAmount != null)
        {
            // 没识别到价税合计时，用不含税金额作为开票金额
            result.put("invoiceAmount", excludeTaxAmount);
        }

        // 如果识别到不含税金额和税额，但没识别到税率，计算税率
        if (!result.containsKey("taxRate") && excludeTaxAmount != null && result.containsKey("taxAmount"))
        {
            BigDecimal taxAmount = (BigDecimal) result.get("taxAmount");
            if (excludeTaxAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal rate = taxAmount.multiply(new BigDecimal("100"))
                    .divide(excludeTaxAmount, 2, BigDecimal.ROUND_HALF_UP);
                result.put("taxRate", rate);
            }
        }

        return result;
    }
}
