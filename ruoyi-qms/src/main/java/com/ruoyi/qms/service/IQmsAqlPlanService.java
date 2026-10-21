package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsAqlPlan;

public interface IQmsAqlPlanService {
    public List<QmsAqlPlan> selectAqlPlanList(QmsAqlPlan plan);
    public QmsAqlPlan selectAqlPlanById(Long planId);
    public int insertAqlPlan(QmsAqlPlan plan);
    public int updateAqlPlan(QmsAqlPlan plan);
    public int deleteAqlPlanByIds(Long[] planIds);
    public QmsAqlPlan selectByAqlAndCode(String aqlLevel, String codeLetter, String inspectLevel);
}
