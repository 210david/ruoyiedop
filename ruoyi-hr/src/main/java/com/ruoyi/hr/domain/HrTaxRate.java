package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

public class HrTaxRate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taxRateId;
    @NotNull(message = "级数不能为空")
    @Excel(name = "级数")
    private Integer level;
    @NotNull(message = "下限不能为空")
    @Excel(name = "下限")
    private BigDecimal minAmount;
    @Excel(name = "上限")
    private BigDecimal maxAmount;
    @NotNull(message = "税率不能为空")
    @Excel(name = "税率")
    private BigDecimal rate;
    @NotNull(message = "速算扣除数不能为空")
    @Excel(name = "速算扣除数")
    private BigDecimal quickDeduction;
    @NotNull(message = "生效日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期")
    private Date effectiveDate;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getTaxRateId() { return taxRateId; }
    public void setTaxRateId(Long taxRateId) { this.taxRateId = taxRateId; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public BigDecimal getQuickDeduction() { return quickDeduction; }
    public void setQuickDeduction(BigDecimal quickDeduction) { this.quickDeduction = quickDeduction; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
