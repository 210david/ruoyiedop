package com.ruoyi.mms.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class MmsQc extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long qcId;
    @Excel(name = "质检单号") private String qcNo;
    private Long workOrderId;
    @Excel(name = "工单号") private String workOrderNo;
    private Long processId;
    @Excel(name = "工序名称") private String processName;
    @Excel(name = "检验类型", readConverterExp = "0=首件,1=巡检,2=末件") private String qcType;
    @Excel(name = "检验数量") private Integer inspectQty;
    @Excel(name = "不良数量") private Integer defectQty;
    @Excel(name = "报废数量") private Integer scrapQty;
    @Excel(name = "缺陷类型") private String defectType;
    @Excel(name = "检验结论", readConverterExp = "0=合格,1=让步,2=拒收") private String qcResult;
    @Excel(name = "检验人") private String qcBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date qcTime;
    private String delFlag;

    public Long getQcId() { return qcId; }
    public void setQcId(Long qcId) { this.qcId = qcId; }
    public String getQcNo() { return qcNo; }
    public void setQcNo(String qcNo) { this.qcNo = qcNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public String getQcType() { return qcType; }
    public void setQcType(String qcType) { this.qcType = qcType; }
    public Integer getInspectQty() { return inspectQty; }
    public void setInspectQty(Integer inspectQty) { this.inspectQty = inspectQty; }
    public Integer getDefectQty() { return defectQty; }
    public void setDefectQty(Integer defectQty) { this.defectQty = defectQty; }
    public Integer getScrapQty() { return scrapQty; }
    public void setScrapQty(Integer scrapQty) { this.scrapQty = scrapQty; }
    public String getDefectType() { return defectType; }
    public void setDefectType(String defectType) { this.defectType = defectType; }
    public String getQcResult() { return qcResult; }
    public void setQcResult(String qcResult) { this.qcResult = qcResult; }
    public String getQcBy() { return qcBy; }
    public void setQcBy(String qcBy) { this.qcBy = qcBy; }
    public Date getQcTime() { return qcTime; }
    public void setQcTime(Date qcTime) { this.qcTime = qcTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
