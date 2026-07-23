package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkOrder;
import com.ruoyi.mk.domain.MkOrderItem;

public interface MkOrderMapper
{
    public List<MkOrder> selectOrderList(MkOrder order);
    public MkOrder selectOrderById(Long orderId);
    public int insertOrder(MkOrder order);
    public int updateOrder(MkOrder order);
    public int deleteOrderByIds(Long[] orderIds);
    /** 订单明细 */
    public List<MkOrderItem> selectOrderItems(Long orderId);
    public int insertOrderItem(MkOrderItem item);
    public int deleteOrderItemsByOrderId(Long orderId);
    /** 订单发货 */
    public int deliverOrder(MkOrder order);
}
