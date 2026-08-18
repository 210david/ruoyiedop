package com.ruoyi.mms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 产线 mms_line
 */
public class MmsLine extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long lineId;

    @Excel(name = "产线编码")
    private String lineCode;

    @Excel(name = "产线名称")
    private String lineName;

    @Excel(name = "车间ID")
    private Long workshopId;

    @Excel(name = "产线类型", readConverterExp = "0=自动化,1=半自动,2=手工")
    private String lineType;

    @Excel(name = "节拍(秒)")
    private Integer cycleTime;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String delFlag;

    /** 所属车间名称（关联查询冗余字段） */
    @Excel(name = "所属车间")
    private String workshopName;

    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public String getLineCode() { return lineCode; }
    public void setLineCode(String lineCode) { this.lineCode = lineCode; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
    public Long getWorkshopId() { return workshopId; }
    public void setWorkshopId(Long workshopId) { this.workshopId = workshopId; }
    public String getLineType() { return lineType; }
    public void setLineType(String lineType) { this.lineType = lineType; }
    public Integer getCycleTime() { return cycleTime; }
    public void setCycleTime(Integer cycleTime) { this.cycleTime = cycleTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getWorkshopName() { return workshopName; }
    public void setWorkshopName(String workshopName) { this.workshopName = workshopName; }
}
