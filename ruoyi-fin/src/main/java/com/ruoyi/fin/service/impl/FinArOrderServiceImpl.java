package com.ruoyi.fin.service.impl;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinArOrder;
import com.ruoyi.fin.domain.FinArWriteoff;
import com.ruoyi.fin.domain.FinBadDebt;
import com.ruoyi.fin.domain.vo.ArReceiptRefVO;
import com.ruoyi.fin.domain.vo.ArShipmentRefVO;
import com.ruoyi.fin.engine.FinNumberGenerator;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinArOrderMapper;
import com.ruoyi.fin.mapper.FinArWriteoffMapper;
import com.ruoyi.fin.mapper.FinRefMapper;
import com.ruoyi.fin.service.IFinArOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应收单 Service 实现
 *
 * <p><b>与 MK 的边界（同 P3 应付的设计）</b></p>
 * <ol>
 *   <li>应收单由已审核发货单（mk_shipment.status IN 2,3,4）生成，金额不可手改</li>
 *   <li>已收金额从 mk_payment_record（已确认）同步，不重复登记收款</li>
 *   <li>核销由财务经理操作并留痕，与实际收款分开</li>
 * </ol>
 *
 * <p><b>税额拆分</b>：mk_shipment 只有价税合计 total_amount，
 * 本服务按税率拆分为不含税额与销项税额。</p>
 *
 * @author ruoyi
 */
@Service
public class FinArOrderServiceImpl implements IFinArOrderService
{
    private static final Logger log = LoggerFactory.getLogger(FinArOrderServiceImpl.class);

    private static final int BATCH_LIMIT = 200;

    /** 默认销售税率（可由 fin_fiscal_config.output_tax_rate 覆盖） */
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.13");

    /** 账龄计提比例默认值 */
    private static final Map<String, BigDecimal> DEFAULT_AGING_RATIO = new HashMap<>();

    static
    {
        DEFAULT_AGING_RATIO.put("1-30", new BigDecimal("0.05"));
        DEFAULT_AGING_RATIO.put("31-60", new BigDecimal("0.10"));
        DEFAULT_AGING_RATIO.put("61-90", new BigDecimal("0.30"));
        DEFAULT_AGING_RATIO.put("90+", new BigDecimal("0.50"));
    }

    @Autowired
    private FinArOrderMapper arMapper;

    @Autowired
    private FinArWriteoffMapper writeoffMapper;

    @Autowired
    private FinRefMapper refMapper;

    @Autowired
    private FinNumberGenerator numberGenerator;

    @Override
    @DataScope(deptAlias = "r", userAlias = "r")
    public List<FinArOrder> selectArList(FinArOrder ar)
    {
        return arMapper.selectArList(ar);
    }

    @Override
    public FinArOrder selectArById(Long arId)
    {
        return arMapper.selectArById(arId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateFromShipment()
    {
        List<ArShipmentRefVO> list = refMapper.selectUnmatchedShipment(BATCH_LIMIT);
        int success = 0;
        int skip = 0;
        int fail = 0;
        String user = SecurityUtils.getUsername();
        BigDecimal taxRate = DEFAULT_TAX_RATE;

        for (ArShipmentRefVO sh : list)
        {
            try
            {
                // 幂等前置：唯一索引兜底，此处先查给友好提示
                if (arMapper.selectBySource("mk", "shipment", sh.getShipmentId()) != null)
                {
                    skip++;
                    log.warn("发货单 {} 已生成应收单，跳过", sh.getShipmentNo());
                    continue;
                }
                generateOne(sh, taxRate, user);
                success++;
            }
            catch (RuntimeException e)
            {
                String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                if (msg.startsWith("发货单") || msg.startsWith("应收单"))
                {
                    skip++;
                }
                else
                {
                    fail++;
                }
                log.warn("应收单生成失败：{} —— {}", sh.getShipmentNo(), msg);
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
     * 单张发货单生成应收单。
     */
    private void generateOne(ArShipmentRefVO sh, BigDecimal taxRate, String user)
    {
        BigDecimal total = nz(sh.getTotalAmount());
        if (total.compareTo(BigDecimal.ZERO) <= 0)
        {
            // 0 元发货单不生成应收（如样品赠送），跳过而非报错
            throw FinException.arAmountInvalid(sh.getShipmentNo(), total.toPlainString());
        }

        // 按税率拆分价税合计
        BigDecimal divisor = BigDecimal.ONE.add(taxRate);
        BigDecimal exclTax = total.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal tax = total.subtract(exclTax);

        Date arDate = sh.getShipmentDate() == null ? new Date() : sh.getShipmentDate();

        FinArOrder ar = new FinArOrder();
        ar.setArNo(numberGenerator.generate(FinNumberGenerator.RULE_AR_ORDER));
        ar.setSourceModule("mk");
        ar.setSourceType("shipment");
        ar.setSourceId(sh.getShipmentId());
        ar.setSourceKey(sh.getShipmentNo());
        ar.setPartnerId(sh.getPartnerId());
        ar.setPartnerName(sh.getCustomerName());
        ar.setCustomerId(sh.getCustomerId());
        ar.setArDate(arDate);
        ar.setPeriod(periodOf(arDate));
        ar.setAmountExclTax(exclTax);
        ar.setTaxAmount(tax);
        ar.setTotalAmount(total);
        ar.setReceivedAmount(BigDecimal.ZERO);
        ar.setBadDebtAmount(BigDecimal.ZERO);
        ar.setTaxRate(taxRate);
        ar.setPaymentDays(sh.getPaymentDays() == null ? 0 : sh.getPaymentDays());
        ar.setDueDate(addDays(arDate, ar.getPaymentDays()));
        ar.setStatus(FinArOrder.STATUS_OPEN);
        ar.setUserId(SecurityUtils.getUserId());
        ar.setCreateBy(user);
        ar.setRemark("由发货单 " + sh.getShipmentNo() + " 生成");

        try
        {
            arMapper.insertAr(ar);
        }
        catch (DuplicateKeyException e)
        {
            throw FinException.arSourceExists(sh.getShipmentNo());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncReceivedAmount()
    {
        FinArOrder cond = new FinArOrder();
        cond.setStatus(FinArOrder.STATUS_OPEN);
        List<FinArOrder> list = arMapper.selectArList(cond);

        // 收集客户ID
        List<Long> customerIds = new ArrayList<>();
        for (FinArOrder ar : list)
        {
            if ("mk".equals(ar.getSourceModule()) && ar.getCustomerId() != null)
            {
                customerIds.add(ar.getCustomerId());
            }
        }
        if (customerIds.isEmpty())
        {
            return 0;
        }

        List<ArReceiptRefVO> received = refMapper.selectCustomerReceivedAmount(customerIds);
        Map<Long, BigDecimal> recvMap = new HashMap<>();
        for (ArReceiptRefVO r : received)
        {
            recvMap.put(r.getCustomerId(), nz(r.getReceivedAmount()));
        }

        int updated = 0;
        String user = SecurityUtils.getUsername();
        for (FinArOrder ar : list)
        {
            BigDecimal remote = recvMap.get(ar.getCustomerId());
            if (remote == null)
            {
                continue;
            }
            // 客户维度已收超过该单应收额时，按应收额封顶（避免负未收）
            BigDecimal capped = remote.compareTo(nz(ar.getTotalAmount())) > 0
                    ? nz(ar.getTotalAmount()) : remote;
            if (capped.compareTo(nz(ar.getReceivedAmount())) != 0)
            {
                arMapper.updateReceivedAmount(ar.getArId(), capped, user);
                log.info("应收单 {} 已收金额同步：{} → {}", ar.getArNo(), ar.getReceivedAmount(), capped);
                updated++;
            }
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int writeoff(Long arId, BigDecimal writeoffAmount, String remark)
    {
        if (writeoffAmount == null || writeoffAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw FinException.arWriteoffInvalid();
        }

        // 行锁防并发重复核销
        FinArOrder ar = arMapper.selectArForUpdate(arId);
        if (ar == null)
        {
            throw new IllegalArgumentException("应收单不存在或已删除");
        }
        if (FinArOrder.STATUS_SETTLED.equals(ar.getStatus()))
        {
            throw FinException.arAlreadySettled(ar.getArNo());
        }

        BigDecimal unreceived = nz(ar.getTotalAmount()).subtract(nz(ar.getReceivedAmount()));
        if (writeoffAmount.compareTo(unreceived) > 0)
        {
            throw FinException.arWriteoffExceed(writeoffAmount.toPlainString(),
                    unreceived.toPlainString());
        }

        BigDecimal newRecv = nz(ar.getReceivedAmount()).add(writeoffAmount);
        int rows = arMapper.updateReceivedAmount(arId, newRecv, SecurityUtils.getUsername());

        Date today = new Date();
        FinArWriteoff w = new FinArWriteoff();
        w.setArId(arId);
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
    public int invoice(Long arId, String invoiceNo, String invoiceCode, Date invoiceDate)
    {
        FinArOrder ar = arMapper.selectArById(arId);
        if (ar == null)
        {
            throw new IllegalArgumentException("应收单不存在或已删除");
        }
        if (invoiceNo == null || invoiceNo.trim().isEmpty())
        {
            throw FinException.invoiceNoRequired();
        }
        return arMapper.updateInvoice(arId, invoiceNo.trim(), invoiceCode, invoiceDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidAr(Long[] arIds, String reason)
    {
        if (arIds == null || arIds.length == 0)
        {
            return 0;
        }
        for (Long id : arIds)
        {
            FinArOrder ar = arMapper.selectArForUpdate(id);
            if (ar == null)
            {
                continue;
            }
            if (FinArOrder.STATUS_SETTLED.equals(ar.getStatus()))
            {
                throw FinException.arAlreadySettled(ar.getArNo());
            }
            if (FinArOrder.STATUS_VOID.equals(ar.getStatus()))
            {
                throw FinException.arAlreadyVoid(ar.getArNo());
            }
            ar.setStatus(FinArOrder.STATUS_VOID);
            ar.setUpdateBy(SecurityUtils.getUsername());
            ar.setRemark("作废原因：" + reason);
            arMapper.updateAr(ar);
        }
        return arIds.length;
    }

    @Override
    public List<Map<String, Object>> aging(Date today)
    {
        return arMapper.selectAging(today == null ? new Date() : today);
    }

    @Override
    public List<FinArOrder> overdue(Date today)
    {
        return arMapper.selectOverdue(today == null ? new Date() : today, 100);
    }

    @Override
    public Map<String, Object> summary(String period)
    {
        return arMapper.sumByPeriod(period);
    }

    @Override
    public List<Map<String, Object>> balanceByCustomer()
    {
        return arMapper.selectBalanceByCustomer();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FinBadDebt> provisionBadDebt(String period, Date today)
    {
        List<FinBadDebt> result = new ArrayList<>();
        if (period == null || period.trim().isEmpty())
        {
            throw new IllegalArgumentException("计提期间不能为空");
        }
        Date base = today == null ? new Date() : today;

        // 按应收单逐笔计提（比客户汇总更精准，可追溯到单据）
        FinArOrder cond = new FinArOrder();
        cond.setStatus(FinArOrder.STATUS_OPEN);
        List<FinArOrder> openList = arMapper.selectArList(cond);
        FinArOrder condPartial = new FinArOrder();
        condPartial.setStatus(FinArOrder.STATUS_PARTIAL);
        openList.addAll(arMapper.selectArList(condPartial));

        String user = SecurityUtils.getUsername();
        BigDecimal totalProvision = BigDecimal.ZERO;

        for (FinArOrder ar : openList)
        {
            BigDecimal unreceived = nz(ar.getTotalAmount()).subtract(nz(ar.getReceivedAmount()));
            if (unreceived.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            if (ar.getDueDate() == null || !ar.getDueDate().before(base))
            {
                // 未到期不计提
                continue;
            }

            long days = ChronoUnit.DAYS.between(
                    ar.getDueDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    base.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            BigDecimal ratio = ratioOf(days);
            if (ratio.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }

            // 已计提部分不重复计提
            BigDecimal already = nz(arMapper.sumBadDebtByAr(ar.getArId()));
            BigDecimal target = unreceived.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal current = target.subtract(already);
            if (current.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }

            FinBadDebt bd = new FinBadDebt();
            bd.setProvisionNo(numberGenerator.generate(FinNumberGenerator.RULE_BAD_DEBT));
            bd.setPeriod(period);
            bd.setArId(ar.getArId());
            bd.setPartnerId(ar.getPartnerId());
            bd.setPartnerName(ar.getPartnerName());
            bd.setReceivableAmount(unreceived);
            bd.setProvisionRatio(ratio);
            bd.setProvisionAmount(current);
            bd.setProvisionType(FinBadDebt.TYPE_AGE);
            bd.setUserId(SecurityUtils.getUserId());
            bd.setCreateBy(user);
            bd.setRemark("账龄 " + days + " 天，计提比例 " + ratio.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%");
            writeoffMapper.insertBadDebt(bd);

            // 同步应收单的已计提坏账
            FinArOrder upd = new FinArOrder();
            upd.setArId(ar.getArId());
            upd.setBadDebtAmount(already.add(current));
            upd.setUpdateBy(user);
            arMapper.updateAr(upd);

            result.add(bd);
            totalProvision = totalProvision.add(current);
        }

        log.info("坏账计提完成：期间 {}，计提 {} 笔，合计 {} 元",
                period, result.size(), totalProvision);
        return result;
    }

    // ==================== 私有方法 ====================

    /**
     * 按逾期天数取计提比例。
     */
    private BigDecimal ratioOf(long days)
    {
        if (days <= 30)
        {
            return DEFAULT_AGING_RATIO.get("1-30");
        }
        if (days <= 60)
        {
            return DEFAULT_AGING_RATIO.get("31-60");
        }
        if (days <= 90)
        {
            return DEFAULT_AGING_RATIO.get("61-90");
        }
        return DEFAULT_AGING_RATIO.get("90+");
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