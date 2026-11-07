package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 试卷题目明细对象 biz_safety_exam_paper_question
 *
 * 快照式存储：发布试卷时冻结题干/选项/答案/解析，
 * 判分以本表 answer 快照为唯一依据，不受源题修改影响。
 *
 * @author ruoyi
 */
public class SafetyExamPaperQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long paperId;

    /** 来源题目ID */
    private Long questionId;

    /** 题型（字典 safety_exam_question_type） */
    private String questionType;

    /** 题干快照 */
    private String content;

    /** 选项快照（JSON数组字符串） */
    private String options;

    /** 答案快照（判分依据） */
    private String answer;

    /** 解析快照 */
    private String analysis;

    /** 本题分值 */
    private BigDecimal score;

    /** 题目顺序号 */
    private Integer sortNo;

    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
