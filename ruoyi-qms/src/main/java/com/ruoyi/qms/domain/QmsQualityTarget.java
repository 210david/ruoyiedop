package com.ruoyi.qms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsQualityTarget extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long targetId;
    @Excel(name = "年份") private Integer targetYear;
    @Excel(name = "月份") private Integer targetMonth;
    @Excel(name = "维度") private String dimension;
    @Excel(name = "维度值") private String dimensionVal;
    private BigDecimal targetPassRate;
    private BigDecimal actualPassRate;
    private Integer targetPpm;
    private Integer actualPpm;
    private BigDecimal targetComplaintRate;
    private BigDecimal actualComplaintRate;
    private BigDecimal targetCapaCloseRate;
    private BigDecimal actualCapaCloseRate;
    @Excel(name = "达成") private String achievement;
    private String delFlag;
    private String status;

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public Integer getTargetYear() { return targetYear; }
    public void setTargetYear(Integer targetYear) { this.targetYear = targetYear; }
    public Integer getTargetMonth() { return targetMonth; }
    public void setTargetMonth(Integer targetMonth) { this.targetMonth = targetMonth; }
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    public String getDimensionVal() { return dimensionVal; }
    public void setDimensionVal(String dimensionVal) { this.dimensionVal = dimensionVal; }
    public BigDecimal getTargetPassRate() { return targetPassRate; }
    public void setTargetPassRate(BigDecimal targetPassRate) { this.targetPassRate = targetPassRate; }
    public BigDecimal getActualPassRate() { return actualPassRate; }
    public void setActualPassRate(BigDecimal actualPassRate) { this.actualPassRate = actualPassRate; }
    public Integer getTargetPpm() { return targetPpm; }
    public void setTargetPpm(Integer targetPpm) { this.targetPpm = targetPpm; }
    public Integer getActualPpm() { return actualPpm; }
    public void setActualPpm(Integer actualPpm) { this.actualPpm = actualPpm; }
    public BigDecimal getTargetComplaintRate() { return targetComplaintRate; }
    public void setTargetComplaintRate(BigDecimal targetComplaintRate) { this.targetComplaintRate = targetComplaintRate; }
    public BigDecimal getActualComplaintRate() { return actualComplaintRate; }
    public void setActualComplaintRate(BigDecimal actualComplaintRate) { this.actualComplaintRate = actualComplaintRate; }
    public BigDecimal getTargetCapaCloseRate() { return targetCapaCloseRate; }
    public void setTargetCapaCloseRate(BigDecimal targetCapaCloseRate) { this.targetCapaCloseRate = targetCapaCloseRate; }
    public BigDecimal getActualCapaCloseRate() { return actualCapaCloseRate; }
    public void setActualCapaCloseRate(BigDecimal actualCapaCloseRate) { this.actualCapaCloseRate = actualCapaCloseRate; }
    public String getAchievement() { return achievement; }
    public void setAchievement(String achievement) { this.achievement = achievement; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
