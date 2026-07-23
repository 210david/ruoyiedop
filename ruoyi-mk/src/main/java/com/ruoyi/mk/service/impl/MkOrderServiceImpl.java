package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkOrder;
import com.ruoyi.mk.domain.MkOrderItem;
import com.ruoyi.mk.mapper.MkOrderMapper;
import com.ruoyi.mk.service.IMkOrderService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 订单 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkOrderServiceImpl implements IMkOrderService
{
    @Autowired
    private MkOrderMapper mkOrderMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkOrder> selectOrderList(MkOrder order)
    {
        return mkOrderMapper.selectOrderList(order);
    }

    @Override
    public MkOrder selectOrderById(Long orderId)
    {
        MkOrder order = mkOrderMapper.selectOrderById(orderId);
        if (order != null)
        {
            List<MkOrderItem> itemList = mkOrderMapper.selectOrderItems(orderId);
            order.setItemList(itemList);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOrder(MkOrder order)
    {
        order.setDelFlag("0");
        if (order.getOrderStatus() == null)
        {
            order.setOrderStatus("0");
        }
        // 自动生成订单编号
        if (StringUtils.isEmpty(order.getOrderNo()))
        {
            order.setOrderNo(mkNumberRuleService.generateNumber("order"));
        }
        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItemList() != null)
        {
            for (MkOrderItem item : order.getItemList())
            {
                if (item.getSubtotal() != null)
                {
                    totalAmount = totalAmount.add(item.getSubtotal());
                }
                else if (item.getQuantity() != null && item.getUnitPrice() != null)
                {
                    item.setSubtotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                    totalAmount = totalAmount.add(item.getSubtotal());
                }
            }
        }
        if (order.getOrderAmount() == null)
        {
            order.setOrderAmount(totalAmount);
        }
        int rows = mkOrderMapper.insertOrder(order);
        // 插入订单明细
        if (order.getItemList() != null)
        {
            for (MkOrderItem item : order.getItemList())
            {
                item.setOrderId(order.getOrderId());
                mkOrderMapper.insertOrderItem(item);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrder(MkOrder order)
    {
        // 删除旧明细，重新插入
        mkOrderMapper.deleteOrderItemsByOrderId(order.getOrderId());
        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getItemList() != null)
        {
            for (MkOrderItem item : order.getItemList())
            {
                item.setOrderId(order.getOrderId());
                if (item.getSubtotal() != null)
                {
                    totalAmount = totalAmount.add(item.getSubtotal());
                }
                else if (item.getQuantity() != null && item.getUnitPrice() != null)
                {
                    item.setSubtotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
                    totalAmount = totalAmount.add(item.getSubtotal());
                }
                mkOrderMapper.insertOrderItem(item);
            }
        }
        if (order.getOrderAmount() == null)
        {
            order.setOrderAmount(totalAmount);
        }
        return mkOrderMapper.updateOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOrderByIds(Long[] orderIds)
    {
        // 删除关联订单明细
        for (Long orderId : orderIds)
        {
            mkOrderMapper.deleteOrderItemsByOrderId(orderId);
        }
        return mkOrderMapper.deleteOrderByIds(orderIds);
    }

    @Override
    public int deliverOrder(MkOrder order)
    {
        // 发货时设置订单状态为已发货
        order.setOrderStatus("2");
        return mkOrderMapper.deliverOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmOrder(Long orderId)
    {
        MkOrder order = mkOrderMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"0".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有待确认状态的订单才能确认");
        }
        MkOrder update = new MkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("1");
        update.setConfirmTime(new java.util.Date());
        update.setUpdateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkOrderMapper.updateOrder(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int receiveOrder(Long orderId, String receivePerson)
    {
        MkOrder order = mkOrderMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"2".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有已发货状态的订单才能签收");
        }
        MkOrder update = new MkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("3");
        update.setReceiveTime(new java.util.Date());
        update.setReceivePerson(receivePerson != null ? receivePerson : com.ruoyi.common.utils.SecurityUtils.getUsername());
        update.setUpdateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkOrderMapper.updateOrder(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeOrder(Long orderId)
    {
        MkOrder order = mkOrderMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"3".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有已签收状态的订单才能完成");
        }
        MkOrder update = new MkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("4");
        update.setFinishTime(new java.util.Date());
        update.setUpdateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkOrderMapper.updateOrder(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelOrder(Long orderId, String cancelReason)
    {
        MkOrder order = mkOrderMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"0".equals(order.getOrderStatus()) && !"1".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有待确认或已确认状态的订单才能取消");
        }
        MkOrder update = new MkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("5");
        update.setCancelReason(cancelReason);
        update.setUpdateBy(com.ruoyi.common.utils.SecurityUtils.getUsername());
        return mkOrderMapper.updateOrder(update);
    }
}
