package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrTransferAuditLog;

/**
 * 调动审核日志Mapper接口
 *
 * @author ruoyi
 */
public interface HrTransferAuditLogMapper
{
    /**
     * 查询调动审核日志
     *
     * @param logId 调动审核日志主键
     * @return 调动审核日志
     */
    public HrTransferAuditLog selectHrTransferAuditLogByLogId(Long logId);

    /**
     * 根据调动单ID查询审核日志列表（按审核时间正序）
     *
     * @param transferId 调动单ID
     * @return 调动审核日志集合
     */
    public List<HrTransferAuditLog> selectHrTransferAuditLogByTransferId(Long transferId);

    /**
     * 新增调动审核日志
     *
     * @param hrTransferAuditLog 调动审核日志
     * @return 结果
     */
    public int insertHrTransferAuditLog(HrTransferAuditLog hrTransferAuditLog);

    /**
     * 删除调动审核日志
     *
     * @param logId 调动审核日志主键
     * @return 结果
     */
    public int deleteHrTransferAuditLogByLogId(Long logId);

    /**
     * 根据调动单ID删除审核日志
     *
     * @param transferId 调动单ID
     * @return 结果
     */
    public int deleteHrTransferAuditLogByTransferId(Long transferId);
}
