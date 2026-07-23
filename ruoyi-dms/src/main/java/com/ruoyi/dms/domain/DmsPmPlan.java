package com.ruoyi.dms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * PM预防性维护计划 dms_pm_plan
 *
 * @author ruoyi
 */
public class DmsPmPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计划ID */
    private Long planId;

    /** 计划名称 */
    @Excel(name = "计划名称")
    private String planName;

    /** 设备ID（为空则按分类） */
    private Long equipmentId;

    /** 设备名称（关联查询） */
    @Excel(name = "关联设备")
    private String equipmentName;

    /** 设备分类ID */
    private Long categoryId;

    /** 触发类型（字典 dms_pm_trigger_type） */
    @Excel(name = "触发类型", dictType = "dms_pm_trigger_type")
    private String triggerType;

    /** 周期值 */
    @Excel(name = "周期值")
    private Integer cycleValue;

    /** 周期单位（天/月/小时/件） */
    @Excel(name = "周期单位")
    private String cycleUnit;

    /** 任务清单（JSON数组） */
    private String taskList;

    /** 备件清单（JSON数组） */
    private String sparePartList;

    /** 预估工时 */
    @Excel(name = "预估工时")
    private BigDecimal estimatedHours;

    /** 提前生成天数 */
    @Excel(name = "提前天数")
    private Integer advanceDays;

    /** 是否自动派工（0否 1是） */
    @Excel(name = "自动派工", readConverterExp = "0=否,1=是")
    private String autoAssign;

    /** 默认派工人ID */
    private Long assigneeId;

    /** 默认派工人名称 */
    @Excel(name = "默认派工人")
    private String assigneeName;

    /** 上次执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastExecuteTime;

    /** 下次执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "下次执行时间", width = 30)
    private Date nextExecuteTime;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public Long getEquipmentId() { return equipmentId; }
    public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public Integer getCycleValue() { return cycleValue; }
    public void setCycleValue(Integer cycleValue) { this.cycleValue = cycleValue; }
    public String getCycleUnit() { return cycleUnit; }
    public void setCycleUnit(String cycleUnit) { this.cycleUnit = cycleUnit; }
    public String getTaskList() { return taskList; }
    public void setTaskList(String taskList) { this.taskList = taskList; }
    public String getSparePartList() { return sparePartList; }
    public void setSparePartList(String sparePartList) { this.sparePartList = sparePartList; }
    public BigDecimal getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(BigDecimal estimatedHours) { this.estimatedHours = estimatedHours; }
    public Integer getAdvanceDays() { return advanceDays; }
    public void setAdvanceDays(Integer advanceDays) { this.advanceDays = advanceDays; }
    public String getAutoAssign() { return autoAssign; }
    public void setAutoAssign(String autoAssign) { this.autoAssign = autoAssign; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public Date getLastExecuteTime() { return lastExecuteTime; }
    public void setLastExecuteTime(Date lastExecuteTime) { this.lastExecuteTime = lastExecuteTime; }
    public Date getNextExecuteTime() { return nextExecuteTime; }
    public void setNextExecuteTime(Date nextExecuteTime) { this.nextExecuteTime = nextExecuteTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
