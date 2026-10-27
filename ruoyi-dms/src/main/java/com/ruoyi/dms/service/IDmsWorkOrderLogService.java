package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.dms.domain.DmsWorkOrderLog;

public interface IDmsWorkOrderLogService
{
    /** 记录工单操作日志 */
    public void recordLog(Long orderId, String orderNo, String fromStatus, String toStatus,
                           String action, String operatorName, String remark);

    /** 按工单ID查询操作日志列表 */
    public List<DmsWorkOrderLog> selectLogByOrderId(Long orderId);
}
