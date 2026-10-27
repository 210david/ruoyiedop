package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsPlanAuditLog;

/**
 * 采购计划审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsPlanAuditLogMapper
{
    /** 根据计划ID查询审核日志 */
    public List<PmsPlanAuditLog> selectAuditLogByPlanId(Long planId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsPlanAuditLog log);
}
