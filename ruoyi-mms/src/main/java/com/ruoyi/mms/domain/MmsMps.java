package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 主生产计划 mms_mps
 *
 * @author ruoyi
 */
public class MmsMps extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long mpsId;

    @Excel(name = "主计划号")
    private String mpsNo;

    private Long demandId;

    @Excel(name = "关联需求号")
    private String demandNo;

    private Long productId;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "规格型号")
    private String specModel;

    @Excel(name = "单位", readConverterExp = "0=个,1=件,2=箱,3=kg,4=吨,5=米,6=把,7=千米,8=厘米,9=套,10=台,11=条")
    private String unit;

    @Excel(name = "计划产量")
    private BigDecimal planQty;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划期起始", width = 20, dateFormat = "yyyy-MM-dd")
    private Date periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划期结束", width = 20, dateFormat = "yyyy-MM-dd")
    private Date periodEnd;

    private Long resourceId;

    @Excel(name = "产能单元")
    private String resourceName;

    @Excel(name = "优先级", readConverterExp = "0=高,1=中,2=低")
    private String priority;

    @Excel(name = "状态", readConverterExp = "0=草稿,1=已确认,2=已发布,3=已取消")
    private String status;

    private String auditBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    private String auditOpinion;

    private String delFlag;

    /** 审核日志列表（非持久化字段） */
    private List<MmsMpsAuditLog> auditLogList;

    public Long getMpsId() { return mpsId; }
    public void setMpsId(Long mpsId) { this.mpsId = mpsId; }
    public String getMpsNo() { return mpsNo; }
    public void setMpsNo(String mpsNo) { this.mpsNo = mpsNo; }
    public Long getDemandId() { return demandId; }
    public void setDemandId(Long demandId) { this.demandId = demandId; }
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
    public Date getPeriodStart() { return periodStart; }
    public void setPeriodStart(Date periodStart) { this.periodStart = periodStart; }
    public Date getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(Date periodEnd) { this.periodEnd = periodEnd; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
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
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<MmsMpsAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<MmsMpsAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
