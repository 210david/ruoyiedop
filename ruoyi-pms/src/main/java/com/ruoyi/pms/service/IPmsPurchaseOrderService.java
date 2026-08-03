package com.ruoyi.pms.service;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.pms.domain.PmsPurchaseOrder;

/**
 * 采购订单 Service接口
 *
 * @author ruoyi
 */
public interface IPmsPurchaseOrderService
{
    public List<PmsPurchaseOrder> selectPurchaseOrderList(PmsPurchaseOrder order);
    public PmsPurchaseOrder selectPurchaseOrderById(Long orderId);
    public int insertPurchaseOrder(PmsPurchaseOrder order);
    public int updatePurchaseOrder(PmsPurchaseOrder order);
    public int deletePurchaseOrderByIds(Long[] orderIds);
    public int submitPurchaseOrder(Long orderId);
    public int auditPurchaseOrder(Long orderId, String status, String auditOpinion);
    
    /**
     * 更新采购订单已收数量
     * 
     * @param orderId 采购订单ID
     * @param orderDetailId 采购订单明细ID
     * @param receivedQty 本次收货数量
     * @param receivedAmount 本次收货金额
     * @return 结果
     */
    public int updateOrderReceivedQty(Long orderId, Long orderDetailId, BigDecimal receivedQty, BigDecimal receivedAmount);
}
