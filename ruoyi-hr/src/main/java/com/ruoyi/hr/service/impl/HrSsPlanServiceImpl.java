package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrSsPlan;
import com.ruoyi.hr.mapper.HrSsPlanMapper;
import com.ruoyi.hr.mapper.HrSocialSecurityMapper;
import com.ruoyi.hr.service.IHrSsPlanService;

/**
 * HR社保方案 Service实现
 */
@Service
public class HrSsPlanServiceImpl implements IHrSsPlanService
{
    @Autowired
    private HrSsPlanMapper hrSsPlanMapper;

    @Autowired
    private HrSocialSecurityMapper hrSocialSecurityMapper;

    @Override
    public List<HrSsPlan> selectHrSsPlanList(HrSsPlan hrSsPlan)
    {
        return hrSsPlanMapper.selectHrSsPlanList(hrSsPlan);
    }

    @Override
    public HrSsPlan selectHrSsPlanById(Long planId)
    {
        return hrSsPlanMapper.selectHrSsPlanById(planId);
    }

    @Override
    public HrSsPlan selectDefaultPlanByYear(Integer year)
    {
        return hrSsPlanMapper.selectDefaultPlanByYear(year);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertHrSsPlan(HrSsPlan hrSsPlan)
    {
        checkYearDuplicate(hrSsPlan, null);
        hrSsPlan.setCreateTime(DateUtils.getNowDate());
        int rows = hrSsPlanMapper.insertHrSsPlan(hrSsPlan);
        if (hrSsPlan.getIsDefault() != null && hrSsPlan.getIsDefault() == 1)
        {
            hrSsPlanMapper.clearDefaultByYear(hrSsPlan.getYear(), hrSsPlan.getPlanId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateHrSsPlan(HrSsPlan hrSsPlan)
    {
        HrSsPlan existing = hrSsPlanMapper.selectHrSsPlanById(hrSsPlan.getPlanId());
        if (existing == null)
        {
            throw new ServiceException("社保方案不存在");
        }
        Integer newYear = hrSsPlan.getYear() != null ? hrSsPlan.getYear() : existing.getYear();
        if (hrSsPlan.getYear() != null && !hrSsPlan.getYear().equals(existing.getYear()))
        {
            Long inUse = hrSocialSecurityMapper.countByPlanIdAndYear(hrSsPlan.getPlanId(), hrSsPlan.getYear());
            if (inUse != null && inUse > 0)
            {
                throw new ServiceException("该方案已被员工社保记录引用，不允许修改适用年度");
            }
        }
        checkYearDuplicate(hrSsPlan, hrSsPlan.getPlanId());
        hrSsPlan.setUpdateBy(StringUtils.isNotEmpty(hrSsPlan.getUpdateBy()) ? hrSsPlan.getUpdateBy() : null);
        int rows = hrSsPlanMapper.updateHrSsPlan(hrSsPlan);
        Integer newDefault = hrSsPlan.getIsDefault() != null ? hrSsPlan.getIsDefault() : existing.getIsDefault();
        if (newDefault != null && newDefault == 1)
        {
            hrSsPlanMapper.clearDefaultByYear(newYear, hrSsPlan.getPlanId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteHrSsPlanByIds(Long[] planIds)
    {
        if (planIds != null)
        {
            for (Long planId : planIds)
            {
                Long inUse = hrSocialSecurityMapper.countByPlanId(planId);
                if (inUse != null && inUse > 0)
                {
                    HrSsPlan plan = hrSsPlanMapper.selectHrSsPlanById(planId);
                    throw new ServiceException("方案[" + (plan != null ? plan.getPlanName() : planId) + "]已被员工社保记录引用，不允许删除");
                }
            }
        }
        return hrSsPlanMapper.deleteHrSsPlanByIds(planIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copyPlanToYear(Long planId, Integer targetYear)
    {
        HrSsPlan src = hrSsPlanMapper.selectHrSsPlanById(planId);
        if (src == null)
        {
            throw new ServiceException("源方案不存在");
        }
        if (targetYear == null || targetYear < 2000 || targetYear > 2100)
        {
            throw new ServiceException("目标年度不合法");
        }
        HrSsPlan dupQuery = new HrSsPlan();
        dupQuery.setYear(targetYear);
        for (HrSsPlan p : hrSsPlanMapper.selectHrSsPlanList(dupQuery))
        {
            if (p.getPlanName().equals(src.getPlanName()))
            {
                throw new ServiceException("目标年度已存在同名方案[" + src.getPlanName() + "]，请先修改名称或直接使用");
            }
        }
        HrSsPlan copy = new HrSsPlan();
        copy.setPlanName(src.getPlanName());
        copy.setYear(targetYear);
        copy.setIsDefault(0); // 默认标记不复制，需手动设为默认
        copy.setBaseLower(src.getBaseLower());
        copy.setBaseUpper(src.getBaseUpper());
        copy.setPensionEmployer(src.getPensionEmployer());
        copy.setPensionEmployee(src.getPensionEmployee());
        copy.setMedicalEmployer(src.getMedicalEmployer());
        copy.setMedicalEmployee(src.getMedicalEmployee());
        copy.setUnemploymentEmployer(src.getUnemploymentEmployer());
        copy.setUnemploymentEmployee(src.getUnemploymentEmployee());
        copy.setInjuryEmployer(src.getInjuryEmployer());
        copy.setMaternityEmployer(src.getMaternityEmployer());
        copy.setFundEmployer(src.getFundEmployer());
        copy.setFundEmployee(src.getFundEmployee());
        copy.setStatus("0");
        copy.setCreateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        copy.setRemark("复制自方案[" + src.getPlanName() + "](" + src.getYear() + "年度)，请按新年度政策核对调整");
        hrSsPlanMapper.insertHrSsPlan(copy);
        return copy.getPlanId();
    }

    /** 同年度默认方案唯一性校验 */
    private void checkYearDuplicate(HrSsPlan hrSsPlan, Long excludePlanId)
    {
        if (hrSsPlan.getYear() == null) return;
        HrSsPlan query = new HrSsPlan();
        query.setYear(hrSsPlan.getYear());
        for (HrSsPlan p : hrSsPlanMapper.selectHrSsPlanList(query))
        {
            if (excludePlanId != null && excludePlanId.equals(p.getPlanId())) continue;
            if (hrSsPlan.getIsDefault() != null && hrSsPlan.getIsDefault() == 1
                    && p.getIsDefault() != null && p.getIsDefault() == 1)
            {
                // 允许多个方案存在，但默认标记唯一：保存时自动清掉其他默认
                return;
            }
        }
    }
}
