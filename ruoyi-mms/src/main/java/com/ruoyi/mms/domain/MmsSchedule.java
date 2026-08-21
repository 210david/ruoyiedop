package com.ruoyi.mms.domain;

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
}
