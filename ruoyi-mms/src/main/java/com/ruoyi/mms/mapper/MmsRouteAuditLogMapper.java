package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsRouteAuditLog;

/**
 * 工艺路线审核日志 Mapper
 */
public interface MmsRouteAuditLogMapper
{
    /** 查询审核日志列表 */
    List<MmsRouteAuditLog> selectAuditLogByRouteId(Long routeId);

    /** 新增审核日志 */
    int insertAuditLog(MmsRouteAuditLog auditLog);
}
