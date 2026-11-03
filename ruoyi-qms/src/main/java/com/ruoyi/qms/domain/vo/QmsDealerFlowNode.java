package com.ruoyi.qms.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 经销商分销流向追溯节点 VO
 *
 * 正向追溯时挂在批次节点下，展示「发货单 → 经销商 → 下游客户」分销去向明细，
 * 补齐成品批次追溯链的最后一环。
 *
 * @author ruoyi
 */
public class QmsDealerFlowNode
{
    /** 流向日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date flowDate;

    /** 批次号 */
    private String batchNo;

    /** 物料名称 */
    private String materialName;

    /** 经销商名称 */
    private String dealerName;

    /** 下游客户类型（1企业 2经销商 3个人 4终端不详） */
    private String downstreamType;

    /** 下游客户名称 */
    private String downstreamName;

    /** 数量 */
    private BigDecimal quantity;

    /** 来源发货单号 */
    private String shipmentNo;

    public Date getFlowDate() { return flowDate; }
    public void setFlowDate(Date flowDate) { this.flowDate = flowDate; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getDealerName() { return dealerName; }
    public void setDealerName(String dealerName) { this.dealerName = dealerName; }
    public String getDownstreamType() { return downstreamType; }
    public void setDownstreamType(String downstreamType) { this.downstreamType = downstreamType; }
    public String getDownstreamName() { return downstreamName; }
    public void setDownstreamName(String downstreamName) { this.downstreamName = downstreamName; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getShipmentNo() { return shipmentNo; }
    public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }
}
