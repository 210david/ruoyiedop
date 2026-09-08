package com.ruoyi.hr.domain.dto;

import java.util.List;

/**
 * 批量排班参数
 */
public class HrScheduleBatchDTO
{
    /** 员工ID列表（与部门ID二选一） */
    private List<Long> employeeIds;

    /** 部门ID（按部门排班时使用，与员工列表二选一） */
    private Long orgId;

    /** 开始日期 yyyy-MM-dd */
    private String startDate;

    /** 结束日期 yyyy-MM-dd */
    private String endDate;

    /** 模板ID（使用模板排班时） */
    private Long templateId;

    /** 班次ID（使用固定班次排班时，与模板二选一） */
    private Long shiftId;

    /** 班次名称 */
    private String shiftName;

    /** 是否覆盖已存在排班 */
    private Boolean overwrite;

    /** 排除周末（周六周日） */
    private Boolean excludeWeekend;

    public List<Long> getEmployeeIds() { return employeeIds; }
    public void setEmployeeIds(List<Long> employeeIds) { this.employeeIds = employeeIds; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public Boolean getOverwrite() { return overwrite; }
    public void setOverwrite(Boolean overwrite) { this.overwrite = overwrite; }
    public Boolean getExcludeWeekend() { return excludeWeekend; }
    public void setExcludeWeekend(Boolean excludeWeekend) { this.excludeWeekend = excludeWeekend; }
}
