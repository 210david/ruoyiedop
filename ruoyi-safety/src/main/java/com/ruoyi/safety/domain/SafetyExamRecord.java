package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 考生考试记录对象 biz_safety_exam_record
 *
 * 一人一场一条记录；(exam_id, user_id) 唯一索引 + exam_result 状态机双重防重复提交。
 *
 * @author ruoyi
 */
public class SafetyExamRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long examId;
    private Long paperId;

    /** 考生用户ID */
    private Long userId;

    @Excel(name = "考生")
    private String userName;

    /** 关联员工ID（biz_safety_worker，可空） */
    private Long workerId;

    /** 关联培训参训人ID（biz_safety_training_attendee，可空） */
    private Long attendeeId;

    /** 本人开始答题时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 120, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 实际用时（秒） */
    private Integer usedSeconds;

    /** 提交方式（字典 safety_exam_submit_type：1手动 2超时自动 3切屏强制） */
    @Excel(name = "提交方式", readConverterExp = "1=手动提交,2=超时自动提交,3=切屏强制交卷")
    private String submitType;

    /** 总得分 */
    @Excel(name = "总得分")
    private BigDecimal totalScore;

    /** 是否合格（0不合格 1合格） */
    @Excel(name = "是否合格", readConverterExp = "0=不合格,1=合格")
    private String isPass;

    /** 判分状态（0未交卷 1已自动判分） */
    private String judgeStatus;

    /** 切屏次数 */
    private Integer switchCount;

    /** 考生状态（字典 safety_exam_result：0未开始 1进行中 2已交卷 3缺考 4已判分） */
    @Excel(name = "考生状态", readConverterExp = "0=未开始,1=进行中,2=已交卷,3=缺考,4=已判分")
    private String examResult;

    /** 归属部门ID（数据权限） */
    private Long deptId;

    @Excel(name = "所属部门")
    private String deptName;

    private Long enterpriseId;
    private String delFlag;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public Long getAttendeeId() { return attendeeId; }
    public void setAttendeeId(Long attendeeId) { this.attendeeId = attendeeId; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public Integer getUsedSeconds() { return usedSeconds; }
    public void setUsedSeconds(Integer usedSeconds) { this.usedSeconds = usedSeconds; }
    public String getSubmitType() { return submitType; }
    public void setSubmitType(String submitType) { this.submitType = submitType; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
    public String getIsPass() { return isPass; }
    public void setIsPass(String isPass) { this.isPass = isPass; }
    public String getJudgeStatus() { return judgeStatus; }
    public void setJudgeStatus(String judgeStatus) { this.judgeStatus = judgeStatus; }
    public Integer getSwitchCount() { return switchCount; }
    public void setSwitchCount(Integer switchCount) { this.switchCount = switchCount; }
    public String getExamResult() { return examResult; }
    public void setExamResult(String examResult) { this.examResult = examResult; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
