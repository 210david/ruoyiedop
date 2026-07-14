package com.ruoyi.wms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 出库单头表 wms_outbound_order
 *
 * @author ruoyi
 */
public class WmsOutboundOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 出库单ID */
    private Long orderId;

    /** 出库单号 */
    @Excel(name = "出库单号")
    private String orderNo;

    /** 出库类型 */
    @Excel(name = "出库类型", readConverterExp = "0=销售出库,1=领料出库,2=调拨出库,3=退货出库")
    private String orderType;

    /** 出库仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    @Excel(name = "出库仓库")
    private String warehouseName;

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=待拣货,2=复核中,3=已完成,4=已作废")
    private String status;

    /** 所属波次ID */
    private Long waveId;

    /** 波次号 */
    @Excel(name = "波次号")
    private String waveNo;

    /** 预计出库日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date outboundDate;

    /** 实际完成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeDate;

    /** 总数量 */
    @Excel(name = "总数量")
    private BigDecimal totalQty;

    /** 删除标志 */
    private String delFlag;

    /** 出库单明细列表 */
    private List<WmsOutboundOrderDetail> detailList;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getWaveId() { return waveId; }
    public void setWaveId(Long waveId) { this.waveId = waveId; }
    public String getWaveNo() { return waveNo; }
    public void setWaveNo(String waveNo) { this.waveNo = waveNo; }
    public Date getOutboundDate() { return outboundDate; }
    public void setOutboundDate(Date outboundDate) { this.outboundDate = outboundDate; }
    public Date getCompleteDate() { return completeDate; }
    public void setCompleteDate(Date completeDate) { this.completeDate = completeDate; }
    public BigDecimal getTotalQty() { return totalQty; }
    public void setTotalQty(BigDecimal totalQty) { this.totalQty = totalQty; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<WmsOutboundOrderDetail> getDetailList() { return detailList; }
    public void setDetailList(List<WmsOutboundOrderDetail> detailList) { this.detailList = detailList; }
}
