package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyInspectionTask;

public interface SafetyInspectionTaskMapper {
    public SafetyInspectionTask selectSafetyInspectionTaskById(Long taskId);
    public List<SafetyInspectionTask> selectSafetyInspectionTaskList(SafetyInspectionTask safetyInspectionTask);
    public int insertSafetyInspectionTask(SafetyInspectionTask safetyInspectionTask);
    public int updateSafetyInspectionTask(SafetyInspectionTask safetyInspectionTask);
    public int deleteSafetyInspectionTaskByIds(Long[] taskIds);
    public int deleteSafetyInspectionTaskById(Long taskId);
}