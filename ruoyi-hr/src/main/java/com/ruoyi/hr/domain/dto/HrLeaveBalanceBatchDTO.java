package com.ruoyi.hr.domain.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 假期余额批量生成DTO
 * 按部门+假别+年度，为部门下所有在职员工批量生成假期余额记录
 */
public class HrLeaveBalanceBatchDTO
{
    /** 部门ID列表（为空则处理全部部门） */
    private List<Long> deptIds;
    /** 假别列表 */
    private List<String> leaveTypes;
    /** 年度 */
    private Integer year;
    /** 各假别对应的总额度（天），key=假别代码，value=总额度 */
    private List<LeaveTypeDays> leaveTypeDaysList;
    /** 是否覆盖已有记录 */
    private Boolean overwrite;

    public List<Long> getDeptIds() { return deptIds; }
    public void setDeptIds(List<Long> deptIds) { this.deptIds = deptIds; }
    public List<String> getLeaveTypes() { return leaveTypes; }
    public void setLeaveTypes(List<String> leaveTypes) { this.leaveTypes = leaveTypes; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public List<LeaveTypeDays> getLeaveTypeDaysList() { return leaveTypeDaysList; }
    public void setLeaveTypeDaysList(List<LeaveTypeDays> leaveTypeDaysList) { this.leaveTypeDaysList = leaveTypeDaysList; }
    public Boolean getOverwrite() { return overwrite; }
    public void setOverwrite(Boolean overwrite) { this.overwrite = overwrite; }

    /**
     * 单个假别+额度的配置项
     */
    public static class LeaveTypeDays
    {
        private String leaveType;
        private BigDecimal totalDays;

        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
        public BigDecimal getTotalDays() { return totalDays; }
        public void setTotalDays(BigDecimal totalDays) { this.totalDays = totalDays; }
    }
}
