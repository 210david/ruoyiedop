package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 派工单 mms_dispatch
 *
 * 派工状态机：
 * 0(待开工) → 1(进行中) → 2(已完成)
 *              ↓
 *          3(已取消)
 *
 * @author ruoyi
 */
public class MmsDispatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long dispatchId;

    @Excel(name = "派工单号")
    private String dispatchNo;

    private Long workOrderId;

    @Excel(name = "工单编号")
    private String workOrderNo;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "工序序号")
    private Integer opSeq;

    private Long processId;

    @Excel(name = "工序名称")
    private String processName;

    private Long resourceId;

    @Excel(name = "产能单元")
    private String resourceName;

    private Long teamId;

    @Excel(name = "班组")
    private String teamName;

    @Excel(name = "派工人员")
    private String userIds;

    @Excel(name = "计划数量")
    private BigDecimal planQty;

    @Excel(name = "合格数量")
    private BigDecimal goodQty;

    @Excel(name = "不良数量")
    private BigDecimal defectQty;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划开始", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划结束", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planEnd;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualEnd;

    @Excel(name = "状态", readConverterExp = "0=待开工,1=进行中,2=已完成,3=已取消")
    private String status;

    private String delFlag;

    public Long getDispatchId() { return dispatchId; }
    public void setDispatchId(Long dispatchId) { this.dispatchId = dispatchId; }
    public String getDispatchNo() { return dispatchNo; }
    public void setDispatchNo(String dispatchNo) { this.dispatchNo = dispatchNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getOpSeq() { return opSeq; }
    public void setOpSeq(Integer opSeq) { this.opSeq = opSeq; }
    public Long getProcessId() { return processId; }
    public void setProcessId(Long processId) { this.processId = processId; }
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getUserIds() { return userIds; }
    public void setUserIds(String userIds) { this.userIds = userIds; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public BigDecimal getGoodQty() { return goodQty; }
    public void setGoodQty(BigDecimal goodQty) { this.goodQty = goodQty; }
    public BigDecimal getDefectQty() { return defectQty; }
    public void setDefectQty(BigDecimal defectQty) { this.defectQty = defectQty; }
    public Date getPlanStart() { return planStart; }
    public void setPlanStart(Date planStart) { this.planStart = planStart; }
    public Date getPlanEnd() { return planEnd; }
    public void setPlanEnd(Date planEnd) { this.planEnd = planEnd; }
    public Date getActualStart() { return actualStart; }
    public void setActualStart(Date actualStart) { this.actualStart = actualStart; }
    public Date getActualEnd() { return actualEnd; }
    public void setActualEnd(Date actualEnd) { this.actualEnd = actualEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
