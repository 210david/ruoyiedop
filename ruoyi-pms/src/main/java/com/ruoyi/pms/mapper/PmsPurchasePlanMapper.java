package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsPurchasePlan;
import com.ruoyi.pms.domain.PmsPurchasePlanDetail;

/**
 * 采购计划Mapper接口
 *
 * @author ruoyi
 */
public interface PmsPurchasePlanMapper
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
     * 删除采购计划
     *
     * @param planId 采购计划ID
     * @return 结果
     */
    public int deletePmsPurchasePlanById(Long planId);

    /**
     * 批量删除采购计划
     *
     * @param planIds 需要删除的数据ID
     * @return 结果
     */
    public int deletePmsPurchasePlanByIds(Long[] planIds);

    /**
     * 批量删除采购计划明细
     *
     * @param planIds 需要删除的数据ID
     * @return 结果
     */
    public int deletePmsPurchasePlanDetailByPlanIds(Long[] planIds);

    /**
     * 查询采购计划明细列表
     *
     * @param planId 采购计划ID
     * @return 采购计划明细集合
     */
    public List<PmsPurchasePlanDetail> selectPmsPurchasePlanDetailByPlanId(Long planId);

    /**
     * 批量新增采购计划明细
     *
     * @param pmsPurchasePlanDetailList 采购计划明细列表
     * @return 结果
     */
    public int batchPmsPurchasePlanDetail(List<PmsPurchasePlanDetail> pmsPurchasePlanDetailList);

    /**
     * 删除采购计划明细
     *
     * @param planId 采购计划ID
     * @return 结果
     */
    public int deletePmsPurchasePlanDetailByPlanId(Long planId);

    /**
     * 更新计划实际金额
     *
     * @param planId 计划ID
     * @return 结果
     */
    public int updatePlanActualAmount(Long planId);
}
