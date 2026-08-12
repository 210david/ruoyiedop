package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsAuditPlan;

public interface IQmsAuditPlanService {
    public List<QmsAuditPlan> selectAuditPlanList(QmsAuditPlan plan);
    public QmsAuditPlan selectAuditPlanById(Long auditPlanId);
    public int insertAuditPlan(QmsAuditPlan plan);
    public int updateAuditPlan(QmsAuditPlan plan);
    public int deleteAuditPlanByIds(Long[] auditPlanIds);
}
