package com.ruoyi.mk.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkOrder;
import com.ruoyi.mk.domain.MkOrderReturn;
import com.ruoyi.mk.domain.MkOrderReturnApproveLog;
import com.ruoyi.mk.domain.MkOrderReturnItem;
import com.ruoyi.mk.mapper.MkOrderMapper;
import com.ruoyi.mk.mapper.MkOrderReturnApproveLogMapper;
import com.ruoyi.mk.mapper.MkOrderReturnMapper;
import com.ruoyi.mk.service.IMkOrderReturnService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 订单退货 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkOrderReturnServiceImpl implements IMkOrderReturnService
{
    @Autowired
    private MkOrderReturnMapper mkOrderReturnMapper;

    @Autowired
    private MkOrderMapper mkOrderMapper;

    @Autowired
    private MkOrderReturnApproveLogMapper mkOrderReturnApproveLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkOrderReturn> selectReturnList(MkOrderReturn orderReturn)
    {
        return mkOrderReturnMapper.selectReturnList(orderReturn);
    }

    @Override
    public MkOrderReturn selectReturnById(Long returnId)
    {
        MkOrderReturn orderReturn = mkOrderReturnMapper.selectReturnById(returnId);
        if (orderReturn != null)
        {
            List<MkOrderReturnItem> detailList = mkOrderReturnMapper.selectReturnItemsByReturnId(returnId);
            orderReturn.setDetailList(detailList);
            List<MkOrderReturnApproveLog> approveLogList = mkOrderReturnApproveLogMapper.selectApproveLogByReturnId(returnId);
            orderReturn.setApproveLogList(approveLogList);
        }
        return orderReturn;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertReturn(MkOrderReturn orderReturn)
    {
        if (orderReturn.getReturnStatus() == null)
        {
            orderReturn.setReturnStatus("4"); // 草稿
        }
        // 自动生成退货编号
        if (orderReturn.getReturnNo() == null || orderReturn.getReturnNo().isEmpty())
        {
            orderReturn.setReturnNo(mkNumberRuleService.generateNumber("return"));
        }
        // 从订单补充客户信息
        if (orderReturn.getOrderId() != null)
        {
            MkOrder order = mkOrderMapper.selectOrderById(orderReturn.getOrderId());
            if (order != null)
            {
                orderReturn.setOrderNo(order.getOrderNo());
                orderReturn.setCustomerId(order.getCustomerId());
                orderReturn.setCustomerName(order.getCustomerName());
            }
        }
        // 计算退货总金额（如果明细存在则自动汇总）
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (orderReturn.getDetailList() != null && !orderReturn.getDetailList().isEmpty())
        {
            for (MkOrderReturnItem item : orderReturn.getDetailList())
            {
                if (item.getReturnQty() != null && item.getUnitPrice() != null)
                {
                    item.setReturnAmount(item.getReturnQty().multiply(item.getUnitPrice()));
                }
                if (item.getReturnAmount() != null)
                {
                    totalAmount = totalAmount.add(item.getReturnAmount());
                }
            }
            // 明细存在时自动覆盖退货金额
            orderReturn.setReturnAmount(totalAmount);
        }
        // 草稿状态不更新订单状态，提交审批时才更新
        if (orderReturn.getOrderId() != null && !"4".equals(orderReturn.getReturnStatus()))
        {
            MkOrder orderUpdate = new MkOrder();
            orderUpdate.setOrderId(orderReturn.getOrderId());
            orderUpdate.setOrderStatus("6");
            orderUpdate.setUpdateBy(SecurityUtils.getUsername());
            mkOrderMapper.updateOrder(orderUpdate);
        }
        int rows = mkOrderReturnMapper.insertReturn(orderReturn);
        // 插入退货明细
        if (orderReturn.getDetailList() != null)
        {
            for (MkOrderReturnItem item : orderReturn.getDetailList())
            {
                item.setReturnId(orderReturn.getReturnId());
                item.setCreateBy(SecurityUtils.getUsername());
                mkOrderReturnMapper.insertReturnItem(item);
            }
        }
        return rows;
    }

    @Override
    public int deleteReturnByIds(Long[] returnIds)
    {
        return mkOrderReturnMapper.deleteReturnByIds(returnIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateReturn(MkOrderReturn orderReturn)
    {
        MkOrderReturn existing = mkOrderReturnMapper.selectReturnById(orderReturn.getReturnId());
        if (existing == null)
        {
            throw new ServiceException("退货单不存在");
        }
        if (!"2".equals(existing.getReturnStatus()) && !"4".equals(existing.getReturnStatus()))
        {
            throw new ServiceException("只有已驳回或草稿状态的退货单才能修改");
        }
        // 计算退货总金额（如果明细存在则自动汇总）
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (orderReturn.getDetailList() != null && !orderReturn.getDetailList().isEmpty())
        {
            for (MkOrderReturnItem item : orderReturn.getDetailList())
            {
                if (item.getReturnQty() != null && item.getUnitPrice() != null)
                {
                    item.setReturnAmount(item.getReturnQty().multiply(item.getUnitPrice()));
                }
                if (item.getReturnAmount() != null)
                {
                    totalAmount = totalAmount.add(item.getReturnAmount());
                }
            }
            orderReturn.setReturnAmount(totalAmount);
        }
        orderReturn.setReturnStatus("4"); // 修改后回到草稿状态
        orderReturn.setUpdateBy(SecurityUtils.getUsername());
        // 删除旧明细，插入新明细
        mkOrderReturnMapper.deleteReturnItemsByReturnId(orderReturn.getReturnId());
        if (orderReturn.getDetailList() != null)
        {
            for (MkOrderReturnItem item : orderReturn.getDetailList())
            {
                item.setReturnId(orderReturn.getReturnId());
                item.setCreateBy(SecurityUtils.getUsername());
                mkOrderReturnMapper.insertReturnItem(item);
            }
        }
        return mkOrderReturnMapper.updateReturn(orderReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitReturn(Long returnId)
    {
        MkOrderReturn orderReturn = mkOrderReturnMapper.selectReturnById(returnId);
        if (orderReturn == null)
        {
            throw new ServiceException("退货单不存在");
        }
        if (!("4".equals(orderReturn.getReturnStatus()) || "2".equals(orderReturn.getReturnStatus())))
        {
            throw new ServiceException("只有草稿或已驳回状态的退货单才能提交审批");
        }
        orderReturn.setReturnStatus("0"); // 待审批
        orderReturn.setUpdateBy(SecurityUtils.getUsername());
        // 提交审批时更新订单状态为退货中
        if (orderReturn.getOrderId() != null)
        {
            MkOrder orderUpdate = new MkOrder();
            orderUpdate.setOrderId(orderReturn.getOrderId());
            orderUpdate.setOrderStatus("6");
            orderUpdate.setUpdateBy(SecurityUtils.getUsername());
            mkOrderMapper.updateOrder(orderUpdate);
        }
        // 写入审批记录
        MkOrderReturnApproveLog approveLog = new MkOrderReturnApproveLog();
        approveLog.setReturnId(returnId);
        approveLog.setActionType("1");
        approveLog.setApproveOpinion("提交审批");
        approveLog.setApproveBy(SecurityUtils.getUsername());
        mkOrderReturnApproveLogMapper.insertApproveLog(approveLog);
        return mkOrderReturnMapper.updateReturn(orderReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveReturn(Long returnId, boolean approved, String opinion)
    {
        MkOrderReturn orderReturn = mkOrderReturnMapper.selectReturnById(returnId);
        if (orderReturn == null)
        {
            throw new ServiceException("退货记录不存在");
        }
        if (!"0".equals(orderReturn.getReturnStatus()))
        {
            throw new ServiceException("非待审批状态的退货不能操作");
        }
        orderReturn.setReturnStatus(approved ? "1" : "2");
        orderReturn.setApproveOpinion(opinion);
        orderReturn.setApproveBy(SecurityUtils.getUsername());
        orderReturn.setApproveTime(new Date());
        // 驳回时恢复订单原状态
        if (!approved && orderReturn.getOrderId() != null)
        {
            MkOrder order = mkOrderMapper.selectOrderById(orderReturn.getOrderId());
            if (order != null)
            {
                // 根据完成时间判断原状态：有完成时间为已完成(4)，否则为已签收(3)
                String originalStatus = order.getFinishTime() != null ? "4" : "3";
                MkOrder orderUpdate = new MkOrder();
                orderUpdate.setOrderId(orderReturn.getOrderId());
                orderUpdate.setOrderStatus(originalStatus);
                orderUpdate.setUpdateBy(SecurityUtils.getUsername());
                mkOrderMapper.updateOrder(orderUpdate);
            }
        }
        // 写入审批记录
        MkOrderReturnApproveLog approveLog = new MkOrderReturnApproveLog();
        approveLog.setReturnId(returnId);
        approveLog.setActionType(approved ? "2" : "3");
        approveLog.setApproveOpinion(opinion);
        approveLog.setApproveBy(SecurityUtils.getUsername());
        mkOrderReturnApproveLogMapper.insertApproveLog(approveLog);
        return mkOrderReturnMapper.updateReturn(orderReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int refundReturn(Long returnId, BigDecimal refundAmount, Date refundDate)
    {
        MkOrderReturn orderReturn = mkOrderReturnMapper.selectReturnById(returnId);
        if (orderReturn == null)
        {
            throw new ServiceException("退货记录不存在");
        }
        if (!"1".equals(orderReturn.getReturnStatus()))
        {
            throw new ServiceException("只有已批准的退货才能执行退款");
        }
        orderReturn.setReturnStatus("3");
        orderReturn.setRefundAmount(refundAmount);
        orderReturn.setRefundDate(refundDate);
        orderReturn.setUpdateBy(SecurityUtils.getUsername());
        // 更新订单状态为已退货
        MkOrder orderUpdate = new MkOrder();
        orderUpdate.setOrderId(orderReturn.getOrderId());
        orderUpdate.setOrderStatus("7");
        orderUpdate.setUpdateBy(SecurityUtils.getUsername());
        mkOrderMapper.updateOrder(orderUpdate);
        return mkOrderReturnMapper.updateReturn(orderReturn);
    }
}
