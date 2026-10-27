package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsReturnAuditLog;

/**
 * 退货审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsReturnAuditLogMapper
{
    /** 根据退货ID查询审核日志 */
    public List<PmsReturnAuditLog> selectAuditLogByReturnId(Long returnId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsReturnAuditLog log);
}
