package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkShipment;
import com.ruoyi.mk.domain.MkShipmentDetail;
import com.ruoyi.mk.domain.MkOrder;
import com.ruoyi.mk.domain.MkOrderItem;
import com.ruoyi.mk.mapper.MkShipmentMapper;
import com.ruoyi.mk.mapper.MkOrderMapper;
import com.ruoyi.mk.service.IMkShipmentService;
import com.ruoyi.mk.service.IMkOrderService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 发货管理 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkShipmentServiceImpl implements IMkShipmentService
{
    @Autowired
    private MkShipmentMapper mkShipmentMapper;

    @Autowired
    private MkOrderMapper mkOrderMapper;

    @Autowired
    private IMkOrderService mkOrderService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkShipment> selectShipmentList(MkShipment shipment)
    {
        return mkShipmentMapper.selectShipmentList(shipment);
    }

    @Override
    public MkShipment selectShipmentById(Long shipmentId)
    {
        MkShipment shipment = mkShipmentMapper.selectShipmentById(shipmentId);
        if (shipment != null)
        {
            List<MkShipmentDetail> detailList = mkShipmentMapper.selectShipmentDetailByShipmentId(shipmentId);
            shipment.setDetailList(detailList);
        }
        return shipment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertShipment(MkShipment shipment)
    {
        // 校验：必须选择订单
        if (shipment.getOrderId() == null)
        {
            throw new ServiceException("请选择发货关联的订单");
        }
        // 校验：订单状态必须是已审核(2)或部分发货(3)
        MkOrder order = mkOrderService.selectOrderById(shipment.getOrderId());
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!"2".equals(order.getOrderStatus()) && !"3".equals(order.getOrderStatus()))
        {
            throw new ServiceException("只有已审核或部分发货状态的订单才能创建发货单");
        }
        // 校验：发货明细不能为空
        if (shipment.getDetailList() == null || shipment.getDetailList().isEmpty())
        {
            throw new ServiceException("发货明细不能为空");
        }
        // 校验：每行发货数量必须大于0且不能超过未发货数量
        if (order.getItemList() != null)
        {
            for (MkShipmentDetail d : shipment.getDetailList())
            {
                if (d.getShipQty() == null || d.getShipQty().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("商品「" + (d.getProductName() != null ? d.getProductName() : "")
                        + "」的发货数量必须大于0");
                }
                if (d.getOrderItemId() != null)
                {
                    for (MkOrderItem oi : order.getItemList())
                    {
                        if (d.getOrderItemId().equals(oi.getItemId()))
                        {
                            BigDecimal orderQty = new BigDecimal(oi.getQuantity() != null ? oi.getQuantity() : 0);
                            BigDecimal shippedQty = oi.getShippedQty() != null ? oi.getShippedQty() : BigDecimal.ZERO;
                            BigDecimal unshippedQty = orderQty.subtract(shippedQty);
                            if (d.getShipQty().compareTo(unshippedQty) > 0)
                            {
                                throw new ServiceException("商品「" + (d.getProductName() != null ? d.getProductName() : "")
                                    + "」的发货数量(" + d.getShipQty()
                                    + ")不能大于未发货数量(" + unshippedQty + ")");
                            }
                            break;
                        }
                    }
                }
            }
        }
        // 生成发货单号
        if (StringUtils.isEmpty(shipment.getShipmentNo()))
        {
            shipment.setShipmentNo(mkNumberRuleService.generateNumber("shipment"));
        }
        shipment.setDelFlag("0");
        if (shipment.getStatus() == null)
        {
            shipment.setStatus("0"); // 待发货
        }
        // 冗余订单信息
        shipment.setOrderNo(order.getOrderNo());
        shipment.setContractId(order.getContractId());
        shipment.setContractNo(order.getContractNo());
        shipment.setCustomerId(order.getCustomerId());
        shipment.setCustomerName(order.getCustomerName());
        // 计算总数量和总金额
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (MkShipmentDetail d : shipment.getDetailList())
        {
            if (d.getShipQty() != null)
            {
                totalQty = totalQty.add(d.getShipQty());
            }
            if (d.getAmount() == null && d.getShipQty() != null && d.getUnitPrice() != null)
            {
                d.setAmount(d.getShipQty().multiply(d.getUnitPrice()));
            }
            if (d.getAmount() != null)
            {
                totalAmount = totalAmount.add(d.getAmount());
            }
            d.setDelFlag("0");
        }
        shipment.setTotalQty(totalQty);
        shipment.setTotalAmount(totalAmount);
        int rows = mkShipmentMapper.insertShipment(shipment);
        // 插入明细
        for (MkShipmentDetail d : shipment.getDetailList())
        {
            d.setShipmentId(shipment.getShipmentId());
            d.setDelFlag("0");
            mkShipmentMapper.insertShipmentDetail(d);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateShipment(MkShipment shipment)
    {
        MkShipment existing = mkShipmentMapper.selectShipmentById(shipment.getShipmentId());
        if (existing == null)
        {
            throw new ServiceException("发货单不存在");
        }
        // 只有待发货(0)状态才能修改
        if (!"0".equals(existing.getStatus()))
        {
            throw new ServiceException("只有待发货状态的发货单才能修改");
        }
        // 校验：发货明细不能为空
        if (shipment.getDetailList() == null || shipment.getDetailList().isEmpty())
        {
            throw new ServiceException("发货明细不能为空");
        }
        // 校验发货数量
        MkOrder order = mkOrderService.selectOrderById(existing.getOrderId());
        if (order != null && order.getItemList() != null)
        {
            for (MkShipmentDetail d : shipment.getDetailList())
            {
                if (d.getShipQty() == null || d.getShipQty().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("商品「" + (d.getProductName() != null ? d.getProductName() : "")
                        + "」的发货数量必须大于0");
                }
                if (d.getOrderItemId() != null)
                {
                    for (MkOrderItem oi : order.getItemList())
                    {
                        if (d.getOrderItemId().equals(oi.getItemId()))
                        {
                            BigDecimal orderQty = new BigDecimal(oi.getQuantity() != null ? oi.getQuantity() : 0);
                            BigDecimal shippedQty = oi.getShippedQty() != null ? oi.getShippedQty() : BigDecimal.ZERO;
                            // 修改时需要排除当前发货单已占用的数量
                            BigDecimal unshippedQty = orderQty.subtract(shippedQty);
                            if (d.getShipQty().compareTo(unshippedQty) > 0)
                            {
                                throw new ServiceException("商品「" + (d.getProductName() != null ? d.getProductName() : "")
                                    + "」的发货数量(" + d.getShipQty()
                                    + ")不能大于未发货数量(" + unshippedQty + ")");
                            }
                            break;
                        }
                    }
                }
            }
        }
        // 删除旧明细，重新插入
        mkShipmentMapper.deleteShipmentDetailByShipmentId(shipment.getShipmentId());
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (MkShipmentDetail d : shipment.getDetailList())
        {
            d.setShipmentId(shipment.getShipmentId());
            if (d.getShipQty() != null)
            {
                totalQty = totalQty.add(d.getShipQty());
            }
            if (d.getAmount() == null && d.getShipQty() != null && d.getUnitPrice() != null)
            {
                d.setAmount(d.getShipQty().multiply(d.getUnitPrice()));
            }
            if (d.getAmount() != null)
            {
                totalAmount = totalAmount.add(d.getAmount());
            }
            d.setDelFlag("0");
            mkShipmentMapper.insertShipmentDetail(d);
        }
        shipment.setTotalQty(totalQty);
        shipment.setTotalAmount(totalAmount);
        return mkShipmentMapper.updateShipment(shipment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteShipmentByIds(Long[] shipmentIds)
    {
        for (Long shipmentId : shipmentIds)
        {
            MkShipment shipment = mkShipmentMapper.selectShipmentById(shipmentId);
            if (shipment == null)
            {
                continue;
            }
            // 只有待发货(0)或已取消(3)状态才能删除
            if (!"0".equals(shipment.getStatus()) && !"3".equals(shipment.getStatus()))
            {
                throw new ServiceException("发货单「" + shipment.getShipmentNo() + "」非待发货/已取消状态，不能删除");
            }
            // 删除明细
            mkShipmentMapper.deleteShipmentDetailByShipmentId(shipmentId);
        }
        return mkShipmentMapper.deleteShipmentByIds(shipmentIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmShipment(MkShipment shipment)
    {
        MkShipment existing = mkShipmentMapper.selectShipmentById(shipment.getShipmentId());
        if (existing == null)
        {
            throw new ServiceException("发货单不存在");
        }
        if (!"0".equals(existing.getStatus()))
        {
            throw new ServiceException("只有待发货状态的发货单才能确认发货");
        }
        // 设置发货信息
        existing.setStatus("1"); // 已发货
        existing.setShipmentDate(shipment.getShipmentDate() != null ? shipment.getShipmentDate() : new Date());
        existing.setLogisticsCompany(shipment.getLogisticsCompany());
        existing.setTrackingNo(shipment.getTrackingNo());
        existing.setShipperId(SecurityUtils.getUserId());
        existing.setShipperName(SecurityUtils.getUsername());
        existing.setUpdateBy(SecurityUtils.getUsername());
        int rows = mkShipmentMapper.confirmShipment(existing);

        // 更新订单明细的已发货数量
        List<MkShipmentDetail> details = mkShipmentMapper.selectShipmentDetailByShipmentId(shipment.getShipmentId());
        if (details != null && existing.getOrderId() != null)
        {
            MkOrder order = mkOrderService.selectOrderById(existing.getOrderId());
            if (order != null && order.getItemList() != null)
            {
                for (MkShipmentDetail d : details)
                {
                    if (d.getOrderItemId() != null)
                    {
                        for (MkOrderItem oi : order.getItemList())
                        {
                            if (d.getOrderItemId().equals(oi.getItemId()))
                            {
                                BigDecimal currentShipped = oi.getShippedQty() != null ? oi.getShippedQty() : BigDecimal.ZERO;
                                BigDecimal newShipped = currentShipped.add(d.getShipQty());
                                mkShipmentMapper.updateOrderItemShippedQty(d.getOrderItemId(), newShipped);
                                break;
                            }
                        }
                    }
                }
                // 检查订单是否全部发货完成
                boolean allShipped = true;
                for (MkOrderItem oi : order.getItemList())
                {
                    BigDecimal orderQty = new BigDecimal(oi.getQuantity() != null ? oi.getQuantity() : 0);
                    BigDecimal shippedQty = oi.getShippedQty() != null ? oi.getShippedQty() : BigDecimal.ZERO;
                    // 加上本次发货的数量
                    for (MkShipmentDetail d : details)
                    {
                        if (d.getOrderItemId() != null && d.getOrderItemId().equals(oi.getItemId()))
                        {
                            shippedQty = shippedQty.add(d.getShipQty());
                            break;
                        }
                    }
                    if (shippedQty.compareTo(orderQty) < 0)
                    {
                        allShipped = false;
                    }
                }
                // 更新订单状态：已审核(2)或部分发货(3) → 部分发货(3)或已完成(4)
                String newOrderStatus = allShipped ? "4" : "3";
                MkOrder updateOrder = new MkOrder();
                updateOrder.setOrderId(order.getOrderId());
                updateOrder.setOrderStatus(newOrderStatus);
                if ("2".equals(order.getOrderStatus()))
                {
                    // 首次发货设置发货时间和物流信息
                    updateOrder.setDeliverTime(new Date());
                    updateOrder.setLogisticsCompany(existing.getLogisticsCompany());
                    updateOrder.setTrackingNo(existing.getTrackingNo());
                }
                updateOrder.setUpdateBy(SecurityUtils.getUsername());
                mkOrderMapper.updateOrder(updateOrder);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int receiveShipment(Long shipmentId)
    {
        MkShipment existing = mkShipmentMapper.selectShipmentById(shipmentId);
        if (existing == null)
        {
            throw new ServiceException("发货单不存在");
        }
        if (!"1".equals(existing.getStatus()))
        {
            throw new ServiceException("只有已发货状态的发货单才能签收");
        }
        existing.setStatus("2"); // 已签收
        existing.setUpdateBy(SecurityUtils.getUsername());
        return mkShipmentMapper.receiveShipment(existing);
    }

    @Override
    public List<Long> selectInProgressShipmentOrderIds()
    {
        return mkShipmentMapper.selectInProgressShipmentOrderIds();
    }
}
