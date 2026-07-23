package com.ruoyi.wms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsOutboundOrder;
import com.ruoyi.wms.domain.WmsOutboundOrderDetail;
import com.ruoyi.wms.mapper.WmsOutboundOrderMapper;
import com.ruoyi.wms.service.IWmsOutboundOrderService;
import com.ruoyi.wms.service.IWmsInventoryService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsOutboundOrderServiceImpl implements IWmsOutboundOrderService
{
    @Autowired
    private WmsOutboundOrderMapper wmsOutboundOrderMapper;

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<WmsOutboundOrder> selectOutboundOrderList(WmsOutboundOrder order)
    {
        return wmsOutboundOrderMapper.selectOutboundOrderList(order);
    }

    @Override
    public WmsOutboundOrder selectOutboundOrderById(Long orderId)
    {
        WmsOutboundOrder order = wmsOutboundOrderMapper.selectOutboundOrderById(orderId);
        if (order != null)
        {
            order.setDetailList(wmsOutboundOrderMapper.selectOutboundDetailByOrderId(orderId));
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOutboundOrder(WmsOutboundOrder order)
    {
        if (StringUtils.isEmpty(order.getOrderNo()))
        {
            order.setOrderNo(mkNumberRuleService.generateNumber("wms_outbound"));
        }
        order.setDelFlag("0");
        if (order.getStatus() == null)
        {
            order.setStatus("0");
        }
        BigDecimal totalQty = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (WmsOutboundOrderDetail d : order.getDetailList())
            {
                if (d.getPlanQty() != null)
                {
                    totalQty = totalQty.add(d.getPlanQty());
                }
            }
        }
        order.setTotalQty(totalQty);
        int rows = wmsOutboundOrderMapper.insertOutboundOrder(order);
        if (order.getDetailList() != null)
        {
            for (WmsOutboundOrderDetail d : order.getDetailList())
            {
                d.setOrderId(order.getOrderId());
                d.setPickQty(BigDecimal.ZERO);
                d.setActualQty(BigDecimal.ZERO);
                d.setDelFlag("0");
                wmsOutboundOrderMapper.insertOutboundDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOutboundOrder(WmsOutboundOrder order)
    {
        wmsOutboundOrderMapper.deleteOutboundDetailByOrderId(order.getOrderId());
        BigDecimal totalQty = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (WmsOutboundOrderDetail d : order.getDetailList())
            {
                d.setOrderId(order.getOrderId());
                d.setDelFlag("0");
                if (d.getPickQty() == null) { d.setPickQty(BigDecimal.ZERO); }
                if (d.getActualQty() == null) { d.setActualQty(BigDecimal.ZERO); }
                wmsOutboundOrderMapper.insertOutboundDetail(d);
                if (d.getPlanQty() != null)
                {
                    totalQty = totalQty.add(d.getPlanQty());
                }
            }
        }
        order.setTotalQty(totalQty);
        return wmsOutboundOrderMapper.updateOutboundOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOutboundOrderByIds(Long[] orderIds)
    {
        return wmsOutboundOrderMapper.deleteOutboundOrderByIds(orderIds);
    }

    @Override
    public String generateOrderNo()
    {
        return mkNumberRuleService.generateNumber("wms_outbound");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitOutboundOrder(Long orderId)
    {
        WmsOutboundOrder order = wmsOutboundOrderMapper.selectOutboundOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("出库单不存在");
        }
        if (!"0".equals(order.getStatus()))
        {
            throw new ServiceException("只有草稿状态的出库单才能提交");
        }
        // 校验明细不能为空
        List<WmsOutboundOrderDetail> details = wmsOutboundOrderMapper.selectOutboundDetailByOrderId(orderId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("出库单明细不能为空，无法提交");
        }
        order.setStatus("1");
        return wmsOutboundOrderMapper.updateOutboundOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPick(Long orderId, Long detailId, BigDecimal pickQty)
    {
        WmsOutboundOrder order = wmsOutboundOrderMapper.selectOutboundOrderById(orderId);
        if (order == null || !"1".equals(order.getStatus()))
        {
            throw new ServiceException("出库单状态不正确，无法拣货");
        }
        List<WmsOutboundOrderDetail> details = wmsOutboundOrderMapper.selectOutboundDetailByOrderId(orderId);
        WmsOutboundOrderDetail target = null;
        for (WmsOutboundOrderDetail d : details)
        {
            if (d.getDetailId().equals(detailId))
            {
                target = d;
                break;
            }
        }
        if (target == null)
        {
            throw new ServiceException("出库明细不存在");
        }
        if (target.getPlanQty().compareTo(target.getPickQty().add(pickQty)) < 0)
        {
            throw new ServiceException("拣货数量不能超过计划数量");
        }
        target.setPickQty(target.getPickQty().add(pickQty));
        wmsOutboundOrderMapper.updateOutboundDetail(target);

        // reduce inventory
        String username = SecurityUtils.getUsername();
        wmsInventoryService.reduceInventory(target.getMaterialId(), order.getWarehouseId(),
                target.getLocationId(), target.getBatchNo(), pickQty, "1", "outbound", order.getOrderNo(), username);

        // check if all picked
        boolean allPicked = true;
        for (WmsOutboundOrderDetail d : details)
        {
            if (d.getPickQty().compareTo(d.getPlanQty()) < 0)
            {
                allPicked = false;
                break;
            }
        }
        if (allPicked)
        {
            order.setStatus("3");
            order.setCompleteDate(new Date());
            wmsOutboundOrderMapper.updateOutboundOrder(order);
        }
        return 1;
    }
}
