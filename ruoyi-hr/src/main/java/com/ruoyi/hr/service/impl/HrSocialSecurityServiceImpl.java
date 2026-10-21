package com.ruoyi.hr.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrSocialSecurity;
import com.ruoyi.hr.domain.HrSsPlan;
import com.ruoyi.hr.mapper.HrSocialSecurityMapper;
import com.ruoyi.hr.service.IHrSsPlanService;
import com.ruoyi.hr.service.IHrSocialSecurityService;

@Service
public class HrSocialSecurityServiceImpl implements IHrSocialSecurityService
{
    @Autowired
    private HrSocialSecurityMapper hrSocialSecurityMapper;

    @Autowired
    private IHrSsPlanService hrSsPlanService;

    @Override
    public HrSocialSecurity selectHrSocialSecurityById(Long ssId) { return hrSocialSecurityMapper.selectHrSocialSecurityById(ssId); }

    @Override
    @DataScope(deptAlias = "biz_hr_social_security")
    public List<HrSocialSecurity> selectHrSocialSecurityList(HrSocialSecurity hrSocialSecurity) { return hrSocialSecurityMapper.selectHrSocialSecurityList(hrSocialSecurity); }

    @Override
    public int insertHrSocialSecurity(HrSocialSecurity hrSocialSecurity)
    {
        clampBaseAmount(hrSocialSecurity);
        hrSocialSecurity.setCreateTime(DateUtils.getNowDate());
        return hrSocialSecurityMapper.insertHrSocialSecurity(hrSocialSecurity);
    }

    @Override
    public int updateHrSocialSecurity(HrSocialSecurity hrSocialSecurity)
    {
        clampBaseAmount(hrSocialSecurity);
        return hrSocialSecurityMapper.updateHrSocialSecurity(hrSocialSecurity);
    }

    @Override
    public int deleteHrSocialSecurityByIds(Long[] ssIds) { return hrSocialSecurityMapper.deleteHrSocialSecurityByIds(ssIds); }

    @Override
    public int deleteHrSocialSecurityById(Long ssId) { return hrSocialSecurityMapper.deleteHrSocialSecurityById(ssId); }

    @Override
    public int batchInitByPlan(com.ruoyi.hr.domain.HrSsPlan plan)
    {
        plan.setCreateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return hrSocialSecurityMapper.batchInitByPlan(plan);
    }

    /**
     * 缴费基数按方案上下限自动钳制：
     * 基数低于下限自动按下限、高于上限自动按上限。
     * 方案取数优先级：员工记录所选方案(plan_id) > 该年度默认方案；找不到方案时不钳制。
     */
    private void clampBaseAmount(HrSocialSecurity ss)
    {
        if (ss.getBaseAmount() == null || ss.getYear() == null)
        {
            return;
        }
        HrSsPlan plan = null;
        if (ss.getPlanId() != null)
        {
            plan = hrSsPlanService.selectHrSsPlanById(ss.getPlanId());
            // 员工所选方案年度与记录年度不一致时，不按该方案钳制（避免跨年度口径错误）
            if (plan != null && plan.getYear() != null && !plan.getYear().equals(ss.getYear()))
            {
                plan = null;
            }
        }
        if (plan == null)
        {
            plan = hrSsPlanService.selectDefaultPlanByYear(ss.getYear());
        }
        if (plan == null)
        {
            return;
        }
        BigDecimal base = ss.getBaseAmount();
        BigDecimal clamped = base;
        if (plan.getBaseLower() != null && base.compareTo(plan.getBaseLower()) < 0)
        {
            clamped = plan.getBaseLower();
        }
        else if (plan.getBaseUpper() != null && base.compareTo(plan.getBaseUpper()) > 0)
        {
            clamped = plan.getBaseUpper();
        }
        if (clamped.compareTo(base) != 0)
        {
            ss.setBaseAmount(clamped);
            // 留痕：说明基数被钳制，便于用户在列表/详情中理解为什么与录入值不同
            String note = "缴费基数" + base.stripTrailingZeros().toPlainString()
                    + "超出方案[" + plan.getPlanName() + "]允许范围，已按"
                    + (clamped.compareTo(plan.getBaseLower()) == 0 && plan.getBaseLower() != null ? "下限" : "上限")
                    + clamped.stripTrailingZeros().toPlainString() + "自动调整";
            ss.setRemark(StringUtils.isNotEmpty(ss.getRemark()) ? ss.getRemark() + "；" + note : note);
        }
    }
}
