package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyExamCategory;

/**
 * 考试题库分类 Service接口
 *
 * @author ruoyi
 */
public interface ISafetyExamCategoryService
{
    /** 查询分类列表 */
    public List<SafetyExamCategory> selectSafetyExamCategoryList(SafetyExamCategory query);

    /** 根据ID查询 */
    public SafetyExamCategory selectSafetyExamCategoryById(Long categoryId);

    /** 新增 */
    public int insertSafetyExamCategory(SafetyExamCategory category);

    /** 修改 */
    public int updateSafetyExamCategory(SafetyExamCategory category);

    /** 批量删除（软删除，删除前校验分类下是否有题目） */
    public int deleteSafetyExamCategoryByIds(Long[] categoryIds);
}
