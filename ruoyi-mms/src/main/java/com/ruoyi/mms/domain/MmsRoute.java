package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工艺路线 mms_route
 */
public class MmsRoute extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long routeId;

    @Excel(name = "路线编号")
    private String routeNo;

    @Excel(name = "路线名称")
    private String routeName;

    private Long productId;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "版本号")
    private String version;

    /** 总标准工时(自动汇总) */
    @Excel(name = "总标准工时")
    private BigDecimal totalStdTime;

    @Excel(name = "状态", readConverterExp = "0=草稿,1=待审核,2=已审核,3=已停用,4=已驳回")
    private String status;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.util.Date auditTime;

    /** 审核意见 */
    private String auditRemark;

    private String delFlag;

    /** 工序明细列表 */
    private List<MmsRouteProcess> processList;

    /** 审核日志列表（不入库，查询时关联） */
    private List<MmsRouteAuditLog> auditLogList;

    /** 工序数（不入库，查询时统计） */
    private transient Integer processCount;

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteNo() { return routeNo; }
    public void setRouteNo(String routeNo) { this.routeNo = routeNo; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public BigDecimal getTotalStdTime() { return totalStdTime; }
    public void setTotalStdTime(BigDecimal totalStdTime) { this.totalStdTime = totalStdTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public java.util.Date getAuditTime() { return auditTime; }
    public void setAuditTime(java.util.Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<MmsRouteProcess> getProcessList() { return processList; }
    public void setProcessList(List<MmsRouteProcess> processList) { this.processList = processList; }
    public Integer getProcessCount() { return processCount; }
    public void setProcessCount(Integer processCount) { this.processCount = processCount; }
    public List<MmsRouteAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<MmsRouteAuditLog> auditLogList) { this.auditLogList = auditLogList; }
}
