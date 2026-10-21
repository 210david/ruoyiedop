package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrPayslip;
import com.ruoyi.hr.mapper.HrPayslipMapper;
import com.ruoyi.hr.mapper.HrSpecialDeductionMapper;
import com.ruoyi.hr.mapper.HrTaxRateMapper;
import com.ruoyi.hr.service.IHrPayslipService;

@Service
public class HrPayslipServiceImpl implements IHrPayslipService
{
    @Autowired
    private HrPayslipMapper hrPayslipMapper;
    @Autowired
    private HrSpecialDeductionMapper hrSpecialDeductionMapper;
    @Autowired
    private HrTaxRateMapper hrTaxRateMapper;

    @Override
    public HrPayslip selectHrPayslipById(Long payslipId) { return hrPayslipMapper.selectHrPayslipById(payslipId); }

    @Override
    public List<HrPayslip> selectHrPayslipList(HrPayslip hrPayslip) { return hrPayslipMapper.selectHrPayslipList(hrPayslip); }

    @Override
    public int insertHrPayslip(HrPayslip hrPayslip) { return hrPayslipMapper.insertHrPayslip(hrPayslip); }

    @Override
    public int updateHrPayslip(HrPayslip hrPayslip) { return hrPayslipMapper.updateHrPayslip(hrPayslip); }

    @Override
    public int deleteHrPayslipByIds(Long[] payslipIds) { return hrPayslipMapper.deleteHrPayslipByIds(payslipIds); }

    @Override
    public int deleteHrPayslipById(Long payslipId) { return hrPayslipMapper.deleteHrPayslipById(payslipId); }

    @Override
    public int saveWithCumulativeTax(HrPayslip payslip)
    {
        java.math.BigDecimal ZERO = java.math.BigDecimal.ZERO;
        java.math.BigDecimal THRESHOLD = new java.math.BigDecimal("5000");

        // 当月应发/社保/公积金兜底
        java.math.BigDecimal gross = payslip.getGrossSalary() == null ? ZERO : payslip.getGrossSalary();
        java.math.BigDecimal social = payslip.getSocialInsurance() == null ? ZERO : payslip.getSocialInsurance();
        java.math.BigDecimal fund = payslip.getHouseFund() == null ? ZERO : payslip.getHouseFund();

        // 年月解析：2026-09 → year=2026, month=9
        String ym = payslip.getYearMonth();
        int year = Integer.parseInt(ym.substring(0, 4));
        int month = Integer.parseInt(ym.substring(5, 7));

        // 取同年度、同员工、小于本月的所有工资条（累计基数）
        HrPayslip q = new HrPayslip();
        q.setEmployeeId(payslip.getEmployeeId());
        List<HrPayslip> history = hrPayslipMapper.selectHrPayslipList(q);
        java.math.BigDecimal cumGross = ZERO, cumSocial = ZERO, cumFund = ZERO, cumTax = ZERO;
        int monthsCounted = 0;
        if (history != null)
        {
            for (HrPayslip h : history)
            {
                if (h.getYearMonth() == null || h.getYearMonth().length() < 7) continue;
                if (payslip.getPayslipId() != null && payslip.getPayslipId().equals(h.getPayslipId())) continue;
                int hy = Integer.parseInt(h.getYearMonth().substring(0, 4));
                int hm = Integer.parseInt(h.getYearMonth().substring(5, 7));
                if (hy != year || hm >= month) continue;
                cumGross = cumGross.add(h.getGrossSalary() == null ? ZERO : h.getGrossSalary());
                cumSocial = cumSocial.add(h.getSocialInsurance() == null ? ZERO : h.getSocialInsurance());
                cumFund = cumFund.add(h.getHouseFund() == null ? ZERO : h.getHouseFund());
                cumTax = cumTax.add(h.getIncomeTax() == null ? ZERO : h.getIncomeTax());
                monthsCounted++;
            }
        }

        // 累计专项附加扣除：本月生效的扣除记录 × 已发薪月数
        java.math.BigDecimal monthDeduction = hrSpecialDeductionMapper.sumMonthlyDeduction(
                payslip.getEmployeeId(), java.sql.Date.valueOf(ym + "-01"));
        java.math.BigDecimal cumDeduction = monthDeduction.multiply(new java.math.BigDecimal(monthsCounted + 1));

        // 累计应纳税所得额
        java.math.BigDecimal cumTaxable = gross.add(cumGross)
                .subtract(THRESHOLD.multiply(new java.math.BigDecimal(monthsCounted + 1)))
                .subtract(social.add(cumSocial))
                .subtract(fund.add(cumFund))
                .max(ZERO);

        // 累计应预扣税额（套7级税率表）
        java.math.BigDecimal cumWithheld = calcCumulativeTax(cumTaxable);
        // 本月个税 = 累计应预扣 − 累计已预扣
        java.math.BigDecimal thisMonthTax = cumWithheld.subtract(cumTax).max(ZERO).setScale(2, java.math.RoundingMode.HALF_UP);

        payslip.setCumulativeTaxableIncome(cumTaxable.setScale(2, java.math.RoundingMode.HALF_UP));
        payslip.setCumulativeTaxWithheld(cumWithheld.setScale(2, java.math.RoundingMode.HALF_UP));
        payslip.setIncomeTax(thisMonthTax);
        // 实发 = 应发 − 社保 − 公积金 − 个税 − 其他扣款
        java.math.BigDecimal otherDed = payslip.getOtherDeduction() == null ? ZERO : payslip.getOtherDeduction();
        payslip.setNetSalary(gross.subtract(social).subtract(fund).subtract(thisMonthTax).subtract(otherDed).setScale(2, java.math.RoundingMode.HALF_UP));

        if (payslip.getPayslipId() != null)
        {
            return hrPayslipMapper.updateHrPayslip(payslip);
        }
        return hrPayslipMapper.insertHrPayslip(payslip);
    }

    /** 按7级年度税率表计算累计应预扣税额 */
    private java.math.BigDecimal calcCumulativeTax(java.math.BigDecimal cumTaxable)
    {
        List<com.ruoyi.hr.domain.HrTaxRate> rates = hrTaxRateMapper.selectHrTaxRateList(new com.ruoyi.hr.domain.HrTaxRate());
        for (com.ruoyi.hr.domain.HrTaxRate r : rates)
        {
            java.math.BigDecimal min = r.getMinAmount() == null ? java.math.BigDecimal.ZERO : r.getMinAmount();
            java.math.BigDecimal max = r.getMaxAmount() == null ? new java.math.BigDecimal("99999999") : r.getMaxAmount();
            if (cumTaxable.compareTo(min) >= 0 && cumTaxable.compareTo(max) < 0)
            {
                return cumTaxable.multiply(r.getRate()).subtract(r.getQuickDeduction() == null ? java.math.BigDecimal.ZERO : r.getQuickDeduction())
                        .max(java.math.BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
            }
        }
        return java.math.BigDecimal.ZERO;
    }
}
