package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyExamQuestion;

/**
 * 考试题库 Service接口
 *
 * @author ruoyi
 */
public interface ISafetyExamQuestionService
{
    /** 查询题目列表 */
    public List<SafetyExamQuestion> selectSafetyExamQuestionList(SafetyExamQuestion query);

    /** 根据ID查询 */
    public SafetyExamQuestion selectSafetyExamQuestionById(Long questionId);

    /** 新增 */
    public int insertSafetyExamQuestion(SafetyExamQuestion question);

    /** 修改 */
    public int updateSafetyExamQuestion(SafetyExamQuestion question);

    /** 批量删除（软删除） */
    public int deleteSafetyExamQuestionByIds(Long[] questionIds);

    /** 启用/停用 */
    public int changeQuestionStatus(Long questionId, String status);

    /** Excel批量导入（行级校验，任一行非法则全量回滚） */
    public String importQuestion(List<SafetyExamQuestion> questions, String operName);

    /** FR-10：AI 出题结果批量收编入库（题干同分类查重跳过），返回 {inserted, skipped} */
    public java.util.Map<String, Integer> importFromAi(Long categoryId, List<SafetyExamQuestion> questions, String operName);
}
