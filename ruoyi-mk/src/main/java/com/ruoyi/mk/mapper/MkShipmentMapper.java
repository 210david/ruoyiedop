package com.ruoyi.mk.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkShipment;
import com.ruoyi.mk.domain.MkShipmentDetail;

/**
 * 发货管理 Mapper接口
 *
 * @author ruoyi
 */
public interface MkShipmentMapper
{
    public List<MkShipment> selectShipmentList(MkShipment shipment);

    public MkShipment selectShipmentById(Long shipmentId);

    public int insertShipment(MkShipment shipment);

    public int updateShipment(MkShipment shipment);

    public int deleteShipmentByIds(Long[] shipmentIds);

    /** 发货明细 */
    public List<MkShipmentDetail> selectShipmentDetailByShipmentId(Long shipmentId);

    public int insertShipmentDetail(MkShipmentDetail detail);

    public int deleteShipmentDetailByShipmentId(Long shipmentId);

    /** 确认发货（更新状态） */
    public int confirmShipment(MkShipment shipment);

    /** 签收 */
    public int receiveShipment(MkShipment shipment);

    /** 更新订单明细已发货数量 */
    public int updateOrderItemShippedQty(@Param("orderItemId") Long orderItemId, @Param("shippedQty") BigDecimal shippedQty);

    /** 查询存在进行中发货单的订单ID列表 */
    public List<Long> selectInProgressShipmentOrderIds();
}
