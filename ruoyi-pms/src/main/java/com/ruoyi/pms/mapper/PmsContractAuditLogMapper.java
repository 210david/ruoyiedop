package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsContractAuditLog;

/**
 * 采购合同审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsContractAuditLogMapper
{
    /** 根据合同ID查询审核日志 */
    public List<PmsContractAuditLog> selectAuditLogByContractId(Long contractId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsContractAuditLog log);
}
