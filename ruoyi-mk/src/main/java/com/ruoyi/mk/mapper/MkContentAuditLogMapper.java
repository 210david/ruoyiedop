package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkContentAuditLog;

public interface MkContentAuditLogMapper
{
    /** 查询内容审核日志 */
    public List<MkContentAuditLog> selectAuditLogByContentId(Long contentId);

    /** 新增审核日志 */
    public int insertAuditLog(MkContentAuditLog auditLog);
}
