package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyExamQuestion;

/**
 * 考试题库 Mapper接口
 *
 * @author ruoyi
 */
public interface SafetyExamQuestionMapper
{
    public SafetyExamQuestion selectSafetyExamQuestionById(Long questionId);

    public List<SafetyExamQuestion> selectSafetyExamQuestionList(SafetyExamQuestion query);

    public int insertSafetyExamQuestion(SafetyExamQuestion question);

    public int updateSafetyExamQuestion(SafetyExamQuestion question);

    public int deleteSafetyExamQuestionById(Long questionId);

    public int deleteSafetyExamQuestionByIds(Long[] questionIds);

    /** 批量插入（Excel导入用） */
    public int batchInsertSafetyExamQuestion(@Param("list") List<SafetyExamQuestion> questions);

    /** 随机抽题：按题型/难度/分类抽取指定数量 */
    public List<SafetyExamQuestion> selectRandomQuestions(@Param("questionType") String questionType,
                                                          @Param("difficulty") String difficulty,
                                                          @Param("categoryId") Long categoryId,
                                                          @Param("count") Integer count);
}
