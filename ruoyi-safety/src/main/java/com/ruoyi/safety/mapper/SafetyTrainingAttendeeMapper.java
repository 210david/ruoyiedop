package com.ruoyi.safety.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyTrainingAttendee;

public interface SafetyTrainingAttendeeMapper {
    public SafetyTrainingAttendee selectSafetyTrainingAttendeeById(Long attendeeId);
    public List<SafetyTrainingAttendee> selectSafetyTrainingAttendeeList(SafetyTrainingAttendee safetyTrainingAttendee);
    public int insertSafetyTrainingAttendee(SafetyTrainingAttendee safetyTrainingAttendee);
    public int updateSafetyTrainingAttendee(SafetyTrainingAttendee safetyTrainingAttendee);
    public int deleteSafetyTrainingAttendeeByIds(Long[] attendeeIds);
    public int deleteSafetyTrainingAttendeeById(Long attendeeId);
    public int batchInsertAttendee(List<SafetyTrainingAttendee> attendeeList);
    /** 学时统计：按用户聚合查询 */
    public List<SafetyTrainingAttendee> selectHoursStatistics(SafetyTrainingAttendee safetyTrainingAttendee);
    /** 检查同一培训记录下是否已存在该用户 */
    public SafetyTrainingAttendee checkDuplicate(@Param("recordId") Long recordId, @Param("userId") Long userId);
    /** 根据培训记录ID查询已存在的用户ID列表 */
    public List<Long> selectExistingUserIdsByRecordId(@Param("recordId") Long recordId);
    /** 批量更新考试成绩 */
    public int batchUpdateExamScore(@Param("attendeeIds") Long[] attendeeIds, @Param("isPass") String isPass, @Param("examScore") BigDecimal examScore, @Param("examTime") java.util.Date examTime);
}
