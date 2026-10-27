package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsAuditPlan;

public interface QmsAuditPlanMapper {
    public List<QmsAuditPlan> selectAuditPlanList(QmsAuditPlan plan);
    public QmsAuditPlan selectAuditPlanById(Long auditPlanId);
    public int insertAuditPlan(QmsAuditPlan plan);
    public int updateAuditPlan(QmsAuditPlan plan);
    public int deleteAuditPlanByIds(Long[] auditPlanIds);
}
