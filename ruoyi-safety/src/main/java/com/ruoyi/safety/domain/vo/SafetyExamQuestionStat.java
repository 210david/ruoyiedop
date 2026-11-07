package com.ruoyi.safety.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 错题率分析统计对象（FR-7）
 * 按场次维度聚合每道题的作答数/正确数/错误率，用于定位薄弱知识点。
 *
 * @author ruoyi
 */
public class SafetyExamQuestionStat implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 试卷题目快照ID */
    private Long paperQuestionId;

    /** 题干（快照） */
    private String content;

    /** 题型（1单选 2多选 3判断） */
    private String questionType;

    /** 分值 */
    private BigDecimal score;

    /** 应答人数（已交卷） */
    private Integer answerCount;

    /** 答对人数 */
    private Integer correctCount;

    /** 错误率（0~1，保留4位小数） */
    private BigDecimal errorRate;

    /** 题目分类ID（题库源） */
    private Long categoryId;

    /** 题目分类名称 */
    private String categoryName;

    public Long getPaperQuestionId() { return paperQuestionId; }
    public void setPaperQuestionId(Long paperQuestionId) { this.paperQuestionId = paperQuestionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Integer getAnswerCount() { return answerCount; }
    public void setAnswerCount(Integer answerCount) { this.answerCount = answerCount; }

    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public BigDecimal getErrorRate() { return errorRate; }
    public void setErrorRate(BigDecimal errorRate) { this.errorRate = errorRate; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
