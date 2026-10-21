package com.ruoyi.qms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class QmsTraceSn extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long id;
    @Excel(name = "序列号") private String snCode;
    @Excel(name = "批次号") private String batchNo;
    @Excel(name = "工单号") private String workOrderNo;
    private Long materialId;
    @Excel(name = "物料编码") private String materialCode;
    @Excel(name = "状态") private String status;
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSnCode() { return snCode; }
    public void setSnCode(String snCode) { this.snCode = snCode; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
