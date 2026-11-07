package com.ruoyi.safety.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 完整答卷存档导出对象（FR-12）
 * 含每题考生原始作答，用于检查存档。
 *
 * @author ruoyi
 */
public class SafetyExamAnswerArchive implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Excel(name = "考生")
    private String userName;

    @Excel(name = "部门")
    private String deptName;

    @Excel(name = "题号")
    private Integer sortNo;

    @Excel(name = "题型", readConverterExp = "1=单选题,2=多选题,3=判断题")
    private String questionType;

    @Excel(name = "题干")
    private String content;

    @Excel(name = "考生作答")
    private String answer;

    @Excel(name = "正确答案")
    private String correctAnswer;

    @Excel(name = "是否正确", readConverterExp = "1=正确,0=错误")
    private String isCorrect;

    @Excel(name = "得分")
    private BigDecimal score;

    @Excel(name = "交卷时间", width = 120, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getIsCorrect() { return isCorrect; }
    public void setIsCorrect(String isCorrect) { this.isCorrect = isCorrect; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
}
