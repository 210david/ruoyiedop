package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyHazardAuditLog;

/**
 * 隐患审批日志 Mapper
 *
 * @author ruoyi
 */
public interface SafetyHazardAuditLogMapper
{
    /** 根据隐患ID查询审批日志 */
    public List<SafetyHazardAuditLog> selectAuditLogByHazardId(Long hazardId);

    /** 新增审批日志 */
    public int insertAuditLog(SafetyHazardAuditLog log);
}
