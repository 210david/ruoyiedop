package com.ruoyi.safety.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试题库对象 biz_safety_exam_question
 *
 * 题目为快照式组卷的来源数据；发布试卷后原题修改不影响已发布试卷。
 *
 * @author ruoyi
 */
public class SafetyExamQuestion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long questionId;

    /** 题型（字典 safety_exam_question_type：1单选 2多选 3判断） */
    @Excel(name = "题型", readConverterExp = "1=单选题,2=多选题,3=判断题")
    private String questionType;

    /** 题干 */
    @Excel(name = "题干")
    private String content;

    /** 选项（JSON数组字符串，如 ["A内容","B内容","C内容","D内容"]；判断题固定 ["正确","错误"]） */
    private String options;

    /** 正确答案（如 A / ABD / 对） */
    @Excel(name = "正确答案")
    private String answer;

    /** 答案解析 */
    @Excel(name = "答案解析")
    private String analysis;

    private Long categoryId;

    @Excel(name = "题目分类")
    private String categoryName;

    /** 难度（字典 safety_exam_difficulty：1简单 2中等 3困难） */
    @Excel(name = "难度", readConverterExp = "1=简单,2=中等,3=困难")
    private String difficulty;

    /** 适用工种（逗号分隔，字典 safety_job_type） */
    @Excel(name = "适用工种")
    private String jobTypes;

    /** 状态（0启用 1停用） */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    private Long enterpriseId;
    private String delFlag;

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
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getJobTypes() { return jobTypes; }
    public void setJobTypes(String jobTypes) { this.jobTypes = jobTypes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
