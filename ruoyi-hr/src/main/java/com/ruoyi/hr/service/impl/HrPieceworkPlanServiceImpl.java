package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrPieceworkPlan;
import com.ruoyi.hr.mapper.HrPieceworkPlanMapper;
import com.ruoyi.hr.service.IHrPieceworkPlanService;

@Service
public class HrPieceworkPlanServiceImpl implements IHrPieceworkPlanService
{
    @Autowired
    private HrPieceworkPlanMapper hrPieceworkPlanMapper;

    @Override
    public HrPieceworkPlan selectHrPieceworkPlanById(Long pwPlanId) { return hrPieceworkPlanMapper.selectHrPieceworkPlanById(pwPlanId); }

    @Override
    public List<HrPieceworkPlan> selectHrPieceworkPlanList(HrPieceworkPlan hrPieceworkPlan) { return hrPieceworkPlanMapper.selectHrPieceworkPlanList(hrPieceworkPlan); }

    @Override
    public int insertHrPieceworkPlan(HrPieceworkPlan hrPieceworkPlan) { return hrPieceworkPlanMapper.insertHrPieceworkPlan(hrPieceworkPlan); }

    @Override
    public int updateHrPieceworkPlan(HrPieceworkPlan hrPieceworkPlan) { return hrPieceworkPlanMapper.updateHrPieceworkPlan(hrPieceworkPlan); }

    @Override
    public int deleteHrPieceworkPlanByIds(Long[] pwPlanIds) { return hrPieceworkPlanMapper.deleteHrPieceworkPlanByIds(pwPlanIds); }

    @Override
    public int deleteHrPieceworkPlanById(Long pwPlanId) { return hrPieceworkPlanMapper.deleteHrPieceworkPlanById(pwPlanId); }
}
