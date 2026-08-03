package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsInvoiceAuditLog;

/**
 * 发票结算审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsInvoiceAuditLogMapper
{
    /** 根据发票ID查询审核日志 */
    public List<PmsInvoiceAuditLog> selectAuditLogByInvoiceId(Long invoiceId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsInvoiceAuditLog log);
}
