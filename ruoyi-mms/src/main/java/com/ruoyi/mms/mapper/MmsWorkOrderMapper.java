package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.domain.MmsWorkOrderAuditLog;

public interface MmsWorkOrderMapper {
    public List<MmsWorkOrder> selectWorkOrderList(MmsWorkOrder workOrder);
    public MmsWorkOrder selectWorkOrderById(Long workOrderId);
    public int insertWorkOrder(MmsWorkOrder workOrder);
    public int updateWorkOrder(MmsWorkOrder workOrder);
    public int deleteWorkOrderByIds(Long[] workOrderIds);
    public List<MmsWorkOrderAuditLog> selectAuditLogByWorkOrderId(Long workOrderId);
    public int insertAuditLog(MmsWorkOrderAuditLog log);
}