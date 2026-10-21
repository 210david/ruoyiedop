package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SafetyTrainingPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long planId;
    @Excel(name = "计划编号")
    private String planCode;
    @Excel(name = "计划名称")
    @NotBlank(message = "计划名称不能为空")
    private String planName;
    @Excel(name = "计划类型")
    @NotBlank(message = "计划类型不能为空")
    private String planType;
    @Excel(name = "年度")
    @NotBlank(message = "年度不能为空")
    private String planYear;
    @Excel(name = "培训对象")
    @NotBlank(message = "培训对象不能为空")
    private String targetPersons;
    @Excel(name = "总课时")
    @NotNull(message = "总课时不能为空")
    private BigDecimal totalHours;
    @Excel(name = "计划状态", readConverterExp = "0=待执行,1=执行中,2=已完成,3=已取消")
    private String planStatus;
    private Long enterpriseId;
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
    private String delFlag;

    /* ===== 以下为非持久化字段，用于列表/详情展示进度统计 ===== */

    /** 关联培训记录数（已开展培训场次） */
    private Integer recordCount;

    /** 累计参训人数 */
    private Integer attendeeCount;

    /** 已完成课时（关联培训记录的课时合计） */
    private BigDecimal completedHours;

    /** 考试合格率（百分比，如 85.5） */
    private BigDecimal passRate;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public String getPlanYear() { return planYear; }
    public void setPlanYear(String planYear) { this.planYear = planYear; }
    public String getTargetPersons() { return targetPersons; }
    public void setTargetPersons(String targetPersons) { this.targetPersons = targetPersons; }
    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    public String getPlanStatus() { return planStatus; }
    public void setPlanStatus(String planStatus) { this.planStatus = planStatus; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }
    public Integer getAttendeeCount() { return attendeeCount; }
    public void setAttendeeCount(Integer attendeeCount) { this.attendeeCount = attendeeCount; }
    public BigDecimal getCompletedHours() { return completedHours; }
    public void setCompletedHours(BigDecimal completedHours) { this.completedHours = completedHours; }
    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal passRate) { this.passRate = passRate; }
}