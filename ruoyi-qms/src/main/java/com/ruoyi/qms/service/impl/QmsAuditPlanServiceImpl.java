package com.ruoyi.qms.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
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
    @DataScope(deptAlias = "qms_audit_plan")
    public List<QmsAuditPlan> selectAuditPlanList(QmsAuditPlan plan) { return mapper.selectAuditPlanList(plan); }
    @Override
    public QmsAuditPlan selectAuditPlanById(Long auditPlanId) { return mapper.selectAuditPlanById(auditPlanId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAuditPlan(QmsAuditPlan plan) {
        validateAuditPlan(plan);
        if (StringUtils.isEmpty(plan.getAuditPlanNo())) {
            plan.setAuditPlanNo(mkNumberRuleService.generateNumber("qms_audit_plan"));
        }
        plan.setDelFlag("0"); plan.setStatus("0");
        if (plan.getPlanStatus() == null) plan.setPlanStatus("0");
        return mapper.insertAuditPlan(plan);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAuditPlan(QmsAuditPlan plan) { validateAuditPlan(plan); return mapper.updateAuditPlan(plan); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditPlanByIds(Long[] auditPlanIds) { return mapper.deleteAuditPlanByIds(auditPlanIds); }

    /** 保存前兜底校验：关键必填项 + 日期先后关系（与前端口径一致） */
    private void validateAuditPlan(QmsAuditPlan plan) {
        if (StringUtils.isEmpty(plan.getAuditTitle())) { throw new ServiceException("审核标题不能为空"); }
        if (plan.getAuditYear() == null) { throw new ServiceException("审核年度不能为空"); }
        if (StringUtils.isEmpty(plan.getLeadAuditor())) { throw new ServiceException("主审人不能为空"); }
        if (plan.getAuditDateFrom() == null) { throw new ServiceException("开始日期不能为空"); }
        if (plan.getAuditDateTo() == null) { throw new ServiceException("结束日期不能为空"); }
        if (StringUtils.isEmpty(plan.getAuditScope())) { throw new ServiceException("审核范围不能为空"); }
        if (plan.getAuditDateTo().before(plan.getAuditDateFrom())) { throw new ServiceException("结束日期不能早于开始日期"); }
    }
}
