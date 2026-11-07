package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试答题明细对象 biz_safety_exam_answer
 *
 * 逐题作答明细，作答实时 upsert（草稿），提交时判分回填 is_correct/score。
 * (record_id, paper_question_id) 唯一索引支撑幂等 upsert。
 *
 * @author ruoyi
 */
public class SafetyExamAnswer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long answerId;
    private Long recordId;
    private Long paperQuestionId;

    /** 考生答案（如 A / ABD；空=未答） */
    private String userAnswer;

    /** 是否正确（1正确 0错误） */
    private String isCorrect;

    /** 本题得分 */
    private BigDecimal score;

    private String delFlag;

    public Long getAnswerId() { return answerId; }
    public void setAnswerId(Long answerId) { this.answerId = answerId; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getPaperQuestionId() { return paperQuestionId; }
    public void setPaperQuestionId(Long paperQuestionId) { this.paperQuestionId = paperQuestionId; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
    public String getIsCorrect() { return isCorrect; }
    public void setIsCorrect(String isCorrect) { this.isCorrect = isCorrect; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
