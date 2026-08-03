package com.ruoyi.pms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.PmsReceive;
import com.ruoyi.pms.domain.PmsReceiveDetail;
import com.ruoyi.pms.domain.PmsReceiveAuditLog;
import com.ruoyi.pms.domain.PmsPurchaseOrder;
import com.ruoyi.pms.domain.PmsPurchaseOrderDetail;
import com.ruoyi.pms.mapper.PmsReceiveMapper;
import com.ruoyi.pms.mapper.PmsReceiveAuditLogMapper;
import com.ruoyi.pms.service.IPmsReceiveService;
import com.ruoyi.pms.service.IPmsPurchaseOrderService;
import com.ruoyi.wms.domain.WmsInboundOrder;
import com.ruoyi.wms.domain.WmsInboundOrderDetail;
import com.ruoyi.wms.service.IWmsInboundOrderService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 收货验收 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsReceiveServiceImpl implements IPmsReceiveService
{
    @Autowired
    private PmsReceiveMapper pmsReceiveMapper;

    @Autowired
    private PmsReceiveAuditLogMapper pmsReceiveAuditLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private IPmsPurchaseOrderService pmsPurchaseOrderService;

    @Autowired
    private IWmsInboundOrderService wmsInboundOrderService;

    @Override
    public List<PmsReceive> selectReceiveList(PmsReceive receive)
    {
        return pmsReceiveMapper.selectReceiveList(receive);
    }

    @Override
    public PmsReceive selectReceiveById(Long receiveId)
    {
        PmsReceive receive = pmsReceiveMapper.selectReceiveById(receiveId);
        // Mapper XML 使用嵌套查询自动加载 detailList 和 auditLogList
        // 以下为兜底：如果嵌套查询未加载成功，手动补充
        if (receive != null)
        {
            if (receive.getDetailList() == null || receive.getDetailList().isEmpty())
            {
                receive.setDetailList(pmsReceiveMapper.selectReceiveDetailByReceiveId(receiveId));
            }
            if (receive.getAuditLogList() == null || receive.getAuditLogList().isEmpty())
            {
                try
                {
                    receive.setAuditLogList(pmsReceiveAuditLogMapper.selectAuditLogByReceiveId(receiveId));
                }
                catch (Exception e)
                {
                    // 审核日志表可能不存在，忽略错误
                    System.err.println("查询审核日志失败（表可能不存在）：" + e.getMessage());
                }
            }
            // 查询入库单号
            if (receive.getInboundOrderId() != null)
            {
                try
                {
                    WmsInboundOrder inboundOrder = wmsInboundOrderService.selectInboundOrderById(receive.getInboundOrderId());
                    if (inboundOrder != null)
                    {
                        receive.setInboundOrderNo(inboundOrder.getOrderNo());
                    }
                }
                catch (Exception e)
                {
                    // 忽略错误，不影响主流程
                    System.err.println("查询入库单号失败：" + e.getMessage());
                }
            }
        }
        return receive;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertReceive(PmsReceive receive)
    {
        // 校验：只有已到货(5)或已完成(6)的订单不允许再收货
        if (receive.getOrderId() != null)
        {
            PmsPurchaseOrder order = pmsPurchaseOrderService.selectPurchaseOrderById(receive.getOrderId());
            if (order != null && ("5".equals(order.getStatus()) || "6".equals(order.getStatus())))
            {
                throw new ServiceException("该采购订单已全部到货，不能重复发起收货");
            }
            // 校验：该采购订单是否存在进行中的收货单（待验收0、待审核5、已驳回6）
            List<Long> inProgressOrderIds = pmsReceiveMapper.selectInProgressReceiveOrderIds();
            if (inProgressOrderIds != null && inProgressOrderIds.contains(receive.getOrderId()))
            {
                throw new ServiceException("该采购订单已有进行中的收货单（待验收/待审核/已驳回），请先完成或作废该收货单后再发起新的收货");
            }
            // 校验：每行明细的应收数量不能大于采购订单明细的未收数量
            if (order != null && order.getDetailList() != null && receive.getDetailList() != null)
            {
                for (PmsReceiveDetail d : receive.getDetailList())
                {
                    // 校验：应收数量必须大于0
                    if (d.getReceiveQty() == null || d.getReceiveQty().compareTo(BigDecimal.ZERO) <= 0)
                    {
                        throw new ServiceException("物料「" + (d.getMaterialName() != null ? d.getMaterialName() : "")
                            + "」的应收数量必须大于0");
                    }
                    if (d.getOrderDetailId() != null && d.getReceiveQty() != null)
                    {
                        for (PmsPurchaseOrderDetail od : order.getDetailList())
                        {
                            if (d.getOrderDetailId().equals(od.getDetailId()))
                            {
                                BigDecimal unreceivedQty = (od.getQty() != null ? od.getQty() : BigDecimal.ZERO)
                                    .subtract(od.getReceivedQty() != null ? od.getReceivedQty() : BigDecimal.ZERO);
                                if (d.getReceiveQty().compareTo(unreceivedQty) > 0)
                                {
                                    throw new ServiceException("物料「" + (d.getMaterialName() != null ? d.getMaterialName() : "")
                                        + "」的应收数量(" + d.getReceiveQty()
                                        + ")不能大于未收数量(" + unreceivedQty + ")");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (StringUtils.isEmpty(receive.getReceiveNo()))
        {
            receive.setReceiveNo(mkNumberRuleService.generateNumber("pms_receive"));
        }
        receive.setDelFlag("0");
        if (receive.getStatus() == null)
        {
            receive.setStatus("0");
        }
        // 计算总数量
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal qualifiedQty = BigDecimal.ZERO;
        BigDecimal unqualifiedQty = BigDecimal.ZERO;
        if (receive.getDetailList() != null)
        {
            for (PmsReceiveDetail d : receive.getDetailList())
            {
                if (d.getReceiveQty() != null)
                {
                    totalQty = totalQty.add(d.getReceiveQty());
                }
                if (d.getQualifiedQty() != null)
                {
                    qualifiedQty = qualifiedQty.add(d.getQualifiedQty());
                }
                if (d.getUnqualifiedQty() != null)
                {
                    unqualifiedQty = unqualifiedQty.add(d.getUnqualifiedQty());
                }
                // 计算明细金额
                if (d.getAmount() == null && d.getQualifiedQty() != null && d.getUnitPrice() != null)
                {
                    d.setAmount(d.getQualifiedQty().multiply(d.getUnitPrice()));
                }
                d.setDelFlag("0");
            }
        }
        receive.setTotalQty(totalQty);
        receive.setQualifiedQty(qualifiedQty);
        receive.setUnqualifiedQty(unqualifiedQty);
        int rows = pmsReceiveMapper.insertReceive(receive);
        // 插入明细
        if (receive.getDetailList() != null)
        {
            for (PmsReceiveDetail d : receive.getDetailList())
            {
                d.setReceiveId(receive.getReceiveId());
                d.setDelFlag("0");
                pmsReceiveMapper.insertReceiveDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateReceive(PmsReceive receive)
    {
        // 驳回状态修改后重置为待验收，清空审核信息（审核日志保留）
        PmsReceive existing = pmsReceiveMapper.selectReceiveById(receive.getReceiveId());
        if (existing != null && "6".equals(existing.getStatus()))
        {
            receive.setStatus("0");
            receive.setAuditBy(null);
            receive.setAuditTime(null);
            receive.setAuditOpinion(null);
        }
        // 校验：每行明细的应收数量不能大于采购订单明细的未收数量
        if (receive.getOrderId() != null)
        {
            PmsPurchaseOrder order = pmsPurchaseOrderService.selectPurchaseOrderById(receive.getOrderId());
            if (order != null && order.getDetailList() != null && receive.getDetailList() != null)
            {
                for (PmsReceiveDetail d : receive.getDetailList())
                {
                    // 校验：应收数量必须大于0
                    if (d.getReceiveQty() == null || d.getReceiveQty().compareTo(BigDecimal.ZERO) <= 0)
                    {
                        throw new ServiceException("物料「" + (d.getMaterialName() != null ? d.getMaterialName() : "")
                            + "」的应收数量必须大于0");
                    }
                    if (d.getOrderDetailId() != null && d.getReceiveQty() != null)
                    {
                        for (PmsPurchaseOrderDetail od : order.getDetailList())
                        {
                            if (d.getOrderDetailId().equals(od.getDetailId()))
                            {
                                BigDecimal unreceivedQty = (od.getQty() != null ? od.getQty() : BigDecimal.ZERO)
                                    .subtract(od.getReceivedQty() != null ? od.getReceivedQty() : BigDecimal.ZERO);
                                if (d.getReceiveQty().compareTo(unreceivedQty) > 0)
                                {
                                    throw new ServiceException("物料「" + (d.getMaterialName() != null ? d.getMaterialName() : "")
                                        + "」的应收数量(" + d.getReceiveQty()
                                        + ")不能大于未收数量(" + unreceivedQty + ")");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        // 删除旧明细，重新插入
        pmsReceiveMapper.deleteReceiveDetailByReceiveId(receive.getReceiveId());
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal qualifiedQty = BigDecimal.ZERO;
        BigDecimal unqualifiedQty = BigDecimal.ZERO;
        if (receive.getDetailList() != null)
        {
            for (PmsReceiveDetail d : receive.getDetailList())
            {
                d.setReceiveId(receive.getReceiveId());
                if (d.getReceiveQty() != null)
                {
                    totalQty = totalQty.add(d.getReceiveQty());
                }
                if (d.getQualifiedQty() != null)
                {
                    qualifiedQty = qualifiedQty.add(d.getQualifiedQty());
                }
                if (d.getUnqualifiedQty() != null)
                {
                    unqualifiedQty = unqualifiedQty.add(d.getUnqualifiedQty());
                }
                if (d.getAmount() == null && d.getQualifiedQty() != null && d.getUnitPrice() != null)
                {
                    d.setAmount(d.getQualifiedQty().multiply(d.getUnitPrice()));
                }
                d.setDelFlag("0");
                pmsReceiveMapper.insertReceiveDetail(d);
            }
        }
        receive.setTotalQty(totalQty);
        receive.setQualifiedQty(qualifiedQty);
        receive.setUnqualifiedQty(unqualifiedQty);
        return pmsReceiveMapper.updateReceive(receive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReceiveByIds(Long[] receiveIds)
    {
        for (Long receiveId : receiveIds)
        {
            PmsReceive receive = pmsReceiveMapper.selectReceiveById(receiveId);
            if (receive == null)
            {
                continue;
            }
            // 只有审核通过的收货单（已验收=2 或 部分验收=1）才回退采购订单已收数量
            if (("1".equals(receive.getStatus()) || "2".equals(receive.getStatus()))
                && receive.getOrderId() != null)
            {
                List<PmsReceiveDetail> details = pmsReceiveMapper.selectReceiveDetailByReceiveId(receiveId);
                if (details != null)
                {
                    for (PmsReceiveDetail detail : details)
                    {
                        if (detail.getOrderDetailId() != null && detail.getQualifiedQty() != null
                            && detail.getQualifiedQty().compareTo(BigDecimal.ZERO) > 0)
                        {
                            // 回退已收数量（传入负数，累加时自动减去）
                            pmsPurchaseOrderService.updateOrderReceivedQty(
                                receive.getOrderId(),
                                detail.getOrderDetailId(),
                                detail.getQualifiedQty().negate(),
                                BigDecimal.ZERO
                            );
                        }
                    }
                }
            }
        }
        return pmsReceiveMapper.deleteReceiveByIds(receiveIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doInspect(PmsReceive receive)
    {
        Long receiveId = receive.getReceiveId();
        PmsReceive existing = pmsReceiveMapper.selectReceiveById(receiveId);
        if (existing == null)
        {
            throw new ServiceException("收货单不存在");
        }
        if (!"0".equals(existing.getStatus()) && !"1".equals(existing.getStatus()))
        {
            throw new ServiceException("当前状态不允许验收");
        }
        List<PmsReceiveDetail> oldDetails = pmsReceiveMapper.selectReceiveDetailByReceiveId(receiveId);
        if (oldDetails == null || oldDetails.isEmpty())
        {
            throw new ServiceException("收货明细不能为空，无法验收");
        }
        // 更新明细的合格数量、不合格数量
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal qualifiedQty = BigDecimal.ZERO;
        BigDecimal unqualifiedQty = BigDecimal.ZERO;
        boolean allInspected = true;
        boolean hasUnqualified = false;
        if (receive.getDetailList() != null)
        {
            for (PmsReceiveDetail d : receive.getDetailList())
            {
                // 如果用户未填写不合格数量，则自动计算默认值 = 应收 - 合格
                if (d.getUnqualifiedQty() == null && d.getReceiveQty() != null && d.getQualifiedQty() != null)
                {
                    BigDecimal diff = d.getReceiveQty().subtract(d.getQualifiedQty());
                    if (diff.compareTo(BigDecimal.ZERO) < 0)
                    {
                        diff = BigDecimal.ZERO;
                    }
                    d.setUnqualifiedQty(diff);
                }
                if (d.getDetailId() != null)
                {
                    // 计算明细金额（按实际收货数量 = 合格 + 不合格）
                    if (d.getQualifiedQty() != null && d.getUnqualifiedQty() != null && d.getUnitPrice() != null)
                    {
                        BigDecimal actualReceived = d.getQualifiedQty().add(d.getUnqualifiedQty());
                        d.setAmount(actualReceived.multiply(d.getUnitPrice()));
                    }
                    pmsReceiveMapper.updateReceiveDetail(d);
                }
                if (d.getReceiveQty() != null)
                {
                    totalQty = totalQty.add(d.getReceiveQty());
                }
                if (d.getQualifiedQty() != null)
                {
                    qualifiedQty = qualifiedQty.add(d.getQualifiedQty());
                }
                if (d.getUnqualifiedQty() != null)
                {
                    unqualifiedQty = unqualifiedQty.add(d.getUnqualifiedQty());
                }
                // 未填写合格数量视为未完成验收
                if (d.getQualifiedQty() == null)
                {
                    allInspected = false;
                }
                // 存在不合格数量视为部分合格
                if (d.getUnqualifiedQty() != null && d.getUnqualifiedQty().compareTo(BigDecimal.ZERO) > 0)
                {
                    hasUnqualified = true;
                }
            }
        }
        else
        {
            // 没有明细数据，检查旧明细
            for (PmsReceiveDetail d : oldDetails)
            {
                if (d.getQualifiedQty() == null)
                {
                    allInspected = false;
                    break;
                }
                if (d.getUnqualifiedQty() != null && d.getUnqualifiedQty().compareTo(BigDecimal.ZERO) > 0)
                {
                    hasUnqualified = true;
                }
            }
            totalQty = existing.getTotalQty();
            qualifiedQty = existing.getQualifiedQty() != null ? existing.getQualifiedQty() : BigDecimal.ZERO;
            unqualifiedQty = existing.getUnqualifiedQty() != null ? existing.getUnqualifiedQty() : BigDecimal.ZERO;
        }
        // 校验：合格数量不能全为0
        if (qualifiedQty.compareTo(BigDecimal.ZERO) == 0)
        {
            throw new ServiceException("合格数量不能全为0，请填写至少一行物料的合格数量");
        }
        existing.setTotalQty(totalQty);
        existing.setQualifiedQty(qualifiedQty);
        existing.setUnqualifiedQty(unqualifiedQty);
        // 设置验收人信息
        existing.setInspectorId(SecurityUtils.getUserId());
        existing.setInspectorName(SecurityUtils.getUsername());
        // 验收提交后进入待审核状态
        existing.setStatus("5");
        return pmsReceiveMapper.updateReceive(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitReceive(Long receiveId)
    {
        PmsReceive receive = pmsReceiveMapper.selectReceiveById(receiveId);
        if (receive == null)
        {
            throw new ServiceException("收货单不存在");
        }
        if (!"0".equals(receive.getStatus()) && !"1".equals(receive.getStatus()))
        {
            throw new ServiceException("只有待验收或部分验收状态的收货单才能提交审核");
        }
        List<PmsReceiveDetail> details = pmsReceiveMapper.selectReceiveDetailByReceiveId(receiveId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("收货明细不能为空，无法提交");
        }
        receive.setStatus("5"); // 待审核
        return pmsReceiveMapper.updateReceive(receive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditReceive(Long receiveId, String status, String auditOpinion)
    {
        PmsReceive receive = pmsReceiveMapper.selectReceiveById(receiveId);
        if (receive == null)
        {
            throw new ServiceException("收货单不存在");
        }
        if (!"5".equals(receive.getStatus()))
        {
            throw new ServiceException("只有待审核状态的收货单才能审核");
        }
        // 审核通过后，根据验收结果确定最终状态：全部合格→已验收(2)，部分合格→部分验收(1)
        if ("2".equals(status))
        {
            List<PmsReceiveDetail> details = pmsReceiveMapper.selectReceiveDetailByReceiveId(receiveId);
            boolean hasUnqualified = false;
            for (PmsReceiveDetail d : details)
            {
                if (d.getUnqualifiedQty() != null && d.getUnqualifiedQty().compareTo(BigDecimal.ZERO) > 0)
                {
                    hasUnqualified = true;
                    break;
                }
            }
            // 兜底：如果明细中没有检查到不合格，再检查表头的汇总不合格数量
            if (!hasUnqualified && receive.getUnqualifiedQty() != null
                && receive.getUnqualifiedQty().compareTo(BigDecimal.ZERO) > 0)
            {
                hasUnqualified = true;
            }
            receive.setStatus(hasUnqualified ? "1" : "2");
        }
        else
        {
            receive.setStatus(status);
        }
        receive.setAuditBy(SecurityUtils.getUsername());
        receive.setAuditTime(new Date());
        receive.setAuditOpinion(auditOpinion);
        int rows = pmsReceiveMapper.updateReceive(receive);

        // 写入审核日志（永久留存）
        PmsReceiveAuditLog auditLog = new PmsReceiveAuditLog();
        auditLog.setReceiveId(receiveId);
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
        try
        {
            pmsReceiveAuditLogMapper.insertAuditLog(auditLog);
        }
        catch (Exception e)
        {
            // 审核日志表可能不存在，忽略错误，不影响审核流程
            System.err.println("写入审核日志失败（表可能不存在）：" + e.getMessage());
        }

        // 审核通过后，更新采购订单的已收数量和状态
        if ("2".equals(status) && receive.getOrderId() != null)
        {
            List<PmsReceiveDetail> details = pmsReceiveMapper.selectReceiveDetailByReceiveId(receiveId);
            for (PmsReceiveDetail detail : details)
            {
                if (detail.getOrderDetailId() != null && detail.getQualifiedQty() != null 
                    && detail.getQualifiedQty().compareTo(BigDecimal.ZERO) > 0)
                {
                    // 计算本次收货金额
                    BigDecimal receivedAmount = BigDecimal.ZERO;
                    if (detail.getUnitPrice() != null)
                    {
                        receivedAmount = detail.getQualifiedQty().multiply(detail.getUnitPrice());
                    }
                    // 更新采购订单已收数量
                    pmsPurchaseOrderService.updateOrderReceivedQty(
                        receive.getOrderId(), 
                        detail.getOrderDetailId(), 
                        detail.getQualifiedQty(),
                        receivedAmount
                    );
                }
            }

            // 自动生成采购入库单（异常不影响审核流程）
            try
            {
                WmsInboundOrder inboundOrder = createReceiveInboundOrder(receive, details);
                if (inboundOrder != null)
                {
                    // 将入库单ID写入收货单
                    PmsReceive updateReceive = new PmsReceive();
                    updateReceive.setReceiveId(receiveId);
                    updateReceive.setInboundOrderId(inboundOrder.getOrderId());
                    pmsReceiveMapper.updateReceive(updateReceive);
                }
            }
            catch (ServiceException e)
            {
                // 捕获异常，记录错误信息但不影响审核流程
                System.err.println("收货单 " + receive.getReceiveNo() + " 审核通过后生成入库单失败：" + e.getMessage());
            }
        }

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Object> auditReceiveWithInboundOrder(Long receiveId, String status, String auditOpinion)
    {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        PmsReceive receive = pmsReceiveMapper.selectReceiveById(receiveId);
        if (receive == null)
        {
            throw new ServiceException("收货单不存在");
        }
        if (!"5".equals(receive.getStatus()))
        {
            throw new ServiceException("只有待审核状态的收货单才能审核");
        }
        // 审核通过后，根据验收结果确定最终状态：全部合格→已验收(2)，部分合格→部分验收(1)
        if ("2".equals(status))
        {
            List<PmsReceiveDetail> details = pmsReceiveMapper.selectReceiveDetailByReceiveId(receiveId);
            boolean hasUnqualified = false;
            for (PmsReceiveDetail d : details)
            {
                if (d.getUnqualifiedQty() != null && d.getUnqualifiedQty().compareTo(BigDecimal.ZERO) > 0)
                {
                    hasUnqualified = true;
                    break;
                }
            }
            // 兜底：如果明细中没有检查到不合格，再检查表头的汇总不合格数量
            if (!hasUnqualified && receive.getUnqualifiedQty() != null
                && receive.getUnqualifiedQty().compareTo(BigDecimal.ZERO) > 0)
            {
                hasUnqualified = true;
            }
            receive.setStatus(hasUnqualified ? "1" : "2");
        }
        else
        {
            receive.setStatus(status);
        }
        receive.setAuditBy(SecurityUtils.getUsername());
        receive.setAuditTime(new Date());
        receive.setAuditOpinion(auditOpinion);
        int rows = pmsReceiveMapper.updateReceive(receive);
        result.put("rows", rows);

        // 写入审核日志（永久留存）
        PmsReceiveAuditLog auditLog = new PmsReceiveAuditLog();
        auditLog.setReceiveId(receiveId);
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
        try
        {
            pmsReceiveAuditLogMapper.insertAuditLog(auditLog);
        }
        catch (Exception e)
        {
            // 审核日志表可能不存在，忽略错误，不影响审核流程
            System.err.println("写入审核日志失败（表可能不存在）：" + e.getMessage());
        }

        String inboundOrderNo = null;
        String inboundOrderError = null;
        // 审核通过后，更新采购订单的已收数量和状态
        if ("2".equals(status) && receive.getOrderId() != null)
        {
            List<PmsReceiveDetail> details = pmsReceiveMapper.selectReceiveDetailByReceiveId(receiveId);
            for (PmsReceiveDetail detail : details)
            {
                if (detail.getOrderDetailId() != null && detail.getQualifiedQty() != null 
                    && detail.getQualifiedQty().compareTo(BigDecimal.ZERO) > 0)
                {
                    // 计算本次收货金额
                    BigDecimal receivedAmount = BigDecimal.ZERO;
                    if (detail.getUnitPrice() != null)
                    {
                        receivedAmount = detail.getQualifiedQty().multiply(detail.getUnitPrice());
                    }
                    // 更新采购订单已收数量
                    pmsPurchaseOrderService.updateOrderReceivedQty(
                        receive.getOrderId(), 
                        detail.getOrderDetailId(), 
                        detail.getQualifiedQty(),
                        receivedAmount
                    );
                }
            }

            // 自动生成采购入库单（异常不影响审核流程）
            try
            {
                WmsInboundOrder inboundOrder = createReceiveInboundOrder(receive, details);
                if (inboundOrder != null)
                {
                    inboundOrderNo = inboundOrder.getOrderNo();
                    // 将入库单ID写入收货单（只更新入库单相关字段，不更新状态）
                    PmsReceive updateReceive = new PmsReceive();
                    updateReceive.setReceiveId(receiveId);
                    updateReceive.setInboundOrderId(inboundOrder.getOrderId());
                    pmsReceiveMapper.updateReceive(updateReceive);
                }
            }
            catch (ServiceException e)
            {
                // 捕获异常，记录错误信息但不影响审核流程
                inboundOrderError = e.getMessage();
                System.err.println("收货单 " + receive.getReceiveNo() + " 审核通过后生成入库单失败：" + e.getMessage());
            }
        }
        result.put("inboundOrderNo", inboundOrderNo);
        result.put("inboundOrderError", inboundOrderError);
        return result;
    }

    /**
     * 验收审核通过后，自动生成WMS采购入库单（合格品+不合格品都入库）
     * 不合格品在物料名称后标记"(不合格品待退)"，便于仓库区分存放
     * @return 生成的入库单对象，如果未生成则返回null
     */
    private WmsInboundOrder createReceiveInboundOrder(PmsReceive receive, List<PmsReceiveDetail> details)
    {
        if (details == null || details.isEmpty())
        {
            return null;
        }
        if (receive.getWarehouseId() == null)
        {
            throw new ServiceException("收货单未设置入库仓库，无法自动生成入库单，请先在收货验收中补充仓库信息");
        }
        // 构建入库明细：合格品和不合格品分别生成行
        List<WmsInboundOrderDetail> inboundDetails = new java.util.ArrayList<>();
        for (PmsReceiveDetail d : details)
        {
            if (d.getMaterialId() == null)
            {
                throw new ServiceException("收货明细中存在未关联物料的记录，无法生成入库单");
            }
            // 合格品行
            if (d.getQualifiedQty() != null && d.getQualifiedQty().compareTo(BigDecimal.ZERO) > 0)
            {
                WmsInboundOrderDetail id = new WmsInboundOrderDetail();
                id.setMaterialId(d.getMaterialId());
                id.setMaterialCode(d.getMaterialCode());
                id.setMaterialName(d.getMaterialName());
                id.setSpecModel(d.getSpecModel());
                id.setUnit(d.getUnit());
                id.setPlanQty(d.getQualifiedQty());
                id.setBatchNo(d.getBatchNo());
                id.setUnitPrice(d.getUnitPrice());
                inboundDetails.add(id);
            }
            // 不合格品行（标记为不合格品待退）
            if (d.getUnqualifiedQty() != null && d.getUnqualifiedQty().compareTo(BigDecimal.ZERO) > 0)
            {
                WmsInboundOrderDetail id = new WmsInboundOrderDetail();
                id.setMaterialId(d.getMaterialId());
                id.setMaterialCode(d.getMaterialCode());
                id.setMaterialName(d.getMaterialName() + "(不合格品待退)");
                id.setSpecModel(d.getSpecModel());
                id.setUnit(d.getUnit());
                id.setPlanQty(d.getUnqualifiedQty());
                id.setBatchNo(receive.getReceiveNo() + "-不合格");
                id.setUnitPrice(d.getUnitPrice());
                inboundDetails.add(id);
            }
        }
        if (inboundDetails.isEmpty())
        {
            return null;
        }
        WmsInboundOrder inbound = new WmsInboundOrder();
        inbound.setOrderType("0"); // 采购入库
        inbound.setSupplierId(receive.getSupplierId());
        inbound.setSupplierName(receive.getSupplierName());
        inbound.setWarehouseId(receive.getWarehouseId());
        inbound.setStatus("0"); // 草稿
        inbound.setInboundDate(new Date());
        inbound.setRemark("收货单 " + receive.getReceiveNo() + " 验收通过自动生成");
        inbound.setCreateBy(SecurityUtils.getUsername());
        inbound.setDetailList(inboundDetails);
        wmsInboundOrderService.insertInboundOrder(inbound);
        return inbound;
    }

    @Override
    public List<Long> selectReceivedOrderIds()
    {
        return pmsReceiveMapper.selectReceivedOrderIds();
    }

    @Override
    public List<Long> selectInProgressReceiveOrderIds()
    {
        return pmsReceiveMapper.selectInProgressReceiveOrderIds();
    }

}
