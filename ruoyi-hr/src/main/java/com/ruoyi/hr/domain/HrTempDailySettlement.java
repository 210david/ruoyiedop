package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HrTempDailySettlement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long settlementId;
    @NotBlank(message = "结算单号不能为空")
    @Excel(name = "结算单号")
    private String settlementNo;
    @NotNull(message = "结算日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "结算日期")
    private Date settlementDate;
    @NotNull(message = "劳务工ID不能为空")
    @Excel(name = "劳务工ID")
    private Long laborId;
    @Excel(name = "姓名")
    private String workerName;
    @Excel(name = "出勤天数")
    private BigDecimal attendDays;
    @Excel(name = "日薪")
    private BigDecimal dailyWage;
    @Excel(name = "计件金额")
    private BigDecimal pieceworkAmount;
    @Excel(name = "结算总额")
    private BigDecimal totalAmount;
    @Excel(name = "支付状态")
    private String payStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "支付日期")
    private Date payDate;
    @Excel(name = "支付方式")
    private String payMethod;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }
    public String getSettlementNo() { return settlementNo; }
    public void setSettlementNo(String settlementNo) { this.settlementNo = settlementNo; }
    public Date getSettlementDate() { return settlementDate; }
    public void setSettlementDate(Date settlementDate) { this.settlementDate = settlementDate; }
    public Long getLaborId() { return laborId; }
    public void setLaborId(Long laborId) { this.laborId = laborId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public BigDecimal getAttendDays() { return attendDays; }
    public void setAttendDays(BigDecimal attendDays) { this.attendDays = attendDays; }
    public BigDecimal getDailyWage() { return dailyWage; }
    public void setDailyWage(BigDecimal dailyWage) { this.dailyWage = dailyWage; }
    public BigDecimal getPieceworkAmount() { return pieceworkAmount; }
    public void setPieceworkAmount(BigDecimal pieceworkAmount) { this.pieceworkAmount = pieceworkAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }
    public Date getPayDate() { return payDate; }
    public void setPayDate(Date payDate) { this.payDate = payDate; }
    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
