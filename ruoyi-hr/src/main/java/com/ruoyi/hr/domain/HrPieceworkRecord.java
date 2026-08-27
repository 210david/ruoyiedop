package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrPieceworkRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long pwRecordId;
    @NotBlank(message = "报工单号不能为空")
    @Excel(name = "报工单号")
    private String pwRecordNo;
    @NotNull(message = "报工日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "报工日期")
    private Date recordDate;
    @Excel(name = "员工ID")
    private Long employeeId;
    @Excel(name = "员工姓名")
    private String employeeName;
    @Excel(name = "班组组织ID")
    private Long orgId;
    @NotNull(message = "计件方案ID不能为空")
    @Excel(name = "计件方案ID")
    private Long pwPlanId;
    @Excel(name = "计件名称")
    private String pwName;
    @NotNull(message = "合格数量不能为空")
    @Excel(name = "合格数量")
    private BigDecimal qualifiedQty;
    @Excel(name = "不合格数量")
    private BigDecimal unqualifiedQty;
    @Excel(name = "单价")
    private BigDecimal unitPrice;
    @Excel(name = "计件总额")
    private BigDecimal totalAmount;
    @Excel(name = "确认状态")
    private String confirmStatus;
    @Excel(name = "确认人")
    private String confirmBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "确认时间")
    private Date confirmTime;
    @Excel(name = "来源")
    private String source;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getPwRecordId() { return pwRecordId; }
    public void setPwRecordId(Long pwRecordId) { this.pwRecordId = pwRecordId; }
    public String getPwRecordNo() { return pwRecordNo; }
    public void setPwRecordNo(String pwRecordNo) { this.pwRecordNo = pwRecordNo; }
    public Date getRecordDate() { return recordDate; }
    public void setRecordDate(Date recordDate) { this.recordDate = recordDate; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public Long getPwPlanId() { return pwPlanId; }
    public void setPwPlanId(Long pwPlanId) { this.pwPlanId = pwPlanId; }
    public String getPwName() { return pwName; }
    public void setPwName(String pwName) { this.pwName = pwName; }
    public BigDecimal getQualifiedQty() { return qualifiedQty; }
    public void setQualifiedQty(BigDecimal qualifiedQty) { this.qualifiedQty = qualifiedQty; }
    public BigDecimal getUnqualifiedQty() { return unqualifiedQty; }
    public void setUnqualifiedQty(BigDecimal unqualifiedQty) { this.unqualifiedQty = unqualifiedQty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getConfirmStatus() { return confirmStatus; }
    public void setConfirmStatus(String confirmStatus) { this.confirmStatus = confirmStatus; }
    public String getConfirmBy() { return confirmBy; }
    public void setConfirmBy(String confirmBy) { this.confirmBy = confirmBy; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
