package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyExamPaperQuestion;

/**
 * 试卷题目明细（快照） Mapper接口
 *
 * @author ruoyi
 */
public interface SafetyExamPaperQuestionMapper
{
    public SafetyExamPaperQuestion selectSafetyExamPaperQuestionById(Long id);

    public List<SafetyExamPaperQuestion> selectSafetyExamPaperQuestionList(SafetyExamPaperQuestion query);

    /** 按试卷ID查询题目明细（按 sort_no 排序） */
    public List<SafetyExamPaperQuestion> selectByPaperId(Long paperId);

    public int insertSafetyExamPaperQuestion(SafetyExamPaperQuestion paperQuestion);

    /** 批量插入（发布试卷快照用） */
    public int batchInsert(@Param("list") List<SafetyExamPaperQuestion> list);

    public int updateSafetyExamPaperQuestion(SafetyExamPaperQuestion paperQuestion);

    public int deleteSafetyExamPaperQuestionById(Long id);

    /** 按试卷ID删除全部题目明细（重新组卷用） */
    public int deleteByPaperId(Long paperId);
}
