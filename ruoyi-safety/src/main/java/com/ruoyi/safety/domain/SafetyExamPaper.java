package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试试卷对象 biz_safety_exam_paper
 *
 * 试卷发布后题目集合冻结（快照存储于 biz_safety_exam_paper_question），
 * 源题后续修改/停用不影响已发布试卷的完整性与判分一致性。
 *
 * @author ruoyi
 */
public class SafetyExamPaper extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long paperId;

    /** 试卷名称 */
    private String paperName;

    /** 关联培训课程ID（可空） */
    private Long courseId;

    /** 课程名称（冗余，列表展示用） */
    private String courseName;

    /** 总分（按题目分值自动计算） */
    private BigDecimal totalScore;

    /** 及格分 */
    private BigDecimal passScore;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 题目乱序（Y是 N否） */
    private String shuffleQuestion;

    /** 选项乱序（Y是 N否） */
    private String shuffleOption;

    /** 切屏限制次数（0不限） */
    private Integer switchLimit;

    /** 交卷后展示解析（Y是 N否） */
    private String showAnalysis;

    /** 试卷状态（字典 safety_exam_paper_status：0草稿 1已发布 2停用） */
    private String status;

    private String publishBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    private Long enterpriseId;
    private String delFlag;

    /** 题目明细（仅用于详情回显与保存提交，不落 paper 表） */
    private List<SafetyExamPaperQuestion> questions;

    public List<SafetyExamPaperQuestion> getQuestions() { return questions; }
    public void setQuestions(List<SafetyExamPaperQuestion> questions) { this.questions = questions; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public BigDecimal getPassScore() { return passScore; }
    public void setPassScore(BigDecimal passScore) { this.passScore = passScore; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getShuffleQuestion() { return shuffleQuestion; }
    public void setShuffleQuestion(String shuffleQuestion) { this.shuffleQuestion = shuffleQuestion; }
    public String getShuffleOption() { return shuffleOption; }
    public void setShuffleOption(String shuffleOption) { this.shuffleOption = shuffleOption; }
    public Integer getSwitchLimit() { return switchLimit; }
    public void setSwitchLimit(Integer switchLimit) { this.switchLimit = switchLimit; }
    public String getShowAnalysis() { return showAnalysis; }
    public void setShowAnalysis(String showAnalysis) { this.showAnalysis = showAnalysis; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPublishBy() { return publishBy; }
    public void setPublishBy(String publishBy) { this.publishBy = publishBy; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
