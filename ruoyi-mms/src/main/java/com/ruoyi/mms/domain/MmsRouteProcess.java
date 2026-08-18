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

    /** 绑定产能单元ID(MD-12) */
    private Long resourceId;

    /** 产能单元名称 */
    @Excel(name = "产能单元")
    private String resourceName;

    /** 绑定设备ID */
    private Long equipId;

    /** 设备名称 */
    @Excel(name = "设备")
    private String equipName;

    /** 模具/工装ID */
    private Long moldId;

    /** 模具/工装名称 */
    @Excel(name = "模具/工装")
    private String moldName;

    /** 作业指导书ID(MD-14) */
    private Long sopId;

    /** 作业指导书编号 */
    @Excel(name = "SOP编号")
    private String sopNo;

    /** 作业指导书名称 */
    @Excel(name = "SOP名称")
    private String sopName;

    /** 工艺参数模板JSON(MD-13) */
    private String paramTemplate;

    private String delFlag;

    /** 工序类型（从工序定义表带出） */
    private String processType;

    /** 工序定义表.所属车间 */
    private String workshopName;

    /** 工序定义表.所属产线 */
    private String lineName;

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
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public Long getEquipId() { return equipId; }
    public void setEquipId(Long equipId) { this.equipId = equipId; }
    public String getEquipName() { return equipName; }
    public void setEquipName(String equipName) { this.equipName = equipName; }
    public Long getMoldId() { return moldId; }
    public void setMoldId(Long moldId) { this.moldId = moldId; }
    public String getMoldName() { return moldName; }
    public void setMoldName(String moldName) { this.moldName = moldName; }
    public Long getSopId() { return sopId; }
    public void setSopId(Long sopId) { this.sopId = sopId; }
    public String getSopNo() { return sopNo; }
    public void setSopNo(String sopNo) { this.sopNo = sopNo; }
    public String getSopName() { return sopName; }
    public void setSopName(String sopName) { this.sopName = sopName; }
    public String getParamTemplate() { return paramTemplate; }
    public void setParamTemplate(String paramTemplate) { this.paramTemplate = paramTemplate; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getProcessType() { return processType; }
    public void setProcessType(String processType) { this.processType = processType; }
    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
}
