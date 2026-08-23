package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class MmsReturnMaterial extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private Long returnId;
    @Excel(name = "退料单号") private String returnNo;
    private Long workOrderId;
    @Excel(name = "工单号") private String workOrderNo;
    private Long materialId;
    @Excel(name = "物料编码") private String materialCode;
    @Excel(name = "物料名称") private String materialName;
    @Excel(name = "规格型号") private String specModel;
    @Excel(name = "单位") private String unit;
    @Excel(name = "退料数量") private BigDecimal returnQty;
    @Excel(name = "退料原因") private String returnReason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") private Date returnTime;
    @Excel(name = "退料人") private String returnBy;
    @Excel(name = "状态") private String status;
    private String delFlag;

    /** 退料明细列表（批量退料） */
    private List<MmsReturnDetail> detailList;

    public Long getReturnId() { return returnId; }
    public void setReturnId(Long returnId) { this.returnId = returnId; }
    public String getReturnNo() { return returnNo; }
    public void setReturnNo(String returnNo) { this.returnNo = returnNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getReturnQty() { return returnQty; }
    public void setReturnQty(BigDecimal returnQty) { this.returnQty = returnQty; }
    public String getReturnReason() { return returnReason; }
    public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
    public Date getReturnTime() { return returnTime; }
    public void setReturnTime(Date returnTime) { this.returnTime = returnTime; }
    public String getReturnBy() { return returnBy; }
    public void setReturnBy(String returnBy) { this.returnBy = returnBy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<MmsReturnDetail> getDetailList() { return detailList; }
    public void setDetailList(List<MmsReturnDetail> detailList) { this.detailList = detailList; }
}
