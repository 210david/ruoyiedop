package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 班次定义 mms_shift
 *
 * @author ruoyi
 */
public class MmsShift extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long shiftId;

    @Excel(name = "班次名称")
    private String shiftName;

    @Excel(name = "开始时间")
    private String startTime;

    @Excel(name = "结束时间")
    private String endTime;

    private Integer restMinutes;

    @Excel(name = "可用工时")
    private BigDecimal availHours;

    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    private String delFlag;

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public Integer getRestMinutes() { return restMinutes; }
    public void setRestMinutes(Integer restMinutes) { this.restMinutes = restMinutes; }
    public BigDecimal getAvailHours() { return availHours; }
    public void setAvailHours(BigDecimal availHours) { this.availHours = availHours; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
