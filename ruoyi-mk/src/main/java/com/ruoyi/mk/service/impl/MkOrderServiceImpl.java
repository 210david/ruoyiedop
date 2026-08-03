package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkOrder;
import com.ruoyi.mk.domain.MkOrderItem;
import com.ruoyi.mk.domain.MkOrderAuditLog;
import com.ruoyi.mk.mapper.MkOrderMapper;
import com.ruoyi.mk.mapper.MkOrderAuditLogMapper;
import com.ruoyi.mk.service.IMkOrderService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 订单 Service实现
 *
 * 状态流转说明：
 * 0=草稿 → 提交 → 1=待审核
 * 1=待审核 → 审核通过 → 2=已审核 / 审核驳回 → 5=已驳回
 * 5=已驳回 → 修改 → 1=待审核
 * 2=已审核 → 发货(部分) → 3=部分发货 / 发货(全部) → 4=已完成
 * 3=部分发货 → 发货(全部) → 4=已完成
 * 0/1/2/3 → 作废 → 6=已作废
 *
 * @author ruoyi
 */
@Service
public class MkOrderServiceImpl implements IMkOrderService
{
    @Autowired
    private MkOrderMapper mkOrderMapper;

    @Autowired
    private MkOrderAuditLogMapper mkOrderAuditLogMapper;

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
            List<MkOrderAuditLog> auditLogList = mkOrderAuditLogMapper.selectAuditLogByOrderId(orderId);
            order.setAuditLogList(auditLogList);
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
                // 新建订单明细时已发货数量初始化为0
                item.setShippedQty(BigDecimal.ZERO);
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
        // 驳回状态修改后重置为待审核，清空审核信息（审核日志保留）
        MkOrder existing = mkOrderMapper.selectOrderById(order.getOrderId());
        if (existing != null && "5".equals(existing.getOrderStatus()))
        {
            order.setOrderStatus("1");
            order.setAuditBy(null);
            order.setAuditTime(null);
            order.setAuditOpinion(null);
        }
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
    @Transactional(rollbackFor = Exception.class)
    public int deliverOrder(MkOrder order)
    {
        // 校验：只有已审核或部分发货状态的订单才能发货
        MkOrder existing = mkOrderMapper.selectOrderById(order.getOrderId());
        if (existing == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"2".equals(existing.getOrderStatus()) && !"3".equals(existing.getOrderStatus()))
        {
            throw new ServiceException("只有已审核或部分发货状态的订单才能发货");
        }

        // 更新明细已发货数量
        if (order.getItemList() != null)
        {
            // 一次性查询当前订单所有明细
            List<MkOrderItem> existingItems = mkOrderMapper.selectOrderItems(order.getOrderId());
            for (MkOrderItem item : order.getItemList())
            {
                if (item.getItemId() != null && item.getDeliverQty() != null && item.getDeliverQty() > 0)
                {
                    // 查找当前明细
                    MkOrderItem existingItem = null;
                    for (MkOrderItem ei : existingItems)
                    {
                        if (ei.getItemId().equals(item.getItemId()))
                        {
                            existingItem = ei;
                            break;
                        }
                    }
                    if (existingItem == null)
                    {
                        throw new ServiceException("订单明细不存在：" + item.getItemId());
                    }
                    // 累加已发货数量（使用BigDecimal）
                    BigDecimal currentShipped = existingItem.getShippedQty() != null ? existingItem.getShippedQty() : BigDecimal.ZERO;
                    BigDecimal newShippedQty = currentShipped.add(new BigDecimal(item.getDeliverQty()));
                    // 校验不能超过订购数量
                    BigDecimal orderQty = new BigDecimal(existingItem.getQuantity() != null ? existingItem.getQuantity() : 0);
                    if (newShippedQty.compareTo(orderQty) > 0)
                    {
                        throw new ServiceException("商品【" + existingItem.getProductName() + "】发货数量不能超过订购数量");
                    }
                    existingItem.setShippedQty(newShippedQty);
                    mkOrderMapper.updateItemShippedQty(existingItem);
                }
            }
        }

        // 重新查询所有明细，判断是否全部发货完成
        List<MkOrderItem> allItems = mkOrderMapper.selectOrderItems(order.getOrderId());
        boolean allDelivered = true;
        for (MkOrderItem item : allItems)
        {
            BigDecimal shipped = item.getShippedQty() != null ? item.getShippedQty() : BigDecimal.ZERO;
            BigDecimal orderQty = new BigDecimal(item.getQuantity() != null ? item.getQuantity() : 0);
            if (shipped.compareTo(orderQty) < 0)
            {
                allDelivered = false;
            }
        }

        // 判断订单新状态
        String newStatus;
        if (allDelivered)
        {
            // 全部发货完成
            newStatus = "4"; // 已完成
            order.setFinishTime(new java.util.Date());
        }
        else
        {
            // 部分发货
            newStatus = "3"; // 部分发货
        }
        order.setOrderStatus(newStatus);
        order.setUpdateBy(SecurityUtils.getUsername());
        // 如果是首次发货，设置发货时间
        if (existing.getDeliverTime() == null && order.getDeliverTime() == null)
        {
            order.setDeliverTime(new java.util.Date());
        }
        return mkOrderMapper.deliverOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitOrder(Long orderId)
    {
        MkOrder order = mkOrderMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"0".equals(order.getOrderStatus()) && !"5".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有草稿或已驳回状态的订单才能提交");
        }
        MkOrder update = new MkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        return mkOrderMapper.updateOrder(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int voidOrder(Long orderId, String voidReason)
    {
        MkOrder order = mkOrderMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"0".equals(order.getOrderStatus()) && !"1".equals(order.getOrderStatus())
                && !"2".equals(order.getOrderStatus()) && !"3".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有草稿、待审核、已审核或部分发货状态的订单才能作废");
        }
        MkOrder update = new MkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus("6");
        update.setCancelReason(voidReason);
        update.setUpdateBy(SecurityUtils.getUsername());
        return mkOrderMapper.updateOrder(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditOrder(Long orderId, String status, String auditOpinion)
    {
        MkOrder order = mkOrderMapper.selectOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"1".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有待审核状态的订单才能审核");
        }
        // 审核通过变为"已审核(2)"，驳回变为"已驳回(5)"
        String newStatus = "2".equals(status) ? "2" : "5";
        MkOrder update = new MkOrder();
        update.setOrderId(orderId);
        update.setOrderStatus(newStatus);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(new java.util.Date());
        update.setAuditOpinion(auditOpinion);
        update.setUpdateBy(SecurityUtils.getUsername());
        int rows = mkOrderMapper.updateOrder(update);

        // 写入审核日志
        MkOrderAuditLog auditLog = new MkOrderAuditLog();
        auditLog.setOrderId(orderId);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark(auditOpinion);
        // auditAction: 1=通过 2=驳回
        if ("2".equals(status))
        {
            auditLog.setAuditAction("1");
        }
        else
        {
            auditLog.setAuditAction("2");
        }
        mkOrderAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }
}
