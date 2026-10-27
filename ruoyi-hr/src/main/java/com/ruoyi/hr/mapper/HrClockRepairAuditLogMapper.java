package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrClockRepairAuditLog;

public interface HrClockRepairAuditLogMapper
{
    public HrClockRepairAuditLog selectHrClockRepairAuditLogByLogId(Long logId);
    public List<HrClockRepairAuditLog> selectHrClockRepairAuditLogByClockRepairId(Long clockRepairId);
    public int insertHrClockRepairAuditLog(HrClockRepairAuditLog hrClockRepairAuditLog);
    public int deleteHrClockRepairAuditLogByLogId(Long logId);
    public int deleteHrClockRepairAuditLogByClockRepairId(Long clockRepairId);
}
