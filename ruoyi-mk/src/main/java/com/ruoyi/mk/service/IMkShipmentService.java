package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkShipment;

/**
 * 发货管理 Service接口
 *
 * @author ruoyi
 */
public interface IMkShipmentService
{
    public List<MkShipment> selectShipmentList(MkShipment shipment);

    public MkShipment selectShipmentById(Long shipmentId);

    public int insertShipment(MkShipment shipment);

    public int updateShipment(MkShipment shipment);

    public int deleteShipmentByIds(Long[] shipmentIds);

    /** 确认发货 */
    public int confirmShipment(MkShipment shipment);

    /** 签收 */
    public int receiveShipment(Long shipmentId);

    /** 查询存在进行中发货单的订单ID列表 */
    public List<Long> selectInProgressShipmentOrderIds();
}
