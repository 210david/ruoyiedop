package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 波次主表 wms_wave
 *
 * @author ruoyi
 */
public class WmsWave extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 波次ID */
    private Long waveId;

    /** 波次号 */
    @Excel(name = "波次号")
    private String waveNo;

    /** 波次名称 */
    @Excel(name = "波次名称")
    private String waveName;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    @Excel(name = "仓库")
    private String warehouseName;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=待拣货,1=拣货中,2=已完成,3=已作废")
    private String status;

    /** 总数量 */
    @Excel(name = "总数量")
    private BigDecimal totalQty;

    /** 包含订单数 */
    @Excel(name = "订单数")
    private Integer orderCount;

    /** 删除标志 */
    private String delFlag;

    public Long getWaveId() { return waveId; }
    public void setWaveId(Long waveId) { this.waveId = waveId; }
    public String getWaveNo() { return waveNo; }
    public void setWaveNo(String waveNo) { this.waveNo = waveNo; }
    public String getWaveName() { return waveName; }
    public void setWaveName(String waveName) { this.waveName = waveName; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
