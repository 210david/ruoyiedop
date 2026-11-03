package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrCommissionPlan;
import com.ruoyi.hr.domain.HrCommissionBill;

/**
 * 提成方案/核算 Service接口（需求三）
 */
public interface IHrCommissionService {
    // ---- 方案 ----
    public List<HrCommissionPlan> selectPlanList(HrCommissionPlan plan);

    public HrCommissionPlan selectPlanById(Long planId);

    public int insertPlan(HrCommissionPlan plan);

    /** 修改方案（MR-01：锁定后不可追溯修改） */
    public int updatePlan(HrCommissionPlan plan);

    public int deletePlanByIds(Long[] planIds);

    // ---- 核算表 ----
    public List<HrCommissionBill> selectBillList(HrCommissionBill bill);

    /** 生成周期核算表（依据月度归集快照与各员工提成方案计算，幂等：重算前清理待确认记录） */
    public int generateBills(String period);

    /** 确认核算表（待确认→已确认） */
    public int confirmBill(Long billId);

    /** 锁定周期核算表（已确认→已锁定，关联方案自动锁定，MR-01） */
    public int lockBills(String period);

    /** 删除周期内待确认核算表 */
    public int deleteBills(String period);
}
