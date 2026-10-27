package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkOrderAuditLog;

public interface MkOrderAuditLogMapper
{
    /** 查询订单审核日志 */
    public List<MkOrderAuditLog> selectAuditLogByOrderId(Long orderId);

    /** 新增审核日志 */
    public int insertAuditLog(MkOrderAuditLog auditLog);
}
