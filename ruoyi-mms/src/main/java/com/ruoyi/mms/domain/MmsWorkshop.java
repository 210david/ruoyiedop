package com.ruoyi.mms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 车间 mms_workshop
 */
public class MmsWorkshop extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long workshopId;

    @Excel(name = "车间编码")
    private String workshopCode;

    @Excel(name = "车间名称")
    private String workshopName;

    @Excel(name = "工厂ID")
    private Long factoryId;

    @Excel(name = "负责人ID")
    private Long leaderUserId;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String delFlag;

    /** 所属工厂名称（关联查询冗余字段） */
    @Excel(name = "所属工厂")
    private String factoryName;

    public Long getWorkshopId() { return workshopId; }
    public void setWorkshopId(Long workshopId) { this.workshopId = workshopId; }
    public String getWorkshopCode() { return workshopCode; }
    public void setWorkshopCode(String workshopCode) { this.workshopCode = workshopCode; }
    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public Long getLeaderUserId() { return leaderUserId; }
    public void setLeaderUserId(Long leaderUserId) { this.leaderUserId = leaderUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getFactoryName() { return factoryName; }
    public void setFactoryName(String factoryName) { this.factoryName = factoryName; }
}
