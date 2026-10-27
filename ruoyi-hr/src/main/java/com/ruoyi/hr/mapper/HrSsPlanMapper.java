package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrSsPlan;

/**
 * HR社保方案 Mapper
 */
public interface HrSsPlanMapper
{
    public List<HrSsPlan> selectHrSsPlanList(HrSsPlan hrSsPlan);
    public HrSsPlan selectHrSsPlanById(Long planId);
    public HrSsPlan selectDefaultPlanByYear(Integer year);
    public int insertHrSsPlan(HrSsPlan hrSsPlan);
    public int updateHrSsPlan(HrSsPlan hrSsPlan);
    public int deleteHrSsPlanById(Long planId);
    public int deleteHrSsPlanByIds(Long[] planIds);
    public int clearDefaultByYear(Integer year, Long planId);
}
