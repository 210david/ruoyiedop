package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrEmployeeWorkExp extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long workExpId;
    @NotNull(message = "员工ID不能为空") @Excel(name = "员工ID")
    private Long employeeId;

    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "开始日期")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd") @Excel(name = "结束日期")
    private Date endDate;

    @Excel(name = "工作单位")
    private String companyName;

    @Excel(name = "职位")
    private String position;

    @Excel(name = "工作内容")
    private String workContent;

    @Excel(name = "离职原因")
    private String leaveReason;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "删除标志")
    private String delFlag;


    public Long getWorkExpId() { return workExpId; }
    public void setWorkExpId(Long workExpId) { this.workExpId = workExpId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getWorkContent() { return workContent; }
    public void setWorkContent(String workContent) { this.workContent = workContent; }
    public String getLeaveReason() { return leaveReason; }
    public void setLeaveReason(String leaveReason) { this.leaveReason = leaveReason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
