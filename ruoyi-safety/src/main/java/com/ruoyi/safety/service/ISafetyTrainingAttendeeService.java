package com.ruoyi.safety.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.safety.domain.SafetyTrainingAttendee;

public interface ISafetyTrainingAttendeeService {
    public SafetyTrainingAttendee selectSafetyTrainingAttendeeById(Long attendeeId);
    public List<SafetyTrainingAttendee> selectSafetyTrainingAttendeeList(SafetyTrainingAttendee safetyTrainingAttendee);
    public int insertSafetyTrainingAttendee(SafetyTrainingAttendee safetyTrainingAttendee);
    public int batchInsertAttendee(List<SafetyTrainingAttendee> attendeeList);
    public int updateSafetyTrainingAttendee(SafetyTrainingAttendee safetyTrainingAttendee);
    public int deleteSafetyTrainingAttendeeByIds(Long[] attendeeIds);
    public int deleteSafetyTrainingAttendeeById(Long attendeeId);
    /** 学时统计：按用户聚合查询 */
    public List<SafetyTrainingAttendee> selectHoursStatistics(SafetyTrainingAttendee safetyTrainingAttendee);
    /** 导入参训人员 */
    public String importAttendee(List<SafetyTrainingAttendee> list, boolean updateSupport, String operName);
    /** 批量维护考试成绩 */
    public int batchUpdateExamScore(Map<String, Object> payload);
}
