package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 排产计划 mms_schedule_task
 *
 * @author ruoyi
 */
public class MmsSchedule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long scheduleId;

    @Excel(name = "排产编号")
    private String scheduleNo;

    private Long workOrderId;

    @Excel(name = "工单编号")
    private String workOrderNo;

    private Long productId;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    private Long resourceId;

    @Excel(name = "产能单元")
    private String resourceName;

    @Excel(name = "工序序号")
    private Integer opSeq;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划开始", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planStart;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "计划结束", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date planEnd;

    @Excel(name = "准备时间(分)")
    private Integer setupMinutes;

    @Excel(name = "排产策略", readConverterExp = "1=正排,2=倒排")
    private String strategy;

    @Excel(name = "状态", readConverterExp = "0=待确认,1=已确认,2=已下达,3=已取消")
    private String status;

    private String delFlag;

    // ========== 工单关联字段（非持久化，甘特图查询时关联工单表填充） ==========

    /** 工单类型（0=生产,1=返工,2=外协） */
    private String orderType;

    /** 规格型号 */
    private String specModel;

    /** 计划数量 */
    private BigDecimal planQty;

    /** 单位 */
    private String unit;

    /** 优先级（0=高,1=中,2=低） */
    private String priority;

    /** BOM编号 */
    private String bomNo;

    /** 工艺路线编号 */
    private String routeNo;

    /** 关联主计划号 */
    private String mpsNo;

    /** 关联需求号 */
    private String demandNo;

    /** 下达人 */
    private String releaseBy;

    /** 下达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public String getScheduleNo() { return scheduleNo; }
    public void setScheduleNo(String scheduleNo) { this.scheduleNo = scheduleNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public Integer getOpSeq() { return opSeq; }
    public void setOpSeq(Integer opSeq) { this.opSeq = opSeq; }
    public Date getPlanStart() { return planStart; }
    public void setPlanStart(Date planStart) { this.planStart = planStart; }
    public Date getPlanEnd() { return planEnd; }
    public void setPlanEnd(Date planEnd) { this.planEnd = planEnd; }
    public Integer getSetupMinutes() { return setupMinutes; }
    public void setSetupMinutes(Integer setupMinutes) { this.setupMinutes = setupMinutes; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public BigDecimal getPlanQty() { return planQty; }
    public void setPlanQty(BigDecimal planQty) { this.planQty = planQty; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getBomNo() { return bomNo; }
    public void setBomNo(String bomNo) { this.bomNo = bomNo; }
    public String getRouteNo() { return routeNo; }
    public void setRouteNo(String routeNo) { this.routeNo = routeNo; }
    public String getMpsNo() { return mpsNo; }
    public void setMpsNo(String mpsNo) { this.mpsNo = mpsNo; }
    public String getDemandNo() { return demandNo; }
    public void setDemandNo(String demandNo) { this.demandNo = demandNo; }
    public String getReleaseBy() { return releaseBy; }
    public void setReleaseBy(String releaseBy) { this.releaseBy = releaseBy; }
    public Date getReleaseTime() { return releaseTime; }
    public void setReleaseTime(Date releaseTime) { this.releaseTime = releaseTime; }
}
