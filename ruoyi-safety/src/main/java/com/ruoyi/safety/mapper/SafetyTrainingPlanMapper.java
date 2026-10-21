package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyTrainingPlan;

public interface SafetyTrainingPlanMapper {
    public SafetyTrainingPlan selectSafetyTrainingPlanById(Long planId);
    public List<SafetyTrainingPlan> selectSafetyTrainingPlanList(SafetyTrainingPlan safetyTrainingPlan);
    public int insertSafetyTrainingPlan(SafetyTrainingPlan safetyTrainingPlan);
    public int updateSafetyTrainingPlan(SafetyTrainingPlan safetyTrainingPlan);
    public int deleteSafetyTrainingPlanByIds(Long[] planIds);
    public int deleteSafetyTrainingPlanById(Long planId);

    public int updatePlanStatus(@Param("planId") Long planId, @Param("planStatus") String planStatus, @Param("updateBy") String updateBy);

    /** 统计计划关联的培训记录数 */
    public int countRecordsByPlanId(@Param("planId") Long planId);

    /** 统计计划关联的参训人员数 */
    public int countAttendeesByPlanId(@Param("planId") Long planId);
}