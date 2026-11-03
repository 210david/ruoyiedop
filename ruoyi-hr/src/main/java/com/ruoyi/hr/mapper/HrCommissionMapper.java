package com.ruoyi.hr.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hr.domain.HrCommissionPlan;
import com.ruoyi.hr.domain.HrCommissionTier;
import com.ruoyi.hr.domain.HrCommissionBill;

/**
 * 提成方案/核算表 Mapper接口（需求三）
 */
public interface HrCommissionMapper {
    // ---- 方案与阶梯 ----
    public List<HrCommissionPlan> selectPlanList(HrCommissionPlan plan);

    public HrCommissionPlan selectPlanById(Long planId);

    public int insertPlan(HrCommissionPlan plan);

    public int updatePlan(HrCommissionPlan plan);

    public int deletePlanByIds(Long[] planIds);

    public List<HrCommissionTier> selectTiersByPlan(Long planId);

    public int insertTier(HrCommissionTier tier);

    public int deleteTiersByPlan(Long planId);

    // ---- 核算表 ----
    public List<HrCommissionBill> selectBillList(HrCommissionBill bill);

    public HrCommissionBill selectBillById(Long billId);

    public int insertBill(HrCommissionBill bill);

    public int updateBill(HrCommissionBill bill);

    public int deleteBillByPeriod(@Param("period") String period);

    /** 锁定校验：周期内已锁定核算表数量 */
    public int countLockedBills(@Param("period") String period);
}
