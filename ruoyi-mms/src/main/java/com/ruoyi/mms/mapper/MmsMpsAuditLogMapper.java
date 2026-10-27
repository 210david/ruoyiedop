package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsMpsAuditLog;

/**
 * 主生产计划审核日志 Mapper
 *
 * @author ruoyi
 */
public interface MmsMpsAuditLogMapper
{
    /** 根据计划ID查询审核日志 */
    public List<MmsMpsAuditLog> selectAuditLogByMpsId(Long mpsId);

    /** 新增审核日志 */
    public int insertAuditLog(MmsMpsAuditLog log);
}
