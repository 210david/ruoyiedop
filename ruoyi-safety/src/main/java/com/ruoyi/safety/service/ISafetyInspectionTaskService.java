package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyInspectionTask;

public interface ISafetyInspectionTaskService {
    public SafetyInspectionTask selectSafetyInspectionTaskById(Long taskId);
    public List<SafetyInspectionTask> selectSafetyInspectionTaskList(SafetyInspectionTask safetyInspectionTask);
    public int insertSafetyInspectionTask(SafetyInspectionTask safetyInspectionTask);
    public int updateSafetyInspectionTask(SafetyInspectionTask safetyInspectionTask);
    public int deleteSafetyInspectionTaskByIds(Long[] taskIds);
    public int deleteSafetyInspectionTaskById(Long taskId);

    /**
     * 提交执行反馈
     */
    public int submitFeedback(SafetyInspectionTask safetyInspectionTask);

    /**
     * 作废任务
     */
    public int cancelTask(Long taskId);
}