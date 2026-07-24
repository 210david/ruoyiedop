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
import com.ruoyi.wms.domain.WmsInboundOrder;
import com.ruoyi.wms.domain.WmsInboundOrderDetail;
import com.ruoyi.wms.mapper.WmsInboundOrderMapper;
import com.ruoyi.wms.service.IWmsInboundOrderService;
import com.ruoyi.wms.service.IWmsInventoryService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsInboundOrderServiceImpl implements IWmsInboundOrderService
{
    @Autowired
    private WmsInboundOrderMapper wmsInboundOrderMapper;

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

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
        if (StringUtils.isEmpty(order.getOrderNo()))
        {
            order.setOrderNo(mkNumberRuleService.generateNumber("wms_inbound"));
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
        return mkNumberRuleService.generateNumber("wms_inbound");
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
        if (putawayQty == null || putawayQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("上架数量必须大于0");
        }
        // 汇总同物料同批次的已上架数量
        BigDecimal totalPutaway = BigDecimal.ZERO;
        for (WmsInboundOrderDetail d : details)
        {
            if (d.getMaterialId().equals(target.getMaterialId())
                    && (d.getBatchNo() == null ? target.getBatchNo() == null : d.getBatchNo().equals(target.getBatchNo())))
            {
                totalPutaway = totalPutaway.add(d.getPutawayQty() == null ? BigDecimal.ZERO : d.getPutawayQty());
            }
        }
        if (target.getReceivedQty().compareTo(totalPutaway.add(putawayQty)) < 0)
        {
            throw new ServiceException("上架数量不能超过已收数量");
        }
        String username = SecurityUtils.getUsername();

        // 判断是否需要拆分：已有不同库位时，查找或创建拆分行
        if (target.getLocationId() != null && !target.getLocationId().equals(locationId))
        {
            // 先查找是否已有同物料+同库位的拆分行
            WmsInboundOrderDetail existingSplit = null;
            for (WmsInboundOrderDetail d : details)
            {
                if (!d.getDetailId().equals(target.getDetailId())
                        && d.getMaterialId().equals(target.getMaterialId())
                        && (d.getBatchNo() == null ? target.getBatchNo() == null : d.getBatchNo().equals(target.getBatchNo()))
                        && locationId.equals(d.getLocationId()))
                {
                    existingSplit = d;
                    break;
                }
            }
            if (existingSplit != null)
            {
                existingSplit.setPutawayQty(existingSplit.getPutawayQty().add(putawayQty));
                wmsInboundOrderMapper.updateInboundDetail(existingSplit);
            }
            else
            {
                WmsInboundOrderDetail splitDetail = new WmsInboundOrderDetail();
                splitDetail.setOrderId(orderId);
                splitDetail.setMaterialId(target.getMaterialId());
                splitDetail.setPlanQty(BigDecimal.ZERO);
                splitDetail.setReceivedQty(BigDecimal.ZERO);
                splitDetail.setPutawayQty(putawayQty);
                splitDetail.setBatchNo(target.getBatchNo());
                splitDetail.setProductionDate(target.getProductionDate());
                splitDetail.setExpiryDate(target.getExpiryDate());
                splitDetail.setLocationId(locationId);
                splitDetail.setUnitPrice(target.getUnitPrice());
                splitDetail.setCreateBy(username);
                wmsInboundOrderMapper.insertInboundDetail(splitDetail);
            }
        }
        else
        {
            target.setPutawayQty(target.getPutawayQty().add(putawayQty));
            target.setLocationId(locationId);
            wmsInboundOrderMapper.updateInboundDetail(target);
        }

        // add inventory
        wmsInventoryService.addInventory(target.getMaterialId(), order.getWarehouseId(), locationId,
                target.getBatchNo(), putawayQty, "0", "inbound", order.getOrderNo(),
                username, target.getProductionDate(), target.getExpiryDate());

        // check if all putaway: 按物料+批次分组汇总
        boolean allPutaway = true;
        // 重新查询明细列表（可能新增了拆分行）
        details = wmsInboundOrderMapper.selectInboundDetailByOrderId(orderId);
        // 按 materialId + batchNo 分组，检查每组 putawayQty 合计 >= receivedQty
        java.util.Map<String, BigDecimal> groupPutaway = new java.util.HashMap<>();
        java.util.Map<String, BigDecimal> groupReceived = new java.util.HashMap<>();
        for (WmsInboundOrderDetail d : details)
        {
            String key = d.getMaterialId() + "_" + (d.getBatchNo() == null ? "" : d.getBatchNo());
            groupPutaway.merge(key, d.getPutawayQty() == null ? BigDecimal.ZERO : d.getPutawayQty(), BigDecimal::add);
            groupReceived.merge(key, d.getReceivedQty() == null ? BigDecimal.ZERO : d.getReceivedQty(), BigDecimal::add);
        }
        for (java.util.Map.Entry<String, BigDecimal> entry : groupReceived.entrySet())
        {
            BigDecimal received = entry.getValue();
            BigDecimal putaway = groupPutaway.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            if (received.compareTo(BigDecimal.ZERO) > 0 && putaway.compareTo(received) < 0)
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
