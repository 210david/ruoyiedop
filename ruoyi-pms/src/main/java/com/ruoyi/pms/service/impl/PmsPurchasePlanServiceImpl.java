package com.ruoyi.pms.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.PmsPurchasePlan;
import com.ruoyi.pms.domain.PmsPurchasePlanDetail;
import com.ruoyi.pms.domain.PmsPlanAuditLog;
import com.ruoyi.pms.mapper.PmsPurchasePlanMapper;
import com.ruoyi.pms.mapper.PmsPlanAuditLogMapper;
import com.ruoyi.pms.service.IPmsPurchasePlanService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 采购计划Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class PmsPurchasePlanServiceImpl implements IPmsPurchasePlanService
{
    @Autowired
    private PmsPurchasePlanMapper pmsPurchasePlanMapper;

    @Autowired
    private PmsPlanAuditLogMapper pmsPlanAuditLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    /**
     * 查询采购计划
     *
     * @param planId 采购计划ID
     * @return 采购计划
     */
    @Override
    public PmsPurchasePlan selectPmsPurchasePlanById(Long planId)
    {
        PmsPurchasePlan plan = pmsPurchasePlanMapper.selectPmsPurchasePlanById(planId);
        if (plan != null)
        {
            plan.setAuditLogList(pmsPlanAuditLogMapper.selectAuditLogByPlanId(planId));
        }
        return plan;
    }

    /**
     * 查询采购计划列表
     *
     * @param pmsPurchasePlan 采购计划
     * @return 采购计划
     */
    @Override
    public List<PmsPurchasePlan> selectPmsPurchasePlanList(PmsPurchasePlan pmsPurchasePlan)
    {
        return pmsPurchasePlanMapper.selectPmsPurchasePlanList(pmsPurchasePlan);
    }

    /**
     * 新增采购计划
     *
     * @param pmsPurchasePlan 采购计划
     * @return 结果
     */
    @Transactional
    @Override
    public int insertPmsPurchasePlan(PmsPurchasePlan pmsPurchasePlan)
    {
        pmsPurchasePlan.setCreateTime(DateUtils.getNowDate());
        pmsPurchasePlan.setCreateBy(SecurityUtils.getUsername());
        pmsPurchasePlan.setStatus("0"); // 草稿状态
        pmsPurchasePlan.setDelFlag("0");

        // 后端生成真实计划单号（消耗序号，避免前端预览号重复）
        if (StringUtils.isEmpty(pmsPurchasePlan.getPlanNo()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(pmsPurchasePlan.getPlanType())) { params.put("planType", pmsPurchasePlan.getPlanType()); }
        pmsPurchasePlan.setPlanNo(mkNumberRuleService.generateNumber("PMS_PLAN", params));
        }

        // 计算预算总金额
        if (pmsPurchasePlan.getDetailList() != null && !pmsPurchasePlan.getDetailList().isEmpty()) {
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
            for (PmsPurchasePlanDetail detail : pmsPurchasePlan.getDetailList()) {
                if (detail.getBudgetAmount() != null) {
                    totalAmount = totalAmount.add(detail.getBudgetAmount());
                }
            }
            pmsPurchasePlan.setBudgetAmount(totalAmount);
        }

        int rows = pmsPurchasePlanMapper.insertPmsPurchasePlan(pmsPurchasePlan);
        insertPmsPurchasePlanDetail(pmsPurchasePlan);
        return rows;
    }

    /**
     * 修改采购计划
     *
     * @param pmsPurchasePlan 采购计划
     * @return 结果
     */
    @Transactional
    @Override
    public int updatePmsPurchasePlan(PmsPurchasePlan pmsPurchasePlan)
    {
        PmsPurchasePlan existing = pmsPurchasePlanMapper.selectPmsPurchasePlanById(pmsPurchasePlan.getPlanId());
        if (existing != null && "6".equals(existing.getStatus()))
        {
            // 驳回状态修改后重置为待审批，清空主表审批信息（审核日志保留）
            pmsPurchasePlan.setStatus("1");
            pmsPurchasePlan.setAuditBy(null);
            pmsPurchasePlan.setAuditTime(null);
            pmsPurchasePlan.setAuditOpinion(null);
        }
        pmsPurchasePlan.setUpdateTime(DateUtils.getNowDate());
        pmsPurchasePlan.setUpdateBy(SecurityUtils.getUsername());

        // 计算预算总金额
        if (pmsPurchasePlan.getDetailList() != null && !pmsPurchasePlan.getDetailList().isEmpty()) {
            java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
            for (PmsPurchasePlanDetail detail : pmsPurchasePlan.getDetailList()) {
                if (detail.getBudgetAmount() != null) {
                    totalAmount = totalAmount.add(detail.getBudgetAmount());
                }
            }
            pmsPurchasePlan.setBudgetAmount(totalAmount);
        }

        pmsPurchasePlanMapper.deletePmsPurchasePlanDetailByPlanId(pmsPurchasePlan.getPlanId());
        insertPmsPurchasePlanDetail(pmsPurchasePlan);
        return pmsPurchasePlanMapper.updatePmsPurchasePlan(pmsPurchasePlan);
    }

    /**
     * 批量删除采购计划
     *
     * @param planIds 需要删除的采购计划ID
     * @return 结果
     */
    @Transactional
    @Override
    public int deletePmsPurchasePlanByIds(Long[] planIds)
    {
        pmsPurchasePlanMapper.deletePmsPurchasePlanDetailByPlanIds(planIds);
        return pmsPurchasePlanMapper.deletePmsPurchasePlanByIds(planIds);
    }

    /**
     * 删除采购计划信息
     *
     * @param planId 采购计划ID
     * @return 结果
     */
    @Transactional
    @Override
    public int deletePmsPurchasePlanById(Long planId)
    {
        pmsPurchasePlanMapper.deletePmsPurchasePlanDetailByPlanId(planId);
        return pmsPurchasePlanMapper.deletePmsPurchasePlanById(planId);
    }

    /**
     * 新增采购计划明细信息
     *
     * @param pmsPurchasePlan 采购计划对象
     */
    public void insertPmsPurchasePlanDetail(PmsPurchasePlan pmsPurchasePlan)
    {
        List<PmsPurchasePlanDetail> detailList = pmsPurchasePlan.getDetailList();
        if (detailList != null && !detailList.isEmpty()) {
            Long planId = pmsPurchasePlan.getPlanId();
            for (PmsPurchasePlanDetail detail : detailList) {
                detail.setPlanId(planId);
                detail.setDelFlag("0");
                // 计算预算金额
                if (detail.getPlanQuantity() != null && detail.getBudgetPrice() != null) {
                    detail.setBudgetAmount(detail.getPlanQuantity().multiply(detail.getBudgetPrice()));
                }
            }
            pmsPurchasePlanMapper.batchPmsPurchasePlanDetail(detailList);
        }
    }

    /**
     * 审核采购计划
     *
     * @param planId 采购计划ID
     * @param status 审核状态（2=通过 6=驳回）
     * @param auditOpinion 审批意见
     * @return 结果
     */
    @Override
    public int auditPmsPurchasePlan(Long planId, String status, String auditOpinion)
    {
        PmsPurchasePlan pmsPurchasePlan = new PmsPurchasePlan();
        pmsPurchasePlan.setPlanId(planId);
        pmsPurchasePlan.setStatus(status);
        pmsPurchasePlan.setAuditOpinion(auditOpinion);
        pmsPurchasePlan.setAuditTime(DateUtils.getNowDate());
        pmsPurchasePlan.setAuditBy(SecurityUtils.getUsername());

        int rows = pmsPurchasePlanMapper.updatePmsPurchasePlan(pmsPurchasePlan);

        // 写入审核日志（永久留存）
        PmsPlanAuditLog auditLog = new PmsPlanAuditLog();
        auditLog.setPlanId(planId);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark(auditOpinion);
        // auditAction: 1=通过 2=驳回（与前端时间线模板一致）
        if ("2".equals(status)) {
            auditLog.setAuditAction("1");
        } else {
            auditLog.setAuditAction("2");
        }
        pmsPlanAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    /**
     * 关闭采购计划
     *
     * @param planId 采购计划ID
     * @return 结果
     */
    @Override
    public int closePmsPurchasePlan(Long planId)
    {
        PmsPurchasePlan plan = new PmsPurchasePlan();
        plan.setPlanId(planId);
        plan.setStatus("5"); // 已关闭
        plan.setUpdateTime(DateUtils.getNowDate());
        plan.setUpdateBy(SecurityUtils.getUsername());
        return pmsPurchasePlanMapper.updatePmsPurchasePlan(plan);
    }
}
