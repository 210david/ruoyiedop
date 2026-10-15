package com.ruoyi.food.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 在制品温湿度/上霉状态记录 food_wip_env
 *
 * @author ruoyi
 */
public class FoodWipEnv extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long envId;

    private Long wipId;

    @Excel(name = "垛号")
    private String wipNo;

    @Excel(name = "工序序号")
    private Integer stepSeq;

    @Excel(name = "工序名称")
    private String processName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "记录时间")
    private Date recordTime;

    @Excel(name = "温度(℃)")
    private BigDecimal temp;

    @Excel(name = "湿度(%)")
    private BigDecimal humidity;

    /** 上霉状态(字典food_mold_status) */
    @Excel(name = "上霉状态")
    private String moldStatus;

    @Excel(name = "记录人")
    private String recorder;

    private String delFlag;

    public Long getEnvId() { return envId; }
    public void setEnvId(Long envId) { this.envId = envId; }
    public Long getWipId() { return wipId; }
    public void setWipId(Long wipId) { this.wipId = wipId; }
    public String getWipNo() { return wipNo; }
    public void setWipNo(String wipNo) { this.wipNo = wipNo; }
    public Integer getStepSeq() { return stepSeq; }
    public void setStepSeq(Integer stepSeq) { this.stepSeq = stepSeq; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Date getRecordTime() { return recordTime; }
    public void setRecordTime(Date recordTime) { this.recordTime = recordTime; }
    public BigDecimal getTemp() { return temp; }
    public void setTemp(BigDecimal temp) { this.temp = temp; }
    public BigDecimal getHumidity() { return humidity; }
    public void setHumidity(BigDecimal humidity) { this.humidity = humidity; }
    public String getMoldStatus() { return moldStatus; }
    public void setMoldStatus(String moldStatus) { this.moldStatus = moldStatus; }
    public String getRecorder() { return recorder; }
    public void setRecorder(String recorder) { this.recorder = recorder; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
