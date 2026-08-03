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
    /** 订单发货（支持多次发货，自动判断部分发货/已完成） */
    public int deliverOrder(MkOrder order);
    /** 订单提交（草稿/已驳回 → 待审核） */
    public int submitOrder(Long orderId);
    /** 订单作废（草稿/待审核/已审核/部分发货 → 已作废） */
    public int voidOrder(Long orderId, String voidReason);
    /** 订单审核（待审核 → 已审核/已驳回） */
    public int auditOrder(Long orderId, String status, String auditOpinion);
}
