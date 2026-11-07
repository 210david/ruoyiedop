package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.safety.domain.SafetyExamCategory;
import com.ruoyi.safety.domain.SafetyExamQuestion;
import com.ruoyi.safety.mapper.SafetyExamCategoryMapper;
import com.ruoyi.safety.mapper.SafetyExamQuestionMapper;
import com.ruoyi.safety.service.ISafetyExamCategoryService;

/**
 * 考试题库分类 Service实现
 *
 * @author ruoyi
 */
@Service
public class SafetyExamCategoryServiceImpl implements ISafetyExamCategoryService
{
    @Autowired
    private SafetyExamCategoryMapper safetyExamCategoryMapper;

    @Autowired
    private SafetyExamQuestionMapper safetyExamQuestionMapper;

    @Override
    public List<SafetyExamCategory> selectSafetyExamCategoryList(SafetyExamCategory query)
    {
        return safetyExamCategoryMapper.selectSafetyExamCategoryList(query);
    }

    @Override
    public SafetyExamCategory selectSafetyExamCategoryById(Long categoryId)
    {
        return safetyExamCategoryMapper.selectSafetyExamCategoryById(categoryId);
    }

    @Override
    public int insertSafetyExamCategory(SafetyExamCategory category)
    {
        category.setDelFlag("0");
        if (category.getParentId() == null)
        {
            category.setParentId(0L);
        }
        if (category.getStatus() == null)
        {
            category.setStatus("0");
        }
        return safetyExamCategoryMapper.insertSafetyExamCategory(category);
    }

    @Override
    public int updateSafetyExamCategory(SafetyExamCategory category)
    {
        // 禁止将分类挂到自身或自己的子孙节点下（防环）
        if (category.getParentId() != null && category.getCategoryId() != null
                && category.getParentId().equals(category.getCategoryId()))
        {
            throw new ServiceException("父分类不能选择自身");
        }
        return safetyExamCategoryMapper.updateSafetyExamCategory(category);
    }

    @Override
    public int deleteSafetyExamCategoryByIds(Long[] categoryIds)
    {
        // 删除前校验：分类下存在题目则不允许删除
        for (Long categoryId : categoryIds)
        {
            SafetyExamQuestion probe = new SafetyExamQuestion();
            probe.setCategoryId(categoryId);
            probe.setStatus(null);
            List<SafetyExamQuestion> questions = safetyExamQuestionMapper.selectSafetyExamQuestionList(probe);
            if (questions != null && !questions.isEmpty())
            {
                SafetyExamCategory category = safetyExamCategoryMapper.selectSafetyExamCategoryById(categoryId);
                String name = category != null ? category.getCategoryName() : String.valueOf(categoryId);
                throw new ServiceException("分类【" + name + "】下存在 " + questions.size() + " 道题目，请先移除或转移题目后再删除");
            }
        }
        return safetyExamCategoryMapper.deleteSafetyExamCategoryByIds(categoryIds);
    }
}
