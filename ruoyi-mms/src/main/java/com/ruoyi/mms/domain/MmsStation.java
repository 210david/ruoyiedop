package com.ruoyi.mms.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工位 mms_station
 */
public class MmsStation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long stationId;

    @Excel(name = "工位编码")
    private String stationCode;

    @Excel(name = "工位名称")
    private String stationName;

    @Excel(name = "产线ID")
    private Long lineId;

    @Excel(name = "工位类型", readConverterExp = "0=加工,1=检验,2=包装,3=暂存")
    private String stationType;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    private String delFlag;

    /** 所属产线名称（关联查询冗余字段） */
    @Excel(name = "所属产线")
    private String lineName;

    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }
    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }
    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public String getStationType() { return stationType; }
    public void setStationType(String stationType) { this.stationType = stationType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getLineName() { return lineName; }
    public void setLineName(String lineName) { this.lineName = lineName; }
}
