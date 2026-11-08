package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyExamPaper;
import com.ruoyi.safety.domain.SafetyExamPaperQuestion;
import com.ruoyi.safety.domain.SafetyExamQuestion;

/**
 * 考试试卷 Service接口
 *
 * @author ruoyi
 */
public interface ISafetyExamPaperService
{
    public List<SafetyExamPaper> selectSafetyExamPaperList(SafetyExamPaper query);

    /** 详情（含题目快照明细） */
    public SafetyExamPaper selectSafetyExamPaperById(Long paperId);

    public int insertSafetyExamPaper(SafetyExamPaper paper);

    public int updateSafetyExamPaper(SafetyExamPaper paper);

    public int deleteSafetyExamPaperByIds(Long[] paperIds);

    /** 保存试卷题目集合（全量替换，保存即快照，仅草稿可操作；自动回写总分） */
    public int savePaperQuestions(SafetyExamPaper paper);

    /** 发布试卷（锁定状态，校验题目非空与及格分） */
    public int publishPaper(Long paperId, String operName);

    /** 撤回发布（无考试场次引用时退回草稿） */
    public int unpublishPaper(Long paperId);

    /** 随机抽题（管理端预览，不入库） */
    public List<SafetyExamQuestion> randomPick(Long categoryId, String questionType, String difficulty, Integer count);
}
