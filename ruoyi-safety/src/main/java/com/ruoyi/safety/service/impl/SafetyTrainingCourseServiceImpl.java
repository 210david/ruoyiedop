package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyTrainingCourse;
import com.ruoyi.safety.mapper.SafetyTrainingCourseMapper;
import com.ruoyi.safety.service.ISafetyTrainingCourseService;

@Service
public class SafetyTrainingCourseServiceImpl implements ISafetyTrainingCourseService
{
    @Autowired
    private SafetyTrainingCourseMapper safetyTrainingCourseMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyTrainingCourse selectSafetyTrainingCourseById(Long courseId) { return safetyTrainingCourseMapper.selectSafetyTrainingCourseById(courseId); }

    @Override
    public List<SafetyTrainingCourse> selectSafetyTrainingCourseList(SafetyTrainingCourse safetyTrainingCourse) { return safetyTrainingCourseMapper.selectSafetyTrainingCourseList(safetyTrainingCourse); }

    @Override
    public int insertSafetyTrainingCourse(SafetyTrainingCourse safetyTrainingCourse) {
        if (StringUtils.isEmpty(safetyTrainingCourse.getCourseCode())) {
            safetyTrainingCourse.setCourseCode(mkNumberRuleService.generateNumber("safety_training_course"));
        }
        return safetyTrainingCourseMapper.insertSafetyTrainingCourse(safetyTrainingCourse); }

    @Override
    public int updateSafetyTrainingCourse(SafetyTrainingCourse safetyTrainingCourse) { return safetyTrainingCourseMapper.updateSafetyTrainingCourse(safetyTrainingCourse); }

    @Override
    public int deleteSafetyTrainingCourseByIds(Long[] courseIds) { return safetyTrainingCourseMapper.deleteSafetyTrainingCourseByIds(courseIds); }

    @Override
    public int deleteSafetyTrainingCourseById(Long courseId) { return safetyTrainingCourseMapper.deleteSafetyTrainingCourseById(courseId); }
}
