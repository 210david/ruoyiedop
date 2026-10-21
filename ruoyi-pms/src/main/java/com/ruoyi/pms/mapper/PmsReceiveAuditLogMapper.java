package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsReceiveAuditLog;

/**
 * 收货验收审核日志 Mapper
 *
 * @author ruoyi
 */
public interface PmsReceiveAuditLogMapper
{
    /** 根据收货ID查询审核日志 */
    public List<PmsReceiveAuditLog> selectAuditLogByReceiveId(Long receiveId);

    /** 新增审核日志 */
    public int insertAuditLog(PmsReceiveAuditLog log);
}
