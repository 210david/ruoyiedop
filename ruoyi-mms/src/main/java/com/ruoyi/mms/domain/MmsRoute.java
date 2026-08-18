package com.ruoyi.mms.domain;

import java.util.Date;
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

    /** 生效日期 */
    @Excel(name = "生效日期", width = 30, dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 失效日期 */
    @Excel(name = "失效日期", width = 30, dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    @Excel(name = "状态", readConverterExp = "0=草稿,1=已启用,2=已审核,3=已停用")
    private String status;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 默认车间 */
    @Excel(name = "默认车间")
    private String defaultWorkshop;

    /** 默认产线 */
    @Excel(name = "默认产线")
    private String defaultLine;

    /** 总标准工时(自动汇总) */
    @Excel(name = "总标准工时")
    private java.math.BigDecimal totalStdTime;

    private String delFlag;

    /** 工序明细列表 */
    private List<MmsRouteProcess> processList;

    /** 工序列表查询用（不入库） */
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
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getDefaultWorkshop() { return defaultWorkshop; }
    public void setDefaultWorkshop(String defaultWorkshop) { this.defaultWorkshop = defaultWorkshop; }
    public String getDefaultLine() { return defaultLine; }
    public void setDefaultLine(String defaultLine) { this.defaultLine = defaultLine; }
    public java.math.BigDecimal getTotalStdTime() { return totalStdTime; }
    public void setTotalStdTime(java.math.BigDecimal totalStdTime) { this.totalStdTime = totalStdTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<MmsRouteProcess> getProcessList() { return processList; }
    public void setProcessList(List<MmsRouteProcess> processList) { this.processList = processList; }
    public Integer getProcessCount() { return processCount; }
    public void setProcessCount(Integer processCount) { this.processCount = processCount; }
}
