package com.ruoyi.fin.service.impl;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinApOrder;
import com.ruoyi.fin.domain.FinApWriteoff;
import com.ruoyi.fin.domain.vo.ApInvoiceRefVO;
import com.ruoyi.fin.engine.FinNumberGenerator;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinApOrderMapper;
import com.ruoyi.fin.mapper.FinApWriteoffMapper;
import com.ruoyi.fin.mapper.FinRefMapper;
import com.ruoyi.fin.service.IFinApOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应付单 Service 实现
 *
 * <p><b>与 PMS 的边界（本类最重要的约定）</b></p>
 * <ol>
 *   <li>应付单<b>由已审核发票生成</b>（pms_invoice.status='2'），不手工录入金额</li>
 *   <li>已付金额<b>从 PMS 同步</b>，不在财务侧重复登记付款，
 *       否则会与 pms_invoice_payment 造成重复付款</li>
 *   <li>核销是<b>财务确认动作</b>（财务经理操作），与出纳付钱分开，留痕</li>
 * </ol>
 *
 * @author ruoyi
 */
@Service
public class FinApOrderServiceImpl implements IFinApOrderService
{
    private static final Logger log = LoggerFactory.getLogger(FinApOrderServiceImpl.class);

    private static final int BATCH_LIMIT = 200;

    @Autowired
    private FinApOrderMapper apMapper;

    @Autowired
    private FinApWriteoffMapper writeoffMapper;

    @Autowired
    private FinRefMapper refMapper;

    @Autowired
    private FinNumberGenerator numberGenerator;

    @Override
    @DataScope(deptAlias = "a", userAlias = "a")
    public List<FinApOrder> selectApList(FinApOrder ap)
    {
        return apMapper.selectApList(ap);
    }

    @Override
    public FinApOrder selectApById(Long apId)
    {
        return apMapper.selectApById(apId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateFromInvoice()
    {
        List<ApInvoiceRefVO> list = refMapper.selectUnmatchedInvoice(BATCH_LIMIT);
        int success = 0;
        int skip = 0;
        int fail = 0;
        String user = SecurityUtils.getUsername();

        for (ApInvoiceRefVO inv : list)
        {
            try
            {
                generateOne(inv, user);
                success++;
            }
            catch (RuntimeException e)
            {
                // 业务性拒绝（金额勾稽不符、重复来源等）记为跳过，
                // 系统性异常记为失败，便于运维区分
                String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                if (isBusinessReject(msg))
                {
                    skip++;
                    log.warn("发票 {} 跳过：{}", inv.getInvoiceNo(), msg);
                }
                else
                {
                    fail++;
                    log.warn("应付单生成失败：{} —— {}", inv.getInvoiceNo(), msg);
                }
            }
        }
        Map<String, Object> r = new HashMap<>();
        r.put("total", list.size());
        r.put("success", success);
        r.put("skip", skip);
        r.put("fail", fail);
        return r;
    }

    /**
     * 单张发票生成应付单。
     */
    private void generateOne(ApInvoiceRefVO inv, String user)
    {
        // 幂等前置：数据库唯一索引兜底，此处先查给出友好提示
        FinApOrder exist = apMapper.selectBySource("pms", "invoice", inv.getInvoiceId());
        if (exist != null)
        {
            throw FinException.apSourceExists(inv.getInvoiceNo(), exist.getApNo());
        }

        // 金额勾稽校验：不含税 + 税额 = 价税合计
        BigDecimal excl = nz(inv.getInvoiceAmount());
        BigDecimal tax = nz(inv.getTaxAmount());
        BigDecimal total = nz(inv.getTotalAmount());
        if (total.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw FinException.apAmountInvalid(inv.getInvoiceNo(),
                    total.toPlainString(), excl.toPlainString(), tax.toPlainString());
        }
        BigDecimal sum = excl.add(tax);
        // 允许 0.01 元舍入差
        if (sum.subtract(total).abs().compareTo(new BigDecimal("0.01")) > 0)
        {
            throw FinException.apAmountInvalid(inv.getInvoiceNo(),
                    total.toPlainString(), excl.toPlainString(), tax.toPlainString());
        }

        Date apDate = inv.getInvoiceDate() == null ? new Date() : inv.getInvoiceDate();

        FinApOrder ap = new FinApOrder();
        ap.setApNo(numberGenerator.generate(FinNumberGenerator.RULE_AP_ORDER));
        ap.setSourceModule("pms");
        ap.setSourceType("invoice");
        ap.setSourceId(inv.getInvoiceId());
        ap.setSourceKey(inv.getInvoiceNo());
        ap.setPartnerId(inv.getPartnerId());
        ap.setPartnerName(inv.getSupplierName());
        ap.setSupplierId(inv.getSupplierId());
        ap.setApDate(apDate);
        ap.setPeriod(periodOf(apDate));
        ap.setAmountExclTax(excl);
        ap.setTaxAmount(tax);
        ap.setTotalAmount(total);
        ap.setPaidAmount(nz(inv.getPaymentAmount()));
        ap.setTaxRate(inv.getTaxRate() == null ? BigDecimal.ZERO : inv.getTaxRate());
        ap.setInvoiceNo(inv.getInvoiceNo());
        ap.setInvoiceCode(inv.getInvoiceCode());
        ap.setInvoiceDate(inv.getInvoiceDate());
        ap.setPaymentDays(inv.getPaymentDays() == null ? 0 : inv.getPaymentDays());
        ap.setDueDate(addDays(apDate, ap.getPaymentDays()));
        ap.setStatus(resolveStatus(total, ap.getPaidAmount()));
        ap.setUserId(SecurityUtils.getUserId());
        ap.setCreateBy(user);
        ap.setRemark("由采购发票 " + inv.getInvoiceNo() + " 生成");

        try
        {
            apMapper.insertAp(ap);
        }
        catch (DuplicateKeyException e)
        {
            FinApOrder dup = apMapper.selectBySource("pms", "invoice", inv.getInvoiceId());
            throw FinException.apSourceExists(inv.getInvoiceNo(),
                    dup == null ? "-" : dup.getApNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncPaidAmount()
    {
        FinApOrder cond = new FinApOrder();
        // 只同步未核销的
        cond.setStatus(FinApOrder.STATUS_OPEN);
        List<FinApOrder> list = apMapper.selectApList(cond);

        // 收集来源发票ID（应付单里 source_id 即 pms_invoice.invoice_id）
        List<Long> invoiceIds = new ArrayList<>();
        for (FinApOrder ap : list)
        {
            if ("pms".equals(ap.getSourceModule()) && ap.getSourceId() != null)
            {
                invoiceIds.add(ap.getSourceId());
            }
        }
        if (invoiceIds.isEmpty())
        {
            return 0;
        }

        List<ApInvoiceRefVO> paid = refMapper.selectInvoicePaidAmount(invoiceIds);
        Map<Long, BigDecimal> paidMap = new HashMap<>();
        for (ApInvoiceRefVO p : paid)
        {
            paidMap.put(p.getInvoiceId(), nz(p.getPaymentAmount()));
        }

        int updated = 0;
        String user = SecurityUtils.getUsername();
        for (FinApOrder ap : list)
        {
            BigDecimal remote = paidMap.get(ap.getSourceId());
            if (remote == null)
            {
                continue;
            }
            if (remote.compareTo(nz(ap.getPaidAmount())) != 0)
            {
                apMapper.updatePaidAmount(ap.getApId(), remote, user);
                log.info("应付单 {} 已付金额同步：{} → {}", ap.getApNo(), ap.getPaidAmount(), remote);
                updated++;
            }
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int writeoff(Long apId, BigDecimal writeoffAmount, String remark)
    {
        if (writeoffAmount == null || writeoffAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw FinException.apWriteoffInvalid();
        }

        // 行锁：防并发重复核销
        FinApOrder ap = apMapper.selectApForUpdate(apId);
        if (ap == null)
        {
            throw new IllegalArgumentException("应付单不存在或已删除");
        }
        if (FinApOrder.STATUS_SETTLED.equals(ap.getStatus()))
        {
            throw FinException.apAlreadySettled(ap.getApNo());
        }

        BigDecimal unpaid = nz(ap.getTotalAmount()).subtract(nz(ap.getPaidAmount()));
        if (writeoffAmount.compareTo(unpaid) > 0)
        {
            throw FinException.apWriteoffExceed(writeoffAmount.toPlainString(), unpaid.toPlainString());
        }

        // 已付金额按实际付讫推进；核销额用于记录财务确认，不重复加总
        BigDecimal newPaid = nz(ap.getPaidAmount()).add(writeoffAmount);
        int rows = apMapper.updatePaidAmount(apId, newPaid, SecurityUtils.getUsername());

        Date today = new Date();
        FinApWriteoff w = new FinApWriteoff();
        w.setApId(apId);
        w.setWriteoffAmount(writeoffAmount);
        w.setWriteoffDate(today);
        w.setPeriod(periodOf(today));
        w.setUserId(SecurityUtils.getUserId());
        w.setCreateBy(SecurityUtils.getUsername());
        w.setRemark(remark);
        writeoffMapper.insertWriteoff(w);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidAp(Long[] apIds, String reason)
    {
        if (apIds == null || apIds.length == 0)
        {
            return 0;
        }
        for (Long id : apIds)
        {
            FinApOrder ap = apMapper.selectApForUpdate(id);
            if (ap == null)
            {
                continue;
            }
            if (FinApOrder.STATUS_SETTLED.equals(ap.getStatus()))
            {
                throw FinException.apAlreadySettled(ap.getApNo());
            }
            if (FinApOrder.STATUS_VOID.equals(ap.getStatus()))
            {
                throw FinException.apAlreadyVoid(ap.getApNo());
            }
            ap.setStatus(FinApOrder.STATUS_VOID);
            ap.setUpdateBy(SecurityUtils.getUsername());
            ap.setRemark("作废原因：" + reason);
            apMapper.updateAp(ap);
        }
        return apIds.length;
    }

    @Override
    public List<Map<String, Object>> aging(Date today)
    {
        return apMapper.selectAging(today == null ? new Date() : today);
    }

    @Override
    public List<FinApOrder> overdue(Date today)
    {
        return apMapper.selectOverdue(today == null ? new Date() : today, 100);
    }

    @Override
    public Map<String, Object> summary(String period)
    {
        return apMapper.sumByPeriod(period);
    }

    // ==================== 私有方法 ====================

    /**
     * 判断异常是否为业务性拒绝（数据问题）而非系统故障。
     *
     * <p>业务性拒绝的文案均以「发票」或「应付单」开头（源自 10038/10039 的消息模板），
     * 系统故障则为数据库、编号服务等异常。</p>
     *
     * @param msg 异常消息
     * @return true=业务拒绝
     */
    private boolean isBusinessReject(String msg)
    {
        return msg.startsWith("发票") || msg.startsWith("应付单");
    }

    private String resolveStatus(BigDecimal total, BigDecimal paid)
    {
        BigDecimal p = nz(paid);
        if (p.compareTo(BigDecimal.ZERO) <= 0)
        {
            return FinApOrder.STATUS_OPEN;
        }
        if (p.compareTo(total) >= 0)
        {
            return FinApOrder.STATUS_SETTLED;
        }
        return FinApOrder.STATUS_PARTIAL;
    }

    private Date addDays(Date base, int days)
    {
        if (days <= 0)
        {
            return base;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(base);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }

    private String periodOf(Date date)
    {
        LocalDate d = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return String.format("%04d%02d", d.getYear(), d.getMonthValue());
    }

    private static BigDecimal nz(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }
}