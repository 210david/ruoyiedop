package com.ruoyi.wms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.wms.domain.WmsOutboundOrder;
import com.ruoyi.wms.domain.WmsOutboundOrderDetail;

public interface WmsOutboundOrderMapper
{
    public List<WmsOutboundOrder> selectOutboundOrderList(WmsOutboundOrder order);
    public WmsOutboundOrder selectOutboundOrderById(Long orderId);
    public int insertOutboundOrder(WmsOutboundOrder order);
    public int updateOutboundOrder(WmsOutboundOrder order);
    public int deleteOutboundOrderByIds(Long[] orderIds);
    public List<WmsOutboundOrderDetail> selectOutboundDetailByOrderId(Long orderId);
    public int insertOutboundDetail(WmsOutboundOrderDetail detail);
    public int updateOutboundDetail(WmsOutboundOrderDetail detail);
    public int deleteOutboundDetailByOrderId(Long orderId);
    public int updateOutboundWaveId(@Param("orderId") Long orderId, @Param("waveId") Long waveId, @Param("status") String status);
    public List<WmsOutboundOrder> selectOutboundOrdersByWaveId(Long waveId);
    public int clearOutboundWaveId(Long waveId);
}
