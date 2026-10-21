package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsOrderAuditLog;

/**
 * 采购订单审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsOrderAuditLogMapper
{
    /** 根据订单ID查询审核日志 */
    public List<PmsOrderAuditLog> selectAuditLogByOrderId(Long orderId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsOrderAuditLog log);
}
