package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrOvertimeAuditLog;

public interface HrOvertimeAuditLogMapper
{
    public HrOvertimeAuditLog selectHrOvertimeAuditLogByLogId(Long logId);
    public List<HrOvertimeAuditLog> selectHrOvertimeAuditLogByOvertimeId(Long overtimeId);
    public int insertHrOvertimeAuditLog(HrOvertimeAuditLog hrOvertimeAuditLog);
    public int deleteHrOvertimeAuditLogByLogId(Long logId);
    public int deleteHrOvertimeAuditLogByOvertimeId(Long overtimeId);
}
