package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsPurchasePlan;

/**
 * 采购计划Service接口
 *
 * @author ruoyi
 */
public interface IPmsPurchasePlanService
{
    /**
     * 查询采购计划
     *
     * @param planId 采购计划ID
     * @return 采购计划
     */
    public PmsPurchasePlan selectPmsPurchasePlanById(Long planId);

    /**
     * 查询采购计划列表
     *
     * @param pmsPurchasePlan 采购计划
     * @return 采购计划集合
     */
    public List<PmsPurchasePlan> selectPmsPurchasePlanList(PmsPurchasePlan pmsPurchasePlan);

    /**
     * 新增采购计划
     *
     * @param pmsPurchasePlan 采购计划
     * @return 结果
     */
    public int insertPmsPurchasePlan(PmsPurchasePlan pmsPurchasePlan);

    /**
     * 修改采购计划
     *
     * @param pmsPurchasePlan 采购计划
     * @return 结果
     */
    public int updatePmsPurchasePlan(PmsPurchasePlan pmsPurchasePlan);

    /**
     * 批量删除采购计划
     *
     * @param planIds 需要删除的采购计划ID
     * @return 结果
     */
    public int deletePmsPurchasePlanByIds(Long[] planIds);

    /**
     * 删除采购计划信息
     *
     * @param planId 采购计划ID
     * @return 结果
     */
    public int deletePmsPurchasePlanById(Long planId);

    /**
     * 审核采购计划
     *
     * @param planId 采购计划ID
     * @param status 审核状态（2=通过 6=驳回）
     * @param auditOpinion 审批意见
     * @return 结果
     */
    public int auditPmsPurchasePlan(Long planId, String status, String auditOpinion);

    /**
     * 关闭采购计划
     *
     * @param planId 采购计划ID
     * @return 结果
     */
    public int closePmsPurchasePlan(Long planId);
}
