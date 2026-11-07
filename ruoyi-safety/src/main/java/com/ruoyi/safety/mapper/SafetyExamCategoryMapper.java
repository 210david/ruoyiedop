package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyExamCategory;

/**
 * 考试题库分类 Mapper接口
 *
 * @author ruoyi
 */
public interface SafetyExamCategoryMapper
{
    public SafetyExamCategory selectSafetyExamCategoryById(Long categoryId);

    public List<SafetyExamCategory> selectSafetyExamCategoryList(SafetyExamCategory query);

    public int insertSafetyExamCategory(SafetyExamCategory category);

    public int updateSafetyExamCategory(SafetyExamCategory category);

    public int deleteSafetyExamCategoryById(Long categoryId);

    public int deleteSafetyExamCategoryByIds(Long[] categoryIds);
}
