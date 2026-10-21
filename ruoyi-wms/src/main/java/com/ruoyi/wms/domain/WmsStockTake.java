package com.ruoyi.wms.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 盘点单头表 wms_stock_take
 *
 * @author ruoyi
 */
public class WmsStockTake extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 盘点单ID */
    private Long takeId;

    /** 盘点单号 */
    @Excel(name = "盘点单号")
    private String takeNo;

    /** 盘点仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    @Excel(name = "仓库")
    private String warehouseName;

    /** 盘点库区ID */
    private Long areaId;

    /** 库区名称 */
    @Excel(name = "库区")
    private String areaName;

    /** 盘点类型 */
    @Excel(name = "盘点类型", readConverterExp = "0=全盘,1=抽盘,2=循环盘")
    private String takeType;

    /** 抽样比例（百分比，仅抽盘） */
    private Integer sampleRatio;

    /** 循环批次号（仅循环盘） */
    private Integer cycleNo;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=待盘点,1=盘点中,2=待审批,3=已完成,4=已作废")
    private String status;

    /** 计划盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date planDate;

    /** 盘点开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 盘点结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 审批人 */
    @Excel(name = "审批人")
    private String approveBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批意见 */
    private String approveOpinion;

    /** 删除标志 */
    private String delFlag;

    /** 盘点明细列表 */
    private List<WmsStockTakeDetail> detailList;

    /** 审批日志列表 */
    private List<WmsStockTakeApproveLog> approveLogList;

    public Long getTakeId() { return takeId; }
    public void setTakeId(Long takeId) { this.takeId = takeId; }
    public String getTakeNo() { return takeNo; }
    public void setTakeNo(String takeNo) { this.takeNo = takeNo; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Long getAreaId() { return areaId; }
    public void setAreaId(Long areaId) { this.areaId = areaId; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getTakeType() { return takeType; }
    public void setTakeType(String takeType) { this.takeType = takeType; }
    public Integer getSampleRatio() { return sampleRatio; }
    public void setSampleRatio(Integer sampleRatio) { this.sampleRatio = sampleRatio; }
    public Integer getCycleNo() { return cycleNo; }
    public void setCycleNo(Integer cycleNo) { this.cycleNo = cycleNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getPlanDate() { return planDate; }
    public void setPlanDate(Date planDate) { this.planDate = planDate; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getApproveBy() { return approveBy; }
    public void setApproveBy(String approveBy) { this.approveBy = approveBy; }
    public Date getApproveTime() { return approveTime; }
    public void setApproveTime(Date approveTime) { this.approveTime = approveTime; }
    public String getApproveOpinion() { return approveOpinion; }
    public void setApproveOpinion(String approveOpinion) { this.approveOpinion = approveOpinion; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<WmsStockTakeDetail> getDetailList() { return detailList; }
    public void setDetailList(List<WmsStockTakeDetail> detailList) { this.detailList = detailList; }
    public List<WmsStockTakeApproveLog> getApproveLogList() { return approveLogList; }
    public void setApproveLogList(List<WmsStockTakeApproveLog> approveLogList) { this.approveLogList = approveLogList; }
}
