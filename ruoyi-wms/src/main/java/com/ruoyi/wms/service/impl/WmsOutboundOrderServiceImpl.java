package com.ruoyi.wms.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.util.HashMap;
import java.util.Map;


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
import com.ruoyi.wms.domain.WmsMaterial;
import com.ruoyi.food.domain.FoodBatchStock;
import com.ruoyi.food.domain.FoodFefoPlan;
import com.ruoyi.food.service.IFoodBatchStockService;
import com.ruoyi.wms.mapper.WmsOutboundOrderMapper;
import com.ruoyi.wms.mapper.WmsMaterialMapper;
import com.ruoyi.wms.service.IWmsOutboundOrderService;
import com.ruoyi.wms.service.IWmsInventoryService;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.system.utils.MessageHelper;

@Service
public class WmsOutboundOrderServiceImpl implements IWmsOutboundOrderService
{
    @Autowired
    private WmsOutboundOrderMapper wmsOutboundOrderMapper;

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MessageHelper messageHelper;

    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Autowired
    private IFoodBatchStockService foodBatchStockService;

    @Override
    @DataScope(deptAlias = "wms_outbound_order")
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
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(order.getOrderType())) { params.put("outboundType", order.getOrderType()); }
        order.setOrderNo(mkNumberRuleService.generateNumber("wms_outbound", params));
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
        // 保存时即校验批次可发性：不合格/待检/冻结/过期批次不能进入出库单（不必等到提交）
        validateDetailsBatchShippable(order);
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
        // 保存时即校验批次可发性：不合格/待检/冻结/过期批次不能进入出库单（不必等到提交）
        validateDetailsBatchShippable(order);
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
        // 删除前解锁：已提交状态的出库单需要先释放锁定的库存
        String username = SecurityUtils.getUsername();
        for (Long orderId : orderIds)
        {
            WmsOutboundOrder order = wmsOutboundOrderMapper.selectOutboundOrderById(orderId);
            if (order != null && "1".equals(order.getStatus()))
            {
                List<WmsOutboundOrderDetail> details = wmsOutboundOrderMapper.selectOutboundDetailByOrderId(orderId);
                for (WmsOutboundOrderDetail d : details)
                {
                    BigDecimal lockQty = d.getPlanQty().subtract(d.getPickQty() == null ? BigDecimal.ZERO : d.getPickQty());
                    if (lockQty.compareTo(BigDecimal.ZERO) > 0)
                    {
                        wmsInventoryService.unlockInventory(d.getMaterialId(), order.getWarehouseId(),
                                d.getLocationId(), d.getBatchNo(), lockQty, "outbound", order.getOrderNo(), username);
                    }
                }
            }
        }
        int rows = wmsOutboundOrderMapper.deleteOutboundOrderByIds(orderIds);
        // 出库单删除后同步关闭关联消息提醒，避免消息中心残留"孤儿消息"
        for (Long orderId : orderIds)
        {
            messageHelper.markHandled("wms", orderId);
        }
        return rows;
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
        // 效期管理物料：FEFO批次自动分配（三重拦截），人工批次校验并留痕
        String username = SecurityUtils.getUsername();
        List<WmsOutboundOrderDetail> fefoDetails = new java.util.ArrayList<>(details);
        for (WmsOutboundOrderDetail d : fefoDetails)
        {
            allocateBatchIfNeeded(order, d, fefoDetails, username);
        }
        // 换批校验（与FEFO推荐不一致必须填写换批原因）+ 授权换批留痕 + 回写批次分配结果
        for (WmsOutboundOrderDetail d : fefoDetails)
        {
            if (d.getFefoBatchNo() != null && d.getBatchNo() != null && !d.getFefoBatchNo().equals(d.getBatchNo())
                    && StringUtils.isEmpty(d.getChangeReason()))
            {
                throw new ServiceException("物料【" + (d.getMaterialName() != null ? d.getMaterialName() : "") + "】指定批次 "
                        + d.getBatchNo() + " 与FEFO推荐批次 " + d.getFefoBatchNo()
                        + " 不一致，必须填写换批原因（授权换批留痕）");
            }
            if (StringUtils.isNotEmpty(d.getChangeReason()) && d.getBatchNo() != null)
            {
                foodBatchStockService.recordAllocLog(d.getMaterialId(), d.getBatchNo(), "CHANGE_ALLOC",
                        "换批出库：" + d.getChangeReason(), "outbound", orderId, username);
            }
            if (d.getDetailId() != null)
            {
                wmsOutboundOrderMapper.updateOutboundDetail(d);
            }
        }
        // 锁定库存：将各明细的计划数量从可用库存移入锁定库存（含FEFO拆行明细）
        for (WmsOutboundOrderDetail d : fefoDetails)
        {
            try
            {
                wmsInventoryService.lockInventory(d.getMaterialId(), order.getWarehouseId(),
                        d.getLocationId(), d.getBatchNo(), d.getPlanQty(), "outbound", order.getOrderNo(), username);
            }
            catch (ServiceException e)
            {
                // 库存不足，可能是关联的入库单尚未完成上架
                String materialDesc = d.getMaterialName() != null ? d.getMaterialName() : "物料ID:" + d.getMaterialId();
                StringBuilder errorMsg = new StringBuilder();
                errorMsg.append("物料【").append(materialDesc).append("】库存不足");
                // 退货出库单从备注中提取关联入库单号
                if ("3".equals(order.getOrderType()) && order.getRemark() != null)
                {
                    String remark = order.getRemark();
                    int idx = remark.indexOf("关联入库单号：");
                    if (idx >= 0)
                    {
                        String inboundOrderNo = remark.substring(idx + "关联入库单号：".length());
                        // 截取到逗号或字符串末尾
                        int commaIdx = inboundOrderNo.indexOf("，");
                        if (commaIdx >= 0)
                        {
                            inboundOrderNo = inboundOrderNo.substring(0, commaIdx);
                        }
                        errorMsg.append("，关联入库单号：").append(inboundOrderNo).append("，请先在WMS入库管理中完成该入库单的上架操作后重试");
                    }
                    else
                    {
                        errorMsg.append("，关联入库单可能尚未完成上架，请先完成入库操作后重试");
                    }
                }
                else
                {
                    errorMsg.append("，关联入库单可能尚未完成上架，请先完成入库操作后重试");
                }
                throw new ServiceException(errorMsg.toString());
            }
        }
        order.setStatus("1");
        int rows = wmsOutboundOrderMapper.updateOutboundOrder(order);
        // 发送消息：出库单已提交，请安排拣货
        String outboundTypeText = getOutboundTypeText(order.getOrderType());
        messageHelper.sendMessage(
            "出库单" + order.getOrderNo() + "已提交，请安排拣货",
            "出库类型：" + outboundTypeText + "，仓库：" + (order.getWarehouseName() != null ? order.getWarehouseName() : "") + "，总数量：" + order.getTotalQty(),
            "4", "2", "wms", orderId,
            "/wms/outbound/detail?id=" + orderId,
            "wms:outbound:edit",
            "1",  // bizStatus: 待拣货
            "出库作业"  // bizEntryName
        );
        return rows;
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
        if (pickQty == null || pickQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("拣货数量必须大于0");
        }
        if (target.getPlanQty().compareTo(target.getPickQty().add(pickQty)) < 0)
        {
            throw new ServiceException("拣货数量不能超过计划数量");
        }
        target.setPickQty(target.getPickQty().add(pickQty));
        wmsOutboundOrderMapper.updateOutboundDetail(target);

        // 库存已在提交时锁定，拣货时无需再次操作
        String username = SecurityUtils.getUsername();

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
            // release locked inventory (truly deduct)
            for (WmsOutboundOrderDetail d : details)
            {
                wmsInventoryService.releaseLockedInventory(d.getMaterialId(), order.getWarehouseId(),
                        d.getLocationId(), d.getBatchNo(), d.getPickQty(), "outbound", order.getOrderNo(), username);
            }
            order.setStatus("3");
            order.setCompleteDate(new Date());
            wmsOutboundOrderMapper.updateOutboundOrder(order);
            // 发送消息：出库单拣货完成，请复核
            messageHelper.sendMessage(
                "出库单" + order.getOrderNo() + "拣货完成，请复核",
                "",
                "1", "1", "wms", orderId,
                "/wms/outbound/check?id=" + orderId,
                "wms:outbound:check",
                null,
                "扫码复核"  // bizEntryName
            );
            // 拣货完成，标记"请安排拣货"消息为已处理
            messageHelper.markHandled("wms", orderId);
        }
        return 1;
    }

    /**
     * 效期管理物料批次分配：
     * 批次为空 → FEFO自动分配并拆行（最临期优先，三重拦截）；
     * 人工指定 → 三重拦截校验并记录FEFO推荐批次（用于换批留痕判断）
     */
    /**
     * 保存时批次可发性校验：仅效期管理物料的明细参与校验（有批次号的行），
     * 待检/冻结/报废/过期/低于最小发运剩余天数的批次直接拦截，不必等到提交时才发现
     */
    private void validateDetailsBatchShippable(WmsOutboundOrder order)
    {
        if (order.getDetailList() == null)
        {
            return;
        }
        for (WmsOutboundOrderDetail d : order.getDetailList())
        {
            if (d.getMaterialId() == null || StringUtils.isEmpty(d.getBatchNo()))
            {
                continue;
            }
            WmsMaterial material = wmsMaterialMapper.selectMaterialById(d.getMaterialId());
            if (material == null || !"1".equals(material.getIsExpiryManage()))
            {
                continue;
            }
            try
            {
                d.setBatchId(foodBatchStockService.validateBatchShippable(d.getMaterialId(), d.getBatchNo()));
            }
            catch (ServiceException e)
            {
                String desc = StringUtils.isNotEmpty(d.getMaterialName()) ? d.getMaterialName() : d.getMaterialCode();
                throw new ServiceException("保存失败：物料【" + desc + "】" + e.getMessage());
            }
        }
    }

    private void allocateBatchIfNeeded(WmsOutboundOrder order, WmsOutboundOrderDetail d,
                                       List<WmsOutboundOrderDetail> allDetails, String username)
    {
        WmsMaterial material = wmsMaterialMapper.selectMaterialById(d.getMaterialId());
        if (material == null || !"1".equals(material.getIsExpiryManage()))
        {
            return;
        }
        if (StringUtils.isEmpty(d.getBatchNo()))
        {
            FoodFefoPlan plan = foodBatchStockService.planFefo(d.getMaterialId(), order.getWarehouseId(), d.getPlanQty());
            if (!plan.isSuccess())
            {
                throw new ServiceException("物料【" + (d.getMaterialName() != null ? d.getMaterialName() : "")
                        + "】FEFO批次分配失败：" + String.join("；", plan.getSkipped()));
            }
            boolean first = true;
            for (FoodBatchStock alloc : plan.getAllocations())
            {
                if (first)
                {
                    d.setBatchNo(alloc.getBatchCode());
                    d.setBatchId(alloc.getBatchId());
                    d.setLocationId(alloc.getLocationId());
                    d.setFefoBatchNo(alloc.getBatchCode());
                    d.setExpireDate(alloc.getExpireDate());
                    first = false;
                }
                else
                {
                    WmsOutboundOrderDetail split = new WmsOutboundOrderDetail();
                    split.setOrderId(order.getOrderId());
                    split.setMaterialId(d.getMaterialId());
                    split.setMaterialCode(d.getMaterialCode());
                    split.setMaterialName(d.getMaterialName());
                    split.setSpecModel(d.getSpecModel());
                    split.setUnit(d.getUnit());
                    split.setBatchNo(alloc.getBatchCode());
                    split.setBatchId(alloc.getBatchId());
                    split.setFefoBatchNo(alloc.getBatchCode());
                    split.setPlanQty(alloc.getQty());
                    split.setPickQty(BigDecimal.ZERO);
                    split.setActualQty(BigDecimal.ZERO);
                    split.setLocationId(alloc.getLocationId());
                    split.setExpireDate(alloc.getExpireDate());
                    split.setDelFlag("0");
                    split.setCreateBy(username);
                    wmsOutboundOrderMapper.insertOutboundDetail(split);
                    allDetails.add(split);
                }
            }
        }
        else
        {
            d.setBatchId(foodBatchStockService.validateBatchShippable(d.getMaterialId(), d.getBatchNo()));
            // 记录FEFO推荐批次：与人工批次不一致时要求换批原因
            java.util.List<FoodBatchStock> top = foodBatchStockService.listShippableBatches(d.getMaterialId(), order.getWarehouseId());
            d.setFefoBatchNo(top.isEmpty() ? null : top.get(0).getBatchCode());
        }
    }

    /**
     * 出库类型字典转换
     */
    private String getOutboundTypeText(String orderType)
    {
        if (orderType == null) return "-";
        switch (orderType)
        {
            case "0": return "销售出库";
            case "1": return "领料出库";
            case "2": return "调拨出库";
            case "3": return "退货出库";
            default: return orderType;
        }
    }
}
