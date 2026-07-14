package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.WmsInboundOrder;
import com.ruoyi.wms.domain.WmsInboundOrderDetail;

public interface WmsInboundOrderMapper
{
    public List<WmsInboundOrder> selectInboundOrderList(WmsInboundOrder order);
    public WmsInboundOrder selectInboundOrderById(Long orderId);
    public int insertInboundOrder(WmsInboundOrder order);
    public int updateInboundOrder(WmsInboundOrder order);
    public int deleteInboundOrderByIds(Long[] orderIds);
    public List<WmsInboundOrderDetail> selectInboundDetailByOrderId(Long orderId);
    public int insertInboundDetail(WmsInboundOrderDetail detail);
    public int updateInboundDetail(WmsInboundOrderDetail detail);
    public int deleteInboundDetailByOrderId(Long orderId);
}
