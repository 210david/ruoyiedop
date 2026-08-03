package com.ruoyi.pms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.PmsReturn;
import com.ruoyi.pms.domain.PmsReturnDetail;
import com.ruoyi.pms.domain.PmsReturnAuditLog;
import com.ruoyi.pms.mapper.PmsReturnMapper;
import com.ruoyi.pms.mapper.PmsReturnAuditLogMapper;
import com.ruoyi.pms.mapper.PmsReceiveMapper;
import com.ruoyi.pms.domain.PmsReceive;
import com.ruoyi.pms.service.IPmsReturnService;
import com.ruoyi.wms.domain.WmsOutboundOrder;
import com.ruoyi.wms.domain.WmsOutboundOrderDetail;
import com.ruoyi.wms.domain.WmsInboundOrder;
import com.ruoyi.wms.service.IWmsOutboundOrderService;
import com.ruoyi.wms.service.IWmsInboundOrderService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 退货 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsReturnServiceImpl implements IPmsReturnService
{
    @Autowired
    private PmsReturnMapper pmsReturnMapper;

    @Autowired
    private PmsReturnAuditLogMapper pmsReturnAuditLogMapper;

    @Autowired
    private PmsReceiveMapper pmsReceiveMapper;

    @Autowired
    private IWmsOutboundOrderService wmsOutboundOrderService;

    @Autowired
    private IWmsInboundOrderService wmsInboundOrderService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<PmsReturn> selectReturnList(PmsReturn pmsReturn)
    {
        return pmsReturnMapper.selectReturnList(pmsReturn);
    }

    @Override
    public PmsReturn selectReturnById(Long returnId)
    {
        PmsReturn pmsReturn = pmsReturnMapper.selectReturnById(returnId);
        if (pmsReturn != null)
        {
            pmsReturn.setDetailList(pmsReturnMapper.selectReturnDetailByReturnId(returnId));
            pmsReturn.setAuditLogList(pmsReturnAuditLogMapper.selectAuditLogByReturnId(returnId));
            // 关联查询出库单号
            if (pmsReturn.getOutboundOrderId() != null)
            {
                WmsOutboundOrder outboundOrder = wmsOutboundOrderService.selectOutboundOrderById(pmsReturn.getOutboundOrderId());
                if (outboundOrder != null)
                {
                    pmsReturn.setOutboundOrderNo(outboundOrder.getOrderNo());
                }
            }
        }
        return pmsReturn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertReturn(PmsReturn pmsReturn)
    {
        if (StringUtils.isEmpty(pmsReturn.getReturnNo()))
        {
            pmsReturn.setReturnNo(mkNumberRuleService.generateNumber("pms_return"));
        }
        pmsReturn.setDelFlag("0");
        if (pmsReturn.getStatus() == null)
        {
            pmsReturn.setStatus("0");
        }
        // 计算总数量和总金额
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (pmsReturn.getDetailList() != null)
        {
            for (PmsReturnDetail d : pmsReturn.getDetailList())
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
                d.setDelFlag("0");
            }
        }
        pmsReturn.setTotalQty(totalQty);
        pmsReturn.setTotalAmount(totalAmount);
        int rows = pmsReturnMapper.insertReturn(pmsReturn);
        // 插入明细
        if (pmsReturn.getDetailList() != null)
        {
            for (PmsReturnDetail d : pmsReturn.getDetailList())
            {
                d.setReturnId(pmsReturn.getReturnId());
                d.setDelFlag("0");
                pmsReturnMapper.insertReturnDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateReturn(PmsReturn pmsReturn)
    {
        // 驳回状态修改后重置为待审批，清空审核信息（审核日志保留）
        PmsReturn existing = pmsReturnMapper.selectReturnById(pmsReturn.getReturnId());
        if (existing != null && "5".equals(existing.getStatus()))
        {
            pmsReturn.setStatus("1");
            pmsReturn.setAuditBy(null);
            pmsReturn.setAuditTime(null);
            pmsReturn.setAuditOpinion(null);
        }
        // 删除旧明细，重新插入
        pmsReturnMapper.deleteReturnDetailByReturnId(pmsReturn.getReturnId());
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (pmsReturn.getDetailList() != null)
        {
            for (PmsReturnDetail d : pmsReturn.getDetailList())
            {
                d.setReturnId(pmsReturn.getReturnId());
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
                pmsReturnMapper.insertReturnDetail(d);
            }
        }
        pmsReturn.setTotalQty(totalQty);
        pmsReturn.setTotalAmount(totalAmount);
        return pmsReturnMapper.updateReturn(pmsReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReturnByIds(Long[] returnIds)
    {
        return pmsReturnMapper.deleteReturnByIds(returnIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitReturn(Long returnId)
    {
        PmsReturn pmsReturn = pmsReturnMapper.selectReturnById(returnId);
        if (pmsReturn == null)
        {
            throw new ServiceException("退货单不存在");
        }
        if (!("0".equals(pmsReturn.getStatus()) || "5".equals(pmsReturn.getStatus())))
        {
            throw new ServiceException("只有草稿或已驳回状态的退货单才能提交审批");
        }
        pmsReturn.setStatus("1"); // 待审批
        pmsReturn.setUpdateBy(SecurityUtils.getUsername());
        return pmsReturnMapper.updateReturn(pmsReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Object> auditReturn(Long returnId, String status, String auditOpinion)
    {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        PmsReturn pmsReturn = pmsReturnMapper.selectReturnById(returnId);
        if (pmsReturn == null)
        {
            throw new ServiceException("退货单不存在");
        }
        if (!"1".equals(pmsReturn.getStatus()))
        {
            throw new ServiceException("只有待审批状态的退货单才能审批");
        }
        if (!("2".equals(status) || "5".equals(status)))
        {
            throw new ServiceException("审批状态不合法");
        }
        pmsReturn.setStatus(status);
        pmsReturn.setAuditBy(SecurityUtils.getUsername());
        pmsReturn.setAuditTime(new Date());
        pmsReturn.setAuditOpinion(auditOpinion);
        pmsReturn.setUpdateBy(SecurityUtils.getUsername());
        int rows = pmsReturnMapper.updateReturn(pmsReturn);
        result.put("rows", rows);

        // 写入审核日志
        PmsReturnAuditLog auditLog = new PmsReturnAuditLog();
        auditLog.setReturnId(returnId);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark(auditOpinion);
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
            pmsReturnAuditLogMapper.insertAuditLog(auditLog);
        }
        catch (Exception e)
        {
            System.err.println("写入退货审核日志失败：" + e.getMessage());
        }

        // 审批通过后，自动生成退货出库单
        String outboundOrderNo = null;
        String outboundOrderError = null;
        if ("2".equals(status))
        {
            try
            {
                WmsOutboundOrder outboundOrder = createReturnOutboundOrder(pmsReturn);
                if (outboundOrder != null)
                {
                    outboundOrderNo = outboundOrder.getOrderNo();
                    // 将出库单ID写入退货单
                    PmsReturn updateReturn = new PmsReturn();
                    updateReturn.setReturnId(returnId);
                    updateReturn.setOutboundOrderId(outboundOrder.getOrderId());
                    pmsReturnMapper.updateReturn(updateReturn);
                }
            }
            catch (ServiceException e)
            {
                outboundOrderError = e.getMessage();
                System.err.println("退货单 " + pmsReturn.getReturnNo() + " 审批通过后生成出库单失败：" + e.getMessage());
            }
        }
        result.put("outboundOrderNo", outboundOrderNo);
        result.put("outboundOrderError", outboundOrderError);
        return result;
    }

    /**
     * 退货审批通过后，自动生成WMS退货出库单
     * @return 生成的出库单对象，如果未生成则返回null
     */
    private WmsOutboundOrder createReturnOutboundOrder(PmsReturn pmsReturn)
    {
        List<PmsReturnDetail> details = pmsReturnMapper.selectReturnDetailByReturnId(pmsReturn.getReturnId());
        if (details == null || details.isEmpty())
        {
            return null;
        }
        Long warehouseId = null;
        if (pmsReturn.getReceiveId() != null)
        {
            PmsReceive receive = pmsReceiveMapper.selectReceiveById(pmsReturn.getReceiveId());
            if (receive != null)
            {
                warehouseId = receive.getWarehouseId();
            }
        }
        if (warehouseId == null)
        {
            throw new ServiceException("退货单关联的收货单未设置入库仓库，无法自动生成退货出库单");
        }
        for (PmsReturnDetail d : details)
        {
            if (d.getMaterialId() == null)
            {
                throw new ServiceException("退货明细中存在未关联物料的记录，无法生成出库单");
            }
        }
        WmsOutboundOrder outbound = new WmsOutboundOrder();
        outbound.setOrderType("3");
        outbound.setWarehouseId(warehouseId);
        outbound.setStatus("0");
        outbound.setOutboundDate(new Date());
        // 将关联的入库单号写入备注，供出库提交校验时提示
        String remark = "退货单 " + pmsReturn.getReturnNo() + " 审批通过自动生成";
        if (pmsReturn.getReceiveId() != null)
        {
            PmsReceive receive = pmsReceiveMapper.selectReceiveById(pmsReturn.getReceiveId());
            if (receive != null && receive.getInboundOrderId() != null)
            {
                WmsInboundOrder inboundOrder = wmsInboundOrderService.selectInboundOrderById(receive.getInboundOrderId());
                if (inboundOrder != null)
                {
                    remark += "，关联入库单号：" + inboundOrder.getOrderNo();
                }
            }
        }
        outbound.setRemark(remark);
        outbound.setCreateBy(SecurityUtils.getUsername());
        List<WmsOutboundOrderDetail> outboundDetails = new java.util.ArrayList<>();
        for (PmsReturnDetail d : details)
        {
            WmsOutboundOrderDetail od = new WmsOutboundOrderDetail();
            od.setMaterialId(d.getMaterialId());
            od.setMaterialCode(d.getMaterialCode());
            od.setMaterialName(d.getMaterialName());
            od.setSpecModel(d.getSpecModel());
            od.setUnit(d.getUnit());
            od.setPlanQty(d.getQty());
            outboundDetails.add(od);
        }
        outbound.setDetailList(outboundDetails);
        wmsOutboundOrderService.insertOutboundOrder(outbound);
        return outbound;
    }
}
