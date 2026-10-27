package com.ruoyi.dms.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.dms.domain.DmsWorkOrder;

public interface DmsWorkOrderMapper
{
    public List<DmsWorkOrder> selectWorkOrderList(DmsWorkOrder workOrder);
    public DmsWorkOrder selectWorkOrderById(Long orderId);
    public int insertWorkOrder(DmsWorkOrder workOrder);
    public int updateWorkOrder(DmsWorkOrder workOrder);
    public int deleteWorkOrderByIds(Long[] orderIds);
    public String selectMaxOrderNo();

    /** 查询所有SLA超时的工单（未完结且超时未升级） */
    public List<DmsWorkOrder> selectSlaTimeoutOrders();

    /** 统计全部工单数量 */
    public int countAllWorkOrders();

    /** 按状态统计工单数量 */
    public List<Map<String, Object>> countByStatus();
}
