package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsAuditPlan;
import com.ruoyi.qms.mapper.QmsAuditPlanMapper;
import com.ruoyi.qms.service.IQmsAuditPlanService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class QmsAuditPlanServiceImpl implements IQmsAuditPlanService {
    @Autowired
    private QmsAuditPlanMapper mapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsAuditPlan> selectAuditPlanList(QmsAuditPlan plan) { return mapper.selectAuditPlanList(plan); }
    @Override
    public QmsAuditPlan selectAuditPlanById(Long auditPlanId) { return mapper.selectAuditPlanById(auditPlanId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAuditPlan(QmsAuditPlan plan) {
        if (StringUtils.isEmpty(plan.getAuditPlanNo())) {
            plan.setAuditPlanNo(mkNumberRuleService.generateNumber("qms_audit_plan"));
        }
        plan.setDelFlag("0"); plan.setStatus("0");
        if (plan.getPlanStatus() == null) plan.setPlanStatus("0");
        return mapper.insertAuditPlan(plan);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAuditPlan(QmsAuditPlan plan) { return mapper.updateAuditPlan(plan); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditPlanByIds(Long[] auditPlanIds) { return mapper.deleteAuditPlanByIds(auditPlanIds); }
}
