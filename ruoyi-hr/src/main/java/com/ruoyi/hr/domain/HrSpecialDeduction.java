package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrSpecialDeduction extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long sdId;
    @NotNull(message = "员工ID不能为空")
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "工号")
    private String employeeNo;
    @NotBlank(message = "扣除项目不能为空")
    @Excel(name = "扣除项目")
    private String deductionType;
    @Excel(name = "月扣除金额")
    private BigDecimal deductionAmount;
    @NotNull(message = "生效日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期")
    private Date effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "失效日期")
    private Date expiryDate;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getSdId() { return sdId; }
    public void setSdId(Long sdId) { this.sdId = sdId; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeNo() { return employeeNo; }
    public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
    public String getDeductionType() { return deductionType; }
    public void setDeductionType(String deductionType) { this.deductionType = deductionType; }
    public BigDecimal getDeductionAmount() { return deductionAmount; }
    public void setDeductionAmount(BigDecimal deductionAmount) { this.deductionAmount = deductionAmount; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
