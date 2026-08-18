package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工序定义 mms_process
 *
 * @author ruoyi
 */
public class MmsProcess extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long processId;

    @Excel(name = "工序编码")
    private String processCode;

    @Excel(name = "工序名称")
    private String processName;

    @Excel(name = "工序类型", readConverterExp = "0=加工,1=装配,2=检验,3=包装")
    private String processType;

    @Excel(name = "标准工时(小时)")
    private BigDecimal stdTime;

    @Excel(name = "准备时间(小时)")
    private BigDecimal prepTime;

    @Excel(name = "关键工序", readConverterExp = "0=否,1=是")
    private String isKeyProcess;

    @Excel(name = "外协", readConverterExp = "0=否,1=是")
    private String isOutsource;

    @Excel(name = "所属车间")
    private String workshopName;

    @Excel(name = "所属产线")
    private String lineName;

    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    private String delFlag;

    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public String getProcessCode() { return processCode; }
    public void setProcessCode(String processCode) { this.processCode = processCode; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getProcessType() { return processType; }
    public void setProcessType(String processType) { this.processType = processType; }
    public BigDecimal getStdTime() { return stdTime; }
    public void setStdTime(BigDecimal stdTime) { this.stdTime = stdTime; }
    public BigDecimal getPrepTime() { return prepTime; }
    public void setPrepTime(BigDecimal prepTime) { this.prepTime = prepTime; }
    public String getIsKeyProcess() { return isKeyProcess; }
    public void setIsKeyProcess(String isKeyProcess) { this.isKeyProcess = isKeyProcess; }
    public String getIsOutsource() { return isOutsource; }
    public void setIsOutsource(String isOutsource) { this.isOutsource = isOutsource; }
    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
