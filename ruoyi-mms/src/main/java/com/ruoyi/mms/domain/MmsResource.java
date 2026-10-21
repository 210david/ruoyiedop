package com.ruoyi.mms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产能单元 mms_resource
 *
 * @author ruoyi
 */
public class MmsResource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long resourceId;

    @Excel(name = "产能单元编码")
    private String resourceCode;

    @Excel(name = "产能单元名称")
    private String resourceName;

    @Excel(name = "类型", readConverterExp = "0=单设备,1=产线,2=设备组")
    private String resourceType;

    @Excel(name = "所属车间")
    private String workshopName;

    @Excel(name = "所属产线")
    private String lineName;

    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    private String delFlag;

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getResourceCode() { return resourceCode; }
    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
