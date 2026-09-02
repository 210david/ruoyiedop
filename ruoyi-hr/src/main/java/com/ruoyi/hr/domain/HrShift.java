package com.ruoyi.hr.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;

public class HrShift extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long shiftId;
    @Excel(name = "班次编码")
    private String shiftCode;
    @NotBlank(message = "班次名称不能为空")
    @Excel(name = "班次名称")
    private String shiftName;
    @NotBlank(message = "开始时间不能为空")
    @Excel(name = "开始时间")
    private String startTime;
    @NotBlank(message = "结束时间不能为空")
    @Excel(name = "结束时间")
    private String endTime;
    @Excel(name = "跨零点")
    private Integer crossMidnight;
    @Excel(name = "工作时长")
    private BigDecimal workHours;
    @Excel(name = "休息时长")
    private BigDecimal restHours;
    @Excel(name = "用餐时间")
    private Integer mealMinutes;
    @Excel(name = "夜班")
    private Integer nightShift;
    @Excel(name = "夜班津贴")
    private BigDecimal nightAllowance;
    @Excel(name = "适用场景")
    private String applicableTo;
    @Excel(name = "排序号")
    private Integer orderNum;
    @Excel(name = "状态")
    private String status;
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getShiftCode() { return shiftCode; }
    public void setShiftCode(String shiftCode) { this.shiftCode = shiftCode; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getCrossMidnight() { return crossMidnight; }
    public void setCrossMidnight(Integer crossMidnight) { this.crossMidnight = crossMidnight; }
    public BigDecimal getWorkHours() { return workHours; }
    public void setWorkHours(BigDecimal workHours) { this.workHours = workHours; }
    public BigDecimal getRestHours() { return restHours; }
    public void setRestHours(BigDecimal restHours) { this.restHours = restHours; }
    public Integer getMealMinutes() { return mealMinutes; }
    public void setMealMinutes(Integer mealMinutes) { this.mealMinutes = mealMinutes; }
    public Integer getNightShift() { return nightShift; }
    public void setNightShift(Integer nightShift) { this.nightShift = nightShift; }
    public BigDecimal getNightAllowance() { return nightAllowance; }
    public void setNightAllowance(BigDecimal nightAllowance) { this.nightAllowance = nightAllowance; }
    public String getApplicableTo() { return applicableTo; }
    public void setApplicableTo(String applicableTo) { this.applicableTo = applicableTo; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
