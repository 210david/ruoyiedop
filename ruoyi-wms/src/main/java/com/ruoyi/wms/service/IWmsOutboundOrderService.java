package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsOutboundOrder;

public interface IWmsOutboundOrderService
{
    public List<WmsOutboundOrder> selectOutboundOrderList(WmsOutboundOrder order);
    public WmsOutboundOrder selectOutboundOrderById(Long orderId);
    public int insertOutboundOrder(WmsOutboundOrder order);
    public int updateOutboundOrder(WmsOutboundOrder order);
    public int deleteOutboundOrderByIds(Long[] orderIds);
    public String generateOrderNo();
    public int submitOutboundOrder(Long orderId);
    public int doPick(Long orderId, Long detailId, java.math.BigDecimal pickQty);
}
