package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsSupplierEvalAuditLog;

/**
 * 供应商评估审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsSupplierEvalAuditLogMapper
{
    /** 根据评估ID查询审核日志 */
    public List<PmsSupplierEvalAuditLog> selectAuditLogByEvalId(Long evalId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsSupplierEvalAuditLog log);
}
