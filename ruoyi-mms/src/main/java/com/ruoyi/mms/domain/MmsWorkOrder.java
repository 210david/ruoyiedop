package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 生产工单 mms_work_order
 *
 * @author ruoyi
 */
public class MmsWorkOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long workOrderId;

    @Excel(name = "工单号")
    private String workOrderNo;

    @Excel(name = "工单类型", readConverterExp = "0=生产,1=返工,2=外协")
    private String orderType;

    private Long mpsId;

    @Excel(name = "关联主计划号")
    private String mpsNo;

    @Excel(name = "关联需求号")
    private String demandNo;

    private Long productId;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位")
    private String unit;

    @Excel(name = "计划数量")
    private BigDecimal planQty;

    private Long bomId;

    @Excel(name = "BOM编号")
    private String bomNo;

    private Long routeId;

    @Excel(name = "工艺路线编号")
    private String routeNo;

    private Long resourceId;

    @Excel(name = "产能单元")
    private String resourceName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划开工", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划完工", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planFinish;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actualFinish;

    @Excel(name = "完工数量")
    private BigDecimal finishedQty;

    @Excel(name = "合格数量")
    private BigDecimal qualifiedQty;

    @Excel(name = "不良数量")
    private BigDecimal defectQty;

    @Excel(name = "优先级", readConverterExp = "0=高,1=中,2=低")
    private String priority;

    @Excel(name = "状态", readConverterExp = "0=新建,1=已下达,2=执行中,3=报工中,4=待完工质检,5=完工入库,6=已关闭,7=已暂停,8=已作废")
    private String status;

    private String auditBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    private String auditOpinion;

    private String pauseReason;
    private String closeRemark;
    private String delFlag;

    /** 下达人 */
    private String releaseBy;

    /** 下达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public Long getMpsId() { return mpsId; }
    public void setMpsId(Long mpsId) { this.mpsId = mpsId; }
    public String getMpsNo() { return mpsNo; }
    public void setMpsNo(String mpsNo) { this.mpsNo = mpsNo; }
    public String getDemandNo() { return demandNo; }
    public void setDemandNo(String demandNo) { this.demandNo = demandNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public String getBomNo() { return bomNo; }
    public void setBomNo(String bomNo) { this.bomNo = bomNo; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteNo() { return routeNo; }
    public void setRouteNo(String routeNo) { this.routeNo = routeNo; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public Date getPlanStart() { return planStart; }
    public void setPlanStart(Date planStart) { this.planStart = planStart; }
    public Date getPlanFinish() { return planFinish; }
    public void setPlanFinish(Date planFinish) { this.planFinish = planFinish; }
    public Date getActualStart() { return actualStart; }
    public void setActualStart(Date actualStart) { this.actualStart = actualStart; }
    public Date getActualFinish() { return actualFinish; }
    public void setActualFinish(Date actualFinish) { this.actualFinish = actualFinish; }
    public BigDecimal getFinishedQty() { return finishedQty; }
    public void setFinishedQty(BigDecimal finishedQty) { this.finishedQty = finishedQty; }
    public BigDecimal getQualifiedQty() { return qualifiedQty; }
    public void setQualifiedQty(BigDecimal qualifiedQty) { this.qualifiedQty = qualifiedQty; }
    public BigDecimal getDefectQty() { return defectQty; }
    public void setDefectQty(BigDecimal defectQty) { this.defectQty = defectQty; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public String getPauseReason() { return pauseReason; }
    public void setPauseReason(String pauseReason) { this.pauseReason = pauseReason; }
    public String getCloseRemark() { return closeRemark; }
    public void setCloseRemark(String closeRemark) { this.closeRemark = closeRemark; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getReleaseBy() { return releaseBy; }
    public void setReleaseBy(String releaseBy) { this.releaseBy = releaseBy; }
    public Date getReleaseTime() { return releaseTime; }
    public void setReleaseTime(Date releaseTime) { this.releaseTime = releaseTime; }
}
