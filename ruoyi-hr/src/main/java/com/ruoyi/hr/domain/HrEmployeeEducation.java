package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrEmployeeEducation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long educationId;
    @NotNull(message = "员工ID不能为空") @Excel(name = "员工ID")
    private Long employeeId;

    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "开始日期")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "结束日期")
    private Date endDate;

    @Excel(name = "学校名称")
    private String schoolName;

    @Excel(name = "专业")
    private String major;

    @Excel(name = "学历")
    private String eduLevel;

    @Excel(name = "学位")
    private String degree;

    @Excel(name = "是否最高学历")
    private String isHighest;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "删除标志")
    private String delFlag;


    public Long getEducationId() { return educationId; }
    public void setEducationId(Long educationId) { this.educationId = educationId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getEduLevel() { return eduLevel; }
    public void setEduLevel(String eduLevel) { this.eduLevel = eduLevel; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public String getIsHighest() { return isHighest; }
    public void setIsHighest(String isHighest) { this.isHighest = isHighest; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
