package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkOrder;

/**
 * 订单 Service接口
 *
 * @author ruoyi
 */
public interface IMkOrderService
{
    public List<MkOrder> selectOrderList(MkOrder order);
    public MkOrder selectOrderById(Long orderId);
    public int insertOrder(MkOrder order);
    public int updateOrder(MkOrder order);
    public int deleteOrderByIds(Long[] orderIds);
    /** 订单发货 */
    public int deliverOrder(MkOrder order);
    /** 订单确认 */
    public int confirmOrder(Long orderId);
    /** 订单签收 */
    public int receiveOrder(Long orderId, String receivePerson);
    /** 订单完成 */
    public int completeOrder(Long orderId);
    /** 订单取消 */
    public int cancelOrder(Long orderId, String cancelReason);
}
