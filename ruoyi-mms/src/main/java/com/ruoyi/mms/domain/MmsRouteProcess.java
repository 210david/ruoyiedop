package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工艺路线工序 mms_route_process
 */
public class MmsRouteProcess extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long routeId;
    private Long processId;

    @Excel(name = "工序编码")
    private String processCode;

    @Excel(name = "工序名称")
    private String processName;

    @Excel(name = "顺序")
    private Integer stepSeq;

    @Excel(name = "标准工时")
    private BigDecimal stdTime;

    /** 准备时间(小时) */
    @Excel(name = "准备时间")
    private BigDecimal prepTime;

    @Excel(name = "是否关键工序")
    private String isKeyProcess;

    @Excel(name = "是否外协")
    private String isOutsource;

    private String delFlag;

    /** 工序类型（从工序定义表带出，仅展示用） */
    private String processType;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public String getProcessCode() { return processCode; }
    public void setProcessCode(String processCode) { this.processCode = processCode; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Integer getStepSeq() { return stepSeq; }
    public void setStepSeq(Integer stepSeq) { this.stepSeq = stepSeq; }
    public BigDecimal getStdTime() { return stdTime; }
    public void setStdTime(BigDecimal stdTime) { this.stdTime = stdTime; }
    public BigDecimal getPrepTime() { return prepTime; }
    public void setPrepTime(BigDecimal prepTime) { this.prepTime = prepTime; }
    public String getIsKeyProcess() { return isKeyProcess; }
    public void setIsKeyProcess(String isKeyProcess) { this.isKeyProcess = isKeyProcess; }
    public String getIsOutsource() { return isOutsource; }
    public void setIsOutsource(String isOutsource) { this.isOutsource = isOutsource; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProcessType() { return processType; }
    public void setProcessType(String processType) { this.processType = processType; }
}
