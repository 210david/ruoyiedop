package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsAqlPlan;

public interface QmsAqlPlanMapper {
    public List<QmsAqlPlan> selectAqlPlanList(QmsAqlPlan plan);
    public QmsAqlPlan selectAqlPlanById(Long planId);
    public QmsAqlPlan selectByAqlAndCode(String aqlLevel, String codeLetter, String inspectLevel);
    public int insertAqlPlan(QmsAqlPlan plan);
    public int updateAqlPlan(QmsAqlPlan plan);
    public int deleteAqlPlanByIds(Long[] planIds);
}
