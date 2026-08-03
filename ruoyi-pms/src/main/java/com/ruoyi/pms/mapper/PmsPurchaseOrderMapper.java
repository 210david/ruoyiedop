package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsPurchaseOrder;
import com.ruoyi.pms.domain.PmsPurchaseOrderDetail;

/**
 * 采购订单 Mapper
 *
 * @author ruoyi
 */
public interface PmsPurchaseOrderMapper
{
    public List<PmsPurchaseOrder> selectPurchaseOrderList(PmsPurchaseOrder order);
    public PmsPurchaseOrder selectPurchaseOrderById(Long orderId);
    public int insertPurchaseOrder(PmsPurchaseOrder order);
    public int updatePurchaseOrder(PmsPurchaseOrder order);
    public int deletePurchaseOrderByIds(Long[] orderIds);
    public List<PmsPurchaseOrderDetail> selectOrderDetailByOrderId(Long orderId);
    public PmsPurchaseOrderDetail selectOrderDetailById(Long detailId);
    public int insertOrderDetail(PmsPurchaseOrderDetail detail);
    public int updateOrderDetail(PmsPurchaseOrderDetail detail);
    public int deleteOrderDetailByOrderId(Long orderId);
}
