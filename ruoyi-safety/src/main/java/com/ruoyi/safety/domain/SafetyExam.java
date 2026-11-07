package com.ruoyi.safety.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考试场次对象 biz_safety_exam
 *
 * 基于试卷发起考试场次，指定时间窗口与参考人员。
 * 状态机：0草稿 → 1已发布 → 2考试中 → 3已结束；1→4 可撤回（未开始时）。
 *
 * @author ruoyi
 */
public class SafetyExam extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long examId;

    /** 考试名称 */
    @Excel(name = "考试名称")
    private String examName;

    private Long paperId;

    @Excel(name = "试卷名称")
    private String paperName;

    /** 关联培训记录ID（可空，关联后判分自动回写参训人成绩；考生来源=来自培训时必填） */
    private Long recordId;

    /** 考生来源（1=来自培训参训人 2=手动添加） */
    private String attendeeSource;

    /** 补考来源场次ID（FR-8，NULL=非补考） */
    private Long retakeFromId;

    /** 补考代数（0=首次考试，1=第1次补考...） */
    private Integer retakeCount;

    /** 成绩覆盖策略（1=取最高 2=取最新，回写参训人时生效） */
    private String scoreStrategy;

    /** 考试开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 120, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 考试结束时间（窗口截止） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 120, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 考试状态（字典 safety_exam_exam_status：0草稿 1已发布 2考试中 3已结束 4已撤回） */
    @Excel(name = "考试状态", readConverterExp = "0=草稿,1=已发布,2=考试中,3=已结束,4=已撤回")
    private String examStatus;

    private String publishBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 归属部门ID（数据权限） */
    private Long deptId;

    private Long enterpriseId;
    private String delFlag;

    /** 非持久化：参考人员ID数组（考生来源=手动添加时勾选） */
    private Long[] userIds;

    /** 非持久化：手动考试名单（详情回显） */
    private List<SafetyExamUser> manualUsers;

    /** 非持久化：已交卷人数（列表进度展示） */
    private Integer submittedCount;

    /** 非持久化：应考人数（列表进度展示） */
    private Integer totalCount;

    /** 非持久化：缺考人数（列表展示） */
    private Integer absentCount;

    /** 非持久化：及格率%（列表展示） */
    private java.math.BigDecimal passRate;

    /** 非持久化：平均分（列表展示） */
    private java.math.BigDecimal avgScore;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public String getPaperName() { return paperName; }
    public void setPaperName(String paperName) { this.paperName = paperName; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getAttendeeSource() { return attendeeSource; }
    public void setAttendeeSource(String attendeeSource) { this.attendeeSource = attendeeSource; }

    public Long getRetakeFromId() { return retakeFromId; }
    public void setRetakeFromId(Long retakeFromId) { this.retakeFromId = retakeFromId; }

    public Integer getRetakeCount() { return retakeCount; }
    public void setRetakeCount(Integer retakeCount) { this.retakeCount = retakeCount; }

    public String getScoreStrategy() { return scoreStrategy; }
    public void setScoreStrategy(String scoreStrategy) { this.scoreStrategy = scoreStrategy; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getExamStatus() { return examStatus; }
    public void setExamStatus(String examStatus) { this.examStatus = examStatus; }
    public String getPublishBy() { return publishBy; }
    public void setPublishBy(String publishBy) { this.publishBy = publishBy; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Long[] getUserIds() { return userIds; }
    public void setUserIds(Long[] userIds) { this.userIds = userIds; }
    public List<SafetyExamUser> getManualUsers() { return manualUsers; }
    public void setManualUsers(List<SafetyExamUser> manualUsers) { this.manualUsers = manualUsers; }
    public Integer getSubmittedCount() { return submittedCount; }
    public void setSubmittedCount(Integer submittedCount) { this.submittedCount = submittedCount; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public Integer getAbsentCount() { return absentCount; }
    public void setAbsentCount(Integer absentCount) { this.absentCount = absentCount; }
    public java.math.BigDecimal getPassRate() { return passRate; }
    public void setPassRate(java.math.BigDecimal passRate) { this.passRate = passRate; }
    public java.math.BigDecimal getAvgScore() { return avgScore; }
    public void setAvgScore(java.math.BigDecimal avgScore) { this.avgScore = avgScore; }
}
