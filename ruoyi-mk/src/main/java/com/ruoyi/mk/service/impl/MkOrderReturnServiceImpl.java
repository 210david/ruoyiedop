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
import com.ruoyi.mk.mapper.MkOrderMapper;
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
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkOrderReturn> selectReturnList(MkOrderReturn orderReturn)
    {
        return mkOrderReturnMapper.selectReturnList(orderReturn);
    }

    @Override
    public MkOrderReturn selectReturnById(Long returnId)
    {
        return mkOrderReturnMapper.selectReturnById(returnId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertReturn(MkOrderReturn orderReturn)
    {
        if (orderReturn.getReturnStatus() == null)
        {
            orderReturn.setReturnStatus("0");
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
        // 更新订单状态为退货中
        if (orderReturn.getOrderId() != null)
        {
            MkOrder orderUpdate = new MkOrder();
            orderUpdate.setOrderId(orderReturn.getOrderId());
            orderUpdate.setOrderStatus("6");
            orderUpdate.setUpdateBy(SecurityUtils.getUsername());
            mkOrderMapper.updateOrder(orderUpdate);
        }
        return mkOrderReturnMapper.insertReturn(orderReturn);
    }

    @Override
    public int deleteReturnByIds(Long[] returnIds)
    {
        return mkOrderReturnMapper.deleteReturnByIds(returnIds);
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
