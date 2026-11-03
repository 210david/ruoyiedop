package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 经销商分销流向登记 mk_dealer_sell_flow
 *
 * @author ruoyi
 */
public class MkDealerSellFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 流向ID */
    private Long flowId;

    /** 经销商客户ID */
    @Excel(name = "经销商ID")
    private Long dealerId;

    /** 流向日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "流向日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date flowDate;

    /** 物料ID */
    private Long materialId;

    /** 批次号（启用批次管理的物料必填，其他可空） */
    @Excel(name = "批次号")
    private String batchNo;

    /** 数量 */
    @Excel(name = "数量")
    private BigDecimal quantity;

    /** 下游客户类型（1企业 2经销商 3个人 4终端不详） */
    @Excel(name = "下游客户类型", dictType = "mk_downstream_type")
    private String downstreamType;

    /** 下游客户名称 */
    @Excel(name = "下游客户名称")
    private String downstreamName;

    /** 来源发货单ID（系统生成时记录，唯一键防重） */
    private Long shipmentId;

    /** 经销商名称（关联字段） */
    @Excel(name = "经销商")
    private String dealerName;

    /** 物料名称（关联字段） */
    @Excel(name = "物料")
    private String materialName;

    /** 发货单号（关联字段） */
    private String shipmentNo;

    /** 规格型号（查询展示，选择物料带出） */
    private String specModel;

    /** 单位（查询展示，字典 wms_unit） */
    private String unit;

    /** 是否批次管理（查询展示，0否 1是，驱动批次号必填） */
    private String isBatchManage;

    public Long getFlowId() { return flowId; }
    public void setFlowId(Long flowId) { this.flowId = flowId; }
    public Long getDealerId() { return dealerId; }
    public void setDealerId(Long dealerId) { this.dealerId = dealerId; }
    public Date getFlowDate() { return flowDate; }
    public void setFlowDate(Date flowDate) { this.flowDate = flowDate; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getDownstreamType() { return downstreamType; }
    public void setDownstreamType(String downstreamType) { this.downstreamType = downstreamType; }
    public String getDownstreamName() { return downstreamName; }
    public void setDownstreamName(String downstreamName) { this.downstreamName = downstreamName; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public String getDealerName() { return dealerName; }
    public void setDealerName(String dealerName) { this.dealerName = dealerName; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getShipmentNo() { return shipmentNo; }
    public void setShipmentNo(String shipmentNo) { this.shipmentNo = shipmentNo; }
    public String getSpecModel() { return specModel; }
    public void setSpecModel(String specModel) { this.specModel = specModel; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getIsBatchManage() { return isBatchManage; }
    public void setIsBatchManage(String isBatchManage) { this.isBatchManage = isBatchManage; }
}
