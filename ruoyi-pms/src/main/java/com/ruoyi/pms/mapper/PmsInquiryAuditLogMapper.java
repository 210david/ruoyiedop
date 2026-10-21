package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsInquiryAuditLog;

/**
 * 询比价审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsInquiryAuditLogMapper
{
    /** 根据询价ID查询审核日志 */
    public List<PmsInquiryAuditLog> selectAuditLogByInquiryId(Long inquiryId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsInquiryAuditLog log);
}
