package com.ruoyi.qms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 量检具台账表 qms_gauge
 *
 * @author ruoyi
 */
public class QmsGauge extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 量检具ID */
    private Long gaugeId;

    /** 量检具编号 */
    @Excel(name = "量检具编号")
    private String gaugeNo;

    /** 量检具名称 */
    @Excel(name = "量检具名称")
    private String gaugeName;

    /** 量检具类型 */
    @Excel(name = "类型")
    private String gaugeType;

    /** 精度 */
    @Excel(name = "精度")
    private String precisionVal;

    /** 测量范围 */
    @Excel(name = "测量范围")
    private String measureRange;

    /** 校准周期（月） */
    @Excel(name = "校准周期(月)")
    private Integer calibCycle;

    /** 上次校准日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "上次校准日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date lastCalibDate;

    /** 下次校准日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "下次校准日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date nextCalibDate;

    /** 量检具状态（0在用 1停用 2待校准） */
    @Excel(name = "量检具状态", readConverterExp = "0=在用,1=停用,2=待校准")
    private String gaugeStatus;

    /** 使用部门ID */
    private Long deptId;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 删除标志 */
    private String delFlag;

    public Long getGaugeId() { return gaugeId; }
    public void setGaugeId(Long gaugeId) { this.gaugeId = gaugeId; }
    public String getGaugeNo() { return gaugeNo; }
    public void setGaugeNo(String gaugeNo) { this.gaugeNo = gaugeNo; }
    public String getGaugeName() { return gaugeName; }
    public void setGaugeName(String gaugeName) { this.gaugeName = gaugeName; }
    public String getGaugeType() { return gaugeType; }
    public void setGaugeType(String gaugeType) { this.gaugeType = gaugeType; }
    public String getPrecisionVal() { return precisionVal; }
    public void setPrecisionVal(String precisionVal) { this.precisionVal = precisionVal; }
    public String getMeasureRange() { return measureRange; }
    public void setMeasureRange(String measureRange) { this.measureRange = measureRange; }
    public Integer getCalibCycle() { return calibCycle; }
    public void setCalibCycle(Integer calibCycle) { this.calibCycle = calibCycle; }
    public Date getLastCalibDate() { return lastCalibDate; }
    public void setLastCalibDate(Date lastCalibDate) { this.lastCalibDate = lastCalibDate; }
    public Date getNextCalibDate() { return nextCalibDate; }
    public void setNextCalibDate(Date nextCalibDate) { this.nextCalibDate = nextCalibDate; }
    public String getGaugeStatus() { return gaugeStatus; }
    public void setGaugeStatus(String gaugeStatus) { this.gaugeStatus = gaugeStatus; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
