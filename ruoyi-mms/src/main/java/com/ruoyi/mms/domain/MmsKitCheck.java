package com.ruoyi.mms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 物料齐套检查 mms_kit_check
 *
 * @author ruoyi
 */
public class MmsKitCheck extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long kitId;

    @Excel(name = "齐套检查单号")
    private String kitNo;

    private Long workOrderId;

    @Excel(name = "工单号")
    private String workOrderNo;

    private Long mpsId;

    @Excel(name = "主计划号")
    private String mpsNo;

    private Long bomId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;

    @Excel(name = "齐套率(%)")
    private BigDecimal kitRate;

    @Excel(name = "是否齐套", readConverterExp = "0=否,1=是")
    private String isComplete;

    @Excel(name = "状态", readConverterExp = "0=待检查,1=已通过,2=缺料")
    private String status;

    private String delFlag;

    private List<MmsKitCheckDetail> detailList;

    public Long getKitId() { return kitId; }
    public void setKitId(Long kitId) { this.kitId = kitId; }
    public String getKitNo() { return kitNo; }
    public void setKitNo(String kitNo) { this.kitNo = kitNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public String getWorkOrderNo() { return workOrderNo; }
    public void setWorkOrderNo(String workOrderNo) { this.workOrderNo = workOrderNo; }
    public Long getMpsId() { return mpsId; }
    public void setMpsId(Long mpsId) { this.mpsId = mpsId; }
    public String getMpsNo() { return mpsNo; }
    public void setMpsNo(String mpsNo) { this.mpsNo = mpsNo; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public Date getCheckTime() { return checkTime; }
    public void setCheckTime(Date checkTime) { this.checkTime = checkTime; }
    public BigDecimal getKitRate() { return kitRate; }
    public void setKitRate(BigDecimal kitRate) { this.kitRate = kitRate; }
    public String getIsComplete() { return isComplete; }
    public void setIsComplete(String isComplete) { this.isComplete = isComplete; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<MmsKitCheckDetail> getDetailList() { return detailList; }
    public void setDetailList(List<MmsKitCheckDetail> detailList) { this.detailList = detailList; }
}
