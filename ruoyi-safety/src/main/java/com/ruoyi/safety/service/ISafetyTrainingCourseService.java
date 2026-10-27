package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyTrainingCourse;

public interface ISafetyTrainingCourseService {
    public SafetyTrainingCourse selectSafetyTrainingCourseById(Long courseId);
    public List<SafetyTrainingCourse> selectSafetyTrainingCourseList(SafetyTrainingCourse safetyTrainingCourse);
    public int insertSafetyTrainingCourse(SafetyTrainingCourse safetyTrainingCourse);
    public int updateSafetyTrainingCourse(SafetyTrainingCourse safetyTrainingCourse);
    public int deleteSafetyTrainingCourseByIds(Long[] courseIds);
    public int deleteSafetyTrainingCourseById(Long courseId);
}
