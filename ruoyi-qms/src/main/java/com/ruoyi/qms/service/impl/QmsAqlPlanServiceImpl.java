package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qms.domain.QmsAqlPlan;
import com.ruoyi.qms.mapper.QmsAqlPlanMapper;
import com.ruoyi.qms.service.IQmsAqlPlanService;

@Service
public class QmsAqlPlanServiceImpl implements IQmsAqlPlanService {
    @Autowired
    private QmsAqlPlanMapper mapper;

    @Override
    public List<QmsAqlPlan> selectAqlPlanList(QmsAqlPlan plan) { return mapper.selectAqlPlanList(plan); }

    @Override
    public QmsAqlPlan selectAqlPlanById(Long planId) { return mapper.selectAqlPlanById(planId); }

    @Override
    public QmsAqlPlan selectByAqlAndCode(String aqlLevel, String codeLetter, String inspectLevel) {
        return mapper.selectByAqlAndCode(aqlLevel, codeLetter, inspectLevel);
    }

    @Override
    public int insertAqlPlan(QmsAqlPlan plan) {
        if (plan.getStatus() == null) plan.setStatus("0");
        return mapper.insertAqlPlan(plan);
    }

    @Override
    public int updateAqlPlan(QmsAqlPlan plan) { return mapper.updateAqlPlan(plan); }

    @Override
    public int deleteAqlPlanByIds(Long[] planIds) { return mapper.deleteAqlPlanByIds(planIds); }
}
