package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 培训参训人员对象 biz_safety_training_attendee
 *
 * 记录每个参训人员的签到状态及考试成绩。
 * examScore 和 isPass 下沉到本表，实现每人一成绩。
 *
 * @author admin
 */

/**
 * 培训参训人员对象 biz_safety_training_attendee
 *
 * 记录每个参训人员的签到状态及考试成绩。
 * examScore 和 isPass 下沉到本表，实现每人一成绩。
 *
 * @author admin
 */
public class SafetyTrainingAttendee extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long attendeeId;
    private Long recordId;
    private Long userId;

    @Excel(name = "参训人员")
    private String userName;

    private Long deptId;

    @Excel(name = "所属部门")
    private String deptName;

    @Excel(name = "签到状态", readConverterExp = "0=未签到,1=已签到,2=请假")
    private String signInStatus;

    @Excel(name = "签到时间", width = 120)
    private Date signInTime;

    @Excel(name = "签到方式")
    private String signInMethod;

    @Excel(name = "考试分数")
    private BigDecimal examScore;

    @Excel(name = "是否合格", readConverterExp = "0=不合格,1=合格")
    private String isPass;

    @Excel(name = "考试时间", width = 120)
    private Date examTime;

    private Long enterpriseId;
    private String delFlag;

    /** 非持久化：关联培训记录的课程名称（列表展示用） */
    private String courseName;
    /** 非持久化：关联培训记录的培训日期（列表展示用） */
    private Date trainingDate;
    /** 非持久化：学时统计-累计学时 */
    private BigDecimal totalHours;
    /** 非持久化：学时统计-参训次数 */
    private Integer attendCount;
    /** 非持久化：学时统计-合格次数 */
    private Integer passCount;
    /** 非持久化：学时统计-达标状态（1=达标，0=未达标） */
    private String complianceStatus;

    public Long getAttendeeId() { return attendeeId; }
    public void setAttendeeId(Long attendeeId) { this.attendeeId = attendeeId; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getSignInStatus() { return signInStatus; }
    public void setSignInStatus(String signInStatus) { this.signInStatus = signInStatus; }
    public Date getSignInTime() { return signInTime; }
    public void setSignInTime(Date signInTime) { this.signInTime = signInTime; }
    public String getSignInMethod() { return signInMethod; }
    public void setSignInMethod(String signInMethod) { this.signInMethod = signInMethod; }
    public BigDecimal getExamScore() { return examScore; }
    public void setExamScore(BigDecimal examScore) { this.examScore = examScore; }
    public String getIsPass() { return isPass; }
    public void setIsPass(String isPass) { this.isPass = isPass; }
    public Date getExamTime() { return examTime; }
    public void setExamTime(Date examTime) { this.examTime = examTime; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Date getTrainingDate() { return trainingDate; }
    public void setTrainingDate(Date trainingDate) { this.trainingDate = trainingDate; }
    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public Integer getAttendCount() { return attendCount; }
    public void setAttendCount(Integer attendCount) { this.attendCount = attendCount; }
    public Integer getPassCount() { return passCount; }
    public void setPassCount(Integer passCount) { this.passCount = passCount; }
    public String getComplianceStatus() { return complianceStatus; }
    public void setComplianceStatus(String complianceStatus) { this.complianceStatus = complianceStatus; }
}
