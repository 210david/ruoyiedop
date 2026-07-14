package com.ruoyi.wms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.wms.domain.WmsInboundOrder;
import com.ruoyi.wms.domain.WmsInboundOrderDetail;
import com.ruoyi.wms.mapper.WmsInboundOrderMapper;
import com.ruoyi.wms.service.IWmsInboundOrderService;
import com.ruoyi.wms.service.IWmsInventoryService;

@Service
public class WmsInboundOrderServiceImpl implements IWmsInboundOrderService
{
    @Autowired
    private WmsInboundOrderMapper wmsInboundOrderMapper;

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @Override
    public List<WmsInboundOrder> selectInboundOrderList(WmsInboundOrder order)
    {
        return wmsInboundOrderMapper.selectInboundOrderList(order);
    }

    @Override
    public WmsInboundOrder selectInboundOrderById(Long orderId)
    {
        WmsInboundOrder order = wmsInboundOrderMapper.selectInboundOrderById(orderId);
        if (order != null)
        {
            order.setDetailList(wmsInboundOrderMapper.selectInboundDetailByOrderId(orderId));
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInboundOrder(WmsInboundOrder order)
    {
        if (order.getOrderNo() == null || order.getOrderNo().isEmpty())
        {
            order.setOrderNo(generateOrderNo());
        }
        order.setDelFlag("0");
        if (order.getStatus() == null)
        {
            order.setStatus("0");
        }
        // calculate total
        BigDecimal totalQty = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (WmsInboundOrderDetail d : order.getDetailList())
            {
                if (d.getPlanQty() != null)
                {
                    totalQty = totalQty.add(d.getPlanQty());
                }
            }
        }
        order.setTotalQty(totalQty);
        int rows = wmsInboundOrderMapper.insertInboundOrder(order);
        // insert details
        if (order.getDetailList() != null)
        {
            for (WmsInboundOrderDetail d : order.getDetailList())
            {
                d.setOrderId(order.getOrderId());
                d.setReceivedQty(BigDecimal.ZERO);
                d.setPutawayQty(BigDecimal.ZERO);
                d.setDelFlag("0");
                wmsInboundOrderMapper.insertInboundDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInboundOrder(WmsInboundOrder order)
    {
        // delete old details and re-insert
        wmsInboundOrderMapper.deleteInboundDetailByOrderId(order.getOrderId());
        BigDecimal totalQty = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (WmsInboundOrderDetail d : order.getDetailList())
            {
                d.setOrderId(order.getOrderId());
                d.setDelFlag("0");
                wmsInboundOrderMapper.insertInboundDetail(d);
                if (d.getPlanQty() != null)
                {
                    totalQty = totalQty.add(d.getPlanQty());
                }
            }
        }
        order.setTotalQty(totalQty);
        return wmsInboundOrderMapper.updateInboundOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInboundOrderByIds(Long[] orderIds)
    {
        return wmsInboundOrderMapper.deleteInboundOrderByIds(orderIds);
    }

    @Override
    public String generateOrderNo()
    {
        return "IN" + System.currentTimeMillis();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doReceive(Long orderId, Long detailId, BigDecimal receivedQty)
    {
        WmsInboundOrder order = wmsInboundOrderMapper.selectInboundOrderById(orderId);
        if (order == null || !"1".equals(order.getStatus()))
        {
            throw new ServiceException("入库单状态不正确，无法收货");
        }
        List<WmsInboundOrderDetail> details = wmsInboundOrderMapper.selectInboundDetailByOrderId(orderId);
        WmsInboundOrderDetail target = null;
        for (WmsInboundOrderDetail d : details)
        {
            if (d.getDetailId().equals(detailId))
            {
                target = d;
                break;
            }
        }
        if (target == null)
        {
            throw new ServiceException("入库明细不存在");
        }
        target.setReceivedQty(target.getReceivedQty().add(receivedQty));
        wmsInboundOrderMapper.updateInboundDetail(target);

        // check if all received
        boolean allReceived = true;
        for (WmsInboundOrderDetail d : details)
        {
            if (d.getReceivedQty().compareTo(d.getPlanQty()) < 0)
            {
                allReceived = false;
                break;
            }
        }
        if (allReceived)
        {
            order.setStatus("2");
            wmsInboundOrderMapper.updateInboundOrder(order);
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitInboundOrder(Long orderId)
    {
        WmsInboundOrder order = wmsInboundOrderMapper.selectInboundOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!"0".equals(order.getStatus()))
        {
            throw new ServiceException("只有草稿状态的入库单才能提交");
        }
        // 校验明细不能为空
        List<WmsInboundOrderDetail> details = wmsInboundOrderMapper.selectInboundDetailByOrderId(orderId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("入库单明细不能为空，无法提交");
        }
        order.setStatus("1");
        return wmsInboundOrderMapper.updateInboundOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPutaway(Long orderId, Long detailId, Long locationId, BigDecimal putawayQty)
    {
        WmsInboundOrder order = wmsInboundOrderMapper.selectInboundOrderById(orderId);
        if (order == null || !"2".equals(order.getStatus()))
        {
            throw new ServiceException("入库单状态不正确，无法上架");
        }
        List<WmsInboundOrderDetail> details = wmsInboundOrderMapper.selectInboundDetailByOrderId(orderId);
        WmsInboundOrderDetail target = null;
        for (WmsInboundOrderDetail d : details)
        {
            if (d.getDetailId().equals(detailId))
            {
                target = d;
                break;
            }
        }
        if (target == null)
        {
            throw new ServiceException("入库明细不存在");
        }
        if (target.getReceivedQty().compareTo(target.getPutawayQty().add(putawayQty)) < 0)
        {
            throw new ServiceException("上架数量不能超过已收数量");
        }
        target.setPutawayQty(target.getPutawayQty().add(putawayQty));
        target.setLocationId(locationId);
        wmsInboundOrderMapper.updateInboundDetail(target);

        // add inventory
        String username = SecurityUtils.getUsername();
        wmsInventoryService.addInventory(target.getMaterialId(), order.getWarehouseId(), locationId,
                target.getBatchNo(), putawayQty, "0", "inbound", order.getOrderNo(), 
                username, target.getProductionDate(), target.getExpiryDate());

        // check if all putaway
        boolean allPutaway = true;
        for (WmsInboundOrderDetail d : details)
        {
            if (d.getPutawayQty().compareTo(d.getReceivedQty()) < 0)
            {
                allPutaway = false;
                break;
            }
        }
        if (allPutaway)
        {
            order.setStatus("3");
            order.setCompleteDate(new Date());
            wmsInboundOrderMapper.updateInboundOrder(order);
        }
        return 1;
    }
}
