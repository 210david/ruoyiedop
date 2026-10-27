package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrSsPlan;

/**
 * HR社保方案 Service接口
 */
public interface IHrSsPlanService
{
    public List<HrSsPlan> selectHrSsPlanList(HrSsPlan hrSsPlan);
    public HrSsPlan selectHrSsPlanById(Long planId);
    public HrSsPlan selectDefaultPlanByYear(Integer year);
    public int insertHrSsPlan(HrSsPlan hrSsPlan);
    public int updateHrSsPlan(HrSsPlan hrSsPlan);
    public int deleteHrSsPlanByIds(Long[] planIds);

    /** 复制方案到目标年度（比例/上下限原样带出，默认标记不复制），返回新方案ID */
    public Long copyPlanToYear(Long planId, Integer targetYear);
}
