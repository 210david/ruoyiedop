package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrBusinessTripAuditLog;

public interface HrBusinessTripAuditLogMapper
{
    public HrBusinessTripAuditLog selectHrBusinessTripAuditLogByLogId(Long logId);
    public List<HrBusinessTripAuditLog> selectHrBusinessTripAuditLogByTripId(Long tripId);
    public int insertHrBusinessTripAuditLog(HrBusinessTripAuditLog hrBusinessTripAuditLog);
    public int deleteHrBusinessTripAuditLogByLogId(Long logId);
    public int deleteHrBusinessTripAuditLogByTripId(Long tripId);
}
