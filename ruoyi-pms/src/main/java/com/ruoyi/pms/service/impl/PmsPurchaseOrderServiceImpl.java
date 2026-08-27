package com.ruoyi.pms.service.impl;

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
import com.ruoyi.pms.domain.PmsPurchaseOrder;
import com.ruoyi.pms.domain.PmsPurchaseOrderDetail;
import com.ruoyi.pms.domain.PmsOrderAuditLog;
import com.ruoyi.pms.mapper.PmsPurchaseOrderMapper;
import com.ruoyi.pms.mapper.PmsOrderAuditLogMapper;
import com.ruoyi.pms.service.IPmsPurchaseOrderService;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 采购订单 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsPurchaseOrderServiceImpl implements IPmsPurchaseOrderService
{
    @Autowired
    private PmsPurchaseOrderMapper pmsPurchaseOrderMapper;

    @Autowired
    private PmsOrderAuditLogMapper pmsOrderAuditLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public List<PmsPurchaseOrder> selectPurchaseOrderList(PmsPurchaseOrder order)
    {
        return pmsPurchaseOrderMapper.selectPurchaseOrderList(order);
    }

    @Override
    public PmsPurchaseOrder selectPurchaseOrderById(Long orderId)
    {
        PmsPurchaseOrder order = pmsPurchaseOrderMapper.selectPurchaseOrderById(orderId);
        if (order != null)
        {
            order.setDetailList(pmsPurchaseOrderMapper.selectOrderDetailByOrderId(orderId));
            order.setAuditLogList(pmsOrderAuditLogMapper.selectAuditLogByOrderId(orderId));
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPurchaseOrder(PmsPurchaseOrder order)
    {
        if (StringUtils.isEmpty(order.getOrderNo()))
        {
            Map<String, String> params = new HashMap<>();
        order.setOrderNo(mkNumberRuleService.generateNumber("pms_purchase", params));
        }
        order.setDelFlag("0");
        if (order.getStatus() == null)
        {
            order.setStatus("0");
        }
        if (order.getReceivedAmount() == null)
        {
            order.setReceivedAmount(BigDecimal.ZERO);
        }
        // 计算总数量和总金额
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (PmsPurchaseOrderDetail d : order.getDetailList())
            {
                if (d.getQty() != null)
                {
                    totalQty = totalQty.add(d.getQty());
                }
                if (d.getAmount() != null)
                {
                    totalAmount = totalAmount.add(d.getAmount());
                }
                else if (d.getQty() != null && d.getUnitPrice() != null)
                {
                    d.setAmount(d.getQty().multiply(d.getUnitPrice()));
                    totalAmount = totalAmount.add(d.getAmount());
                }
                d.setReceivedQty(BigDecimal.ZERO);
                d.setDelFlag("0");
            }
        }
        order.setTotalQty(totalQty);
        order.setTotalAmount(totalAmount);
        int rows = pmsPurchaseOrderMapper.insertPurchaseOrder(order);
        // 插入明细
        if (order.getDetailList() != null)
        {
            for (PmsPurchaseOrderDetail d : order.getDetailList())
            {
                d.setOrderId(order.getOrderId());
                d.setDelFlag("0");
                pmsPurchaseOrderMapper.insertOrderDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePurchaseOrder(PmsPurchaseOrder order)
    {
        // 驳回状态修改后重置为待审批，清空审核信息（审核日志保留）
        PmsPurchaseOrder existing = pmsPurchaseOrderMapper.selectPurchaseOrderById(order.getOrderId());
        if (existing != null && "7".equals(existing.getStatus()))
        {
            order.setStatus("1");
            order.setAuditBy(null);
            order.setAuditTime(null);
            order.setAuditOpinion(null);
        }
        // 删除旧明细，重新插入
        pmsPurchaseOrderMapper.deleteOrderDetailByOrderId(order.getOrderId());
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (order.getDetailList() != null)
        {
            for (PmsPurchaseOrderDetail d : order.getDetailList())
            {
                d.setOrderId(order.getOrderId());
                if (d.getQty() != null)
                {
                    totalQty = totalQty.add(d.getQty());
                }
                if (d.getAmount() != null)
                {
                    totalAmount = totalAmount.add(d.getAmount());
                }
                else if (d.getQty() != null && d.getUnitPrice() != null)
                {
                    d.setAmount(d.getQty().multiply(d.getUnitPrice()));
                    totalAmount = totalAmount.add(d.getAmount());
                }
                d.setDelFlag("0");
                pmsPurchaseOrderMapper.insertOrderDetail(d);
            }
        }
        order.setTotalQty(totalQty);
        order.setTotalAmount(totalAmount);
        return pmsPurchaseOrderMapper.updatePurchaseOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePurchaseOrderByIds(Long[] orderIds)
    {
        return pmsPurchaseOrderMapper.deletePurchaseOrderByIds(orderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitPurchaseOrder(Long orderId)
    {
        PmsPurchaseOrder order = pmsPurchaseOrderMapper.selectPurchaseOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!"0".equals(order.getStatus()))
        {
            throw new ServiceException("只有草稿状态的采购订单才能提交审批");
        }
        List<PmsPurchaseOrderDetail> details = pmsPurchaseOrderMapper.selectOrderDetailByOrderId(orderId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("采购订单明细不能为空，无法提交");
        }
        order.setStatus("1");
        int rows = pmsPurchaseOrderMapper.updatePurchaseOrder(order);

        // 发送消息给有审批权限的角色
        String content = "供应商：" + (order.getSupplierName() != null ? order.getSupplierName() : "-")
                + "，金额：￥" + (order.getTotalAmount() != null ? order.getTotalAmount().toPlainString() : "0");
        messageHelper.sendMessage(
            "采购订单" + order.getOrderNo() + "待审批",
            content,
            "3",   // 审批消息
            "2",   // 重要
            "pms",
            orderId,
            "/pms/contract/order?id=" + orderId,
            "pms:order:approve",
"1",   // bizStatus: 待审批
"采购订单"  // bizEntryName
);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditPurchaseOrder(Long orderId, String status, String auditOpinion)
    {
        PmsPurchaseOrder order = pmsPurchaseOrderMapper.selectPurchaseOrderById(orderId);
        if (order == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!"1".equals(order.getStatus()))
        {
            throw new ServiceException("只有待审批状态的采购订单才能审批");
        }
        // 审批通过变为"已下单(3)"，驳回变为"已驳回(7)"
        String newStatus = "2".equals(status) ? "3" : "7";
        order.setStatus(newStatus);
        order.setAuditBy(SecurityUtils.getUsername());
        order.setAuditTime(new Date());
        order.setAuditOpinion(auditOpinion);
        int rows = pmsPurchaseOrderMapper.updatePurchaseOrder(order);

        // 写入审核日志（永久留存）
        PmsOrderAuditLog auditLog = new PmsOrderAuditLog();
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
        pmsOrderAuditLogMapper.insertAuditLog(auditLog);

        // 发送审批结果消息给采购专员
        String resultText = "2".equals(status) ? "已审批通过，请安排下单" : "被驳回，原因：" + (auditOpinion != null ? auditOpinion : "");
        messageHelper.sendMessage(
            "采购订单" + order.getOrderNo() + resultText,
            "审批结果：" + resultText,
            "1",   // 系统通知
            "2".equals(status) ? "1" : "2",  // 通过=普通，驳回=重要
            "pms",
            orderId,
            "/pms/contract/order?id=" + orderId,
"pms:order:edit",
            null,
            "采购订单"  // bizEntryName
);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrderReceivedQty(Long orderId, Long orderDetailId, BigDecimal receivedQty, BigDecimal receivedAmount)
    {
        // 更新采购订单明细的已收数量
        PmsPurchaseOrderDetail detail = pmsPurchaseOrderMapper.selectOrderDetailById(orderDetailId);
        if (detail == null)
        {
            throw new ServiceException("采购订单明细不存在");
        }
        
        // 累加已收数量
        BigDecimal newReceivedQty = detail.getReceivedQty() != null ? detail.getReceivedQty().add(receivedQty) : receivedQty;
        detail.setReceivedQty(newReceivedQty);
        pmsPurchaseOrderMapper.updateOrderDetail(detail);
        
        // 重新计算采购订单的总已收数量和已收金额
        PmsPurchaseOrder order = pmsPurchaseOrderMapper.selectPurchaseOrderById(orderId);
        List<PmsPurchaseOrderDetail> details = pmsPurchaseOrderMapper.selectOrderDetailByOrderId(orderId);
        
        BigDecimal totalReceivedQty = BigDecimal.ZERO;
        BigDecimal totalReceivedAmount = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        
        for (PmsPurchaseOrderDetail d : details)
        {
            if (d.getReceivedQty() != null)
            {
                totalReceivedQty = totalReceivedQty.add(d.getReceivedQty());
                // 计算已收金额（按单价计算）
                if (d.getUnitPrice() != null)
                {
                    totalReceivedAmount = totalReceivedAmount.add(d.getReceivedQty().multiply(d.getUnitPrice()));
                }
            }
            if (d.getQty() != null)
            {
                totalQty = totalQty.add(d.getQty());
            }
        }
        
        order.setReceivedAmount(totalReceivedAmount);
        
        // 根据已收数量判断订单状态
        if (totalReceivedQty.compareTo(BigDecimal.ZERO) > 0)
        {
            if (totalReceivedQty.compareTo(totalQty) >= 0)
            {
                // 全部到货
                order.setStatus("5"); // 已完成
            }
            else
            {
                // 部分到货
                order.setStatus("4"); // 部分到货
            }
        }
        else
        {
            // 已收数量回退为0，回退订单状态为已发货
            order.setStatus("3"); // 已发货
        }
        
        return pmsPurchaseOrderMapper.updatePurchaseOrder(order);
    }
}
