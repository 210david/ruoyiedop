package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 培训课程库对象 biz_safety_training_course
 *
 * @author admin
 */
public class SafetyTrainingCourse extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long courseId;

    @Excel(name = "课程编号")
    private String courseCode;

    @Excel(name = "课程名称")
    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @Excel(name = "课程类别")
    @NotBlank(message = "课程类别不能为空")
    private String courseType;

    @Excel(name = "课程形式")
    @NotBlank(message = "课程形式不能为空")
    private String courseForm;

    @Excel(name = "标准课时")
    @NotNull(message = "标准课时不能为空")
    private BigDecimal hours;

    @Excel(name = "讲师")
    @NotBlank(message = "讲师不能为空")
    private String instructor;

    private String description;
    private String materialUrl;
    private Long enterpriseId;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }
    public String getCourseForm() { return courseForm; }
    public void setCourseForm(String courseForm) { this.courseForm = courseForm; }
    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMaterialUrl() { return materialUrl; }
    public void setMaterialUrl(String materialUrl) { this.materialUrl = materialUrl; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
