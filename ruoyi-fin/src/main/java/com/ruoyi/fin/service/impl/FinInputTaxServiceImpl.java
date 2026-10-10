package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinApOrder;
import com.ruoyi.fin.domain.FinInputTax;
import com.ruoyi.fin.domain.FinInventoryValuation;
import com.ruoyi.fin.mapper.FinApOrderMapper;
import com.ruoyi.fin.mapper.FinInputTaxMapper;
import com.ruoyi.fin.service.IFinInputTaxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 进项税抵扣与存货跌价准备 Service 实现
 *
 * @author ruoyi
 */
@Service
public class FinInputTaxServiceImpl implements IFinInputTaxService
{
    private static final Logger log = LoggerFactory.getLogger(FinInputTaxServiceImpl.class);

    /** 认证抵扣期限（月），依据《增值税暂行条例》 */
    private static final int DEDUCT_PERIOD_MONTHS = 12;

    /** 提前预警天数 */
    private static final int WARN_DAYS = 30;

    @Autowired
    private FinInputTaxMapper taxMapper;

    @Autowired
    private FinApOrderMapper apMapper;

    @Override
    public List<FinInputTax> selectInputTaxList(FinInputTax tax)
    {
        List<FinInputTax> list = taxMapper.selectInputTaxList(tax);
        // 运行时计算抵扣截止日与剩余天数，供界面提示
        for (FinInputTax t : list)
        {
            t.setDeductDeadline(calcDeadline(t.getInvoiceDate()));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateFromAp(Long apId)
    {
        FinApOrder ap = apMapper.selectApById(apId);
        if (ap == null)
        {
            throw new IllegalArgumentException("应付单不存在或已删除");
        }
        // 幂等：一个应付单只生成一条进项税
        if (taxMapper.selectByApId(apId) != null)
        {
            FinInputTax exist = taxMapper.selectByApId(apId);
            return exist.getTaxId();
        }
        // 无税额（免税/零税率）不生成进项税记录
        if (nz(ap.getTaxAmount()).compareTo(BigDecimal.ZERO) <= 0)
        {
            log.info("应付单 {} 税额为 0，不生成进项税记录", ap.getApNo());
            return null;
        }

        FinInputTax tax = new FinInputTax();
        tax.setApId(apId);
        tax.setPartnerId(ap.getPartnerId());
        tax.setPartnerName(ap.getPartnerName());
        tax.setInvoiceCode(ap.getInvoiceCode());
        tax.setInvoiceNo(ap.getInvoiceNo());
        tax.setInvoiceDate(ap.getInvoiceDate() != null ? ap.getInvoiceDate() : ap.getApDate());
        tax.setTaxRate(ap.getTaxRate());
        tax.setAmountExclTax(ap.getAmountExclTax());
        tax.setTaxAmount(ap.getTaxAmount());
        tax.setDeductStatus(FinInputTax.STATUS_PENDING);
        tax.setCompanyId(0L);
        tax.setUserId(SecurityUtils.getUserId());
        tax.setCreateBy(SecurityUtils.getUsername());
        tax.setRemark("由应付单 " + ap.getApNo() + " 生成");
        taxMapper.insertInputTax(tax);
        return tax.getTaxId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markDeducted(Long taxId, String deductPeriod)
    {
        if (deductPeriod == null || deductPeriod.length() != 6)
        {
            throw new IllegalArgumentException("抵扣期间格式应为 yyyyMM");
        }
        return taxMapper.markDeducted(taxId, deductPeriod, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markVoid(Long taxId)
    {
        return taxMapper.markVoid(taxId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markExpired()
    {
        int n = taxMapper.markExpired(new Date());
        if (n > 0)
        {
            log.warn("已将 {} 笔超期未抵扣的进项税标记为已过期", n);
        }
        return n;
    }

    @Override
    public List<Map<String, Object>> summary(String deductStatus)
    {
        return taxMapper.sumByStatus(deductStatus);
    }

    @Override
    public List<FinInputTax> expiringSoon(Integer days)
    {
        return taxMapper.selectExpiringSoon(new Date(), days == null ? WARN_DAYS : days, 100);
    }

    @Override
    public int countOverdue()
    {
        return taxMapper.countOverdue(new Date());
    }

    @Override
    public List<FinInventoryValuation> selectValuationList(FinInventoryValuation val)
    {
        return taxMapper.selectValuationList(val);
    }

    @Override
    public List<Map<String, Object>> pendingProvision(String period)
    {
        if (period == null || period.trim().isEmpty())
        {
            throw new IllegalArgumentException("会计期间不能为空");
        }
        return taxMapper.selectPendingProvision(period);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal provision(String period, Long materialId, BigDecimal ratio)
    {
        if (ratio == null || ratio.compareTo(BigDecimal.ZERO) < 0 || ratio.compareTo(BigDecimal.ONE) > 0)
        {
            throw new IllegalArgumentException("跌价比例应在 0 ~ 1 之间（如5% 填 0.05）");
        }
        if (ratio.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new IllegalArgumentException("跌价比例不能为 0，如无需计提请勿操作");
        }

        // 从待计提清单中取该物料的成本数据
        Map<String, Object> target = null;
        for (Map<String, Object> row : taxMapper.selectPendingProvision(period))
        {
            Object id = row.get("materialId");
            if (id != null && Long.valueOf(String.valueOf(id).split("\\.")[0]).equals(materialId))
            {
                target = row;
                break;
            }
        }
        if (target == null)
        {
            throw new IllegalArgumentException(
                    "物料ID=" + materialId + " 在本期无待计提的存货，或本期已计提过");
        }

        BigDecimal totalValue = num(target.get("totalValue"));
        BigDecimal qty = num(target.get("qty"));
        BigDecimal unitCost = num(target.get("unitCost"));
        BigDecimal amount = totalValue.multiply(ratio).setScale(2, RoundingMode.HALF_UP);

        // 计提不得超过存货成本
        if (amount.compareTo(totalValue) > 0)
        {
            amount = totalValue;
        }

        FinInventoryValuation val = new FinInventoryValuation();
        val.setPeriod(period);
        val.setMaterialId(materialId);
        val.setMaterialCode(str(target.get("materialCode")));
        val.setMaterialName(str(target.get("materialName")));
        val.setWarehouseId(null);
        val.setQty(qty);
        val.setUnitCost(unitCost);
        val.setTotalValue(totalValue);
        val.setProvisionAmount(amount);
        val.setProvisionRatio(ratio);
        // 可变现净值 = 成本 - 计提
        val.setNetValue(totalValue.subtract(amount));
        val.setCompanyId(0L);
        val.setCreateBy(SecurityUtils.getUsername());
        val.setRemark("计提比例 " + ratio.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%");
        taxMapper.insertValuation(val);

        log.info("存货跌价计提：期间 {} 物料 {} 成本 {} 比例 {} 计提 {}",
                period, val.getMaterialCode(), totalValue, ratio, amount);
        return amount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reverseProvision(Long valId)
    {
        return taxMapper.reverseProvision(valId);
    }

    @Override
    public Map<String, Object> provisionSummary(String period)
    {
        Map<String, Object> r = taxMapper.sumProvisionByPeriod(period);
        return r == null ? new java.util.HashMap<>() : r;
    }

    // ==================== 私有方法 ====================

    /**
     * 计算抵扣截止日（开票日 +12 个月）。
     */
    private Date calcDeadline(Date invoiceDate)
    {
        if (invoiceDate == null)
        {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(invoiceDate);
        c.add(Calendar.MONTH, DEDUCT_PERIOD_MONTHS);
        return c.getTime();
    }

    private static BigDecimal num(Object o)
    {
        if (o == null)
        {
            return BigDecimal.ZERO;
        }
        if (o instanceof BigDecimal)
        {
            return (BigDecimal) o;
        }
        return new BigDecimal(o.toString());
    }

    private static String str(Object o)
    {
        return o == null ? "" : o.toString();
    }

    private static BigDecimal nz(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }
}