package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsInboundOrder;

public interface IWmsInboundOrderService
{
    public List<WmsInboundOrder> selectInboundOrderList(WmsInboundOrder order);
    public WmsInboundOrder selectInboundOrderById(Long orderId);
    public int insertInboundOrder(WmsInboundOrder order);
    public int updateInboundOrder(WmsInboundOrder order);
    public int deleteInboundOrderByIds(Long[] orderIds);
    public String generateOrderNo();
    public int doReceive(Long orderId, Long detailId, java.math.BigDecimal receivedQty);
    public int doPutaway(Long orderId, Long detailId, Long locationId, java.math.BigDecimal putawayQty);
    public int submitInboundOrder(Long orderId);
}
