package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工单工艺快照 mms_wo_route_snapshot
 *
 * 工单下达时从工艺路线工序复制，此后只读，保证在制工单不受工艺变更影响。
 *
 * @author ruoyi
 */
public class MmsWoRouteSnapshot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long snapshotId;

    private Long workOrderId;

    private Long routeId;

    private String routeNo;

    private String routeVersion;

    private Integer stepSeq;

    private Long processId;

    private String processCode;

    private String processName;

    private BigDecimal stdTime;

    private BigDecimal prepTime;

    private String isKeyProcess;

    private String isOutsource;

    private String delFlag;

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteNo() { return routeNo; }
    public void setRouteNo(String routeNo) { this.routeNo = routeNo; }
    public String getRouteVersion() { return routeVersion; }
    public void setRouteVersion(String routeVersion) { this.routeVersion = routeVersion; }
    public Integer getStepSeq() { return stepSeq; }
    public void setStepSeq(Integer stepSeq) { this.stepSeq = stepSeq; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public String getProcessCode() { return processCode; }
    public void setProcessCode(String processCode) { this.processCode = processCode; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
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
}
