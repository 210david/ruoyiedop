package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrPieceworkPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long pwPlanId;
    @NotBlank(message = "计件编码不能为空")
    @Excel(name = "计件编码")
    private String pwCode;
    @NotBlank(message = "计件名称不能为空")
    @Excel(name = "计件名称")
    private String pwName;
    @Excel(name = "计件类型")
    private String pwType;
    @Excel(name = "单位")
    private String unit;
    @NotNull(message = "计件单价不能为空")
    @Excel(name = "计件单价")
    private BigDecimal unitPrice;
    @Excel(name = "产品ID")
    private Long productId;
    @Excel(name = "工序ID")
    private Long processId;
    @Excel(name = "适用组织ID")
    private Long orgId;
    @Excel(name = "保底工资")
    private BigDecimal baseSalary;
    @NotNull(message = "生效日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期")
    private Date effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "失效日期")
    private Date expiryDate;
    @Excel(name = "审批状态")
    private String auditStatus;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getPwPlanId() { return pwPlanId; }
    public void setPwPlanId(Long pwPlanId) { this.pwPlanId = pwPlanId; }
    public String getPwCode() { return pwCode; }
    public void setPwCode(String pwCode) { this.pwCode = pwCode; }
    public String getPwName() { return pwName; }
    public void setPwName(String pwName) { this.pwName = pwName; }
    public String getPwType() { return pwType; }
    public void setPwType(String pwType) { this.pwType = pwType; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
