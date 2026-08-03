package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsRequestAuditLog;

/**
 * 采购申请审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsRequestAuditLogMapper
{
    /** 根据申请ID查询审核日志 */
    public List<PmsRequestAuditLog> selectAuditLogByRequestId(Long requestId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsRequestAuditLog log);
}
