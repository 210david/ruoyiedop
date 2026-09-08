package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrOutingAuditLog;

public interface HrOutingAuditLogMapper
{
    public HrOutingAuditLog selectHrOutingAuditLogByLogId(Long logId);
    public List<HrOutingAuditLog> selectHrOutingAuditLogByOutingId(Long outingId);
    public int insertHrOutingAuditLog(HrOutingAuditLog hrOutingAuditLog);
    public int deleteHrOutingAuditLogByLogId(Long logId);
    public int deleteHrOutingAuditLogByOutingId(Long outingId);
}
