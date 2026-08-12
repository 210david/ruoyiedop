package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 培训记录对象 biz_safety_training_record
 * 
 * 代表一次具体的培训活动/场次，关联课程库课程。
 * 考试分数、是否合格等个人维度数据下沉到参训人员表(biz_safety_training_attendee)。
 *
 * @author admin
 */
public class SafetyTrainingRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long planId;
    private Long courseId;
    private Long trainerId;

    @Excel(name = "课程名称")
    private String courseName;

    @Excel(name = "课程类别")
    private String courseType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "培训日期", width = 120)
    private Date trainingDate;

    @Excel(name = "培训地点")
    private String trainingLocation;

    @Excel(name = "课时")
    private BigDecimal hours;

    @Excel(name = "培训讲师")
    private String trainer;

    private String attachment;
    private Long enterpriseId;
    private String delFlag;

    /** 参训人员列表（非持久化，用于详情展示） */
    private List<SafetyTrainingAttendee> attendeeList;

    /** 参训人数（非持久化，由子查询计算） */
    @Excel(name = "参训人数")
    private Integer attendeeCount;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public Date getTrainingDate() { return trainingDate; }
    public void setTrainingDate(Date trainingDate) { this.trainingDate = trainingDate; }
    public String getTrainingLocation() { return trainingLocation; }
    public void setTrainingLocation(String trainingLocation) { this.trainingLocation = trainingLocation; }
    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }
    public String getTrainer() { return trainer; }
    public void setTrainer(String trainer) { this.trainer = trainer; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<SafetyTrainingAttendee> getAttendeeList() { return attendeeList; }
    public void setAttendeeList(List<SafetyTrainingAttendee> attendeeList) { this.attendeeList = attendeeList; }
    public Integer getAttendeeCount() { return attendeeCount; }
    public void setAttendeeCount(Integer attendeeCount) { this.attendeeCount = attendeeCount; }
}
