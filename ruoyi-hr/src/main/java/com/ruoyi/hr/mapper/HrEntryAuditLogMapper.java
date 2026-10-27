package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrEntryAuditLog;

/**
 * 入职审核日志Mapper接口
 *
 * @author ruoyi
 */
public interface HrEntryAuditLogMapper
{
    /**
     * 查询入职审核日志
     *
     * @param logId 入职审核日志主键
     * @return 入职审核日志
     */
    public HrEntryAuditLog selectHrEntryAuditLogByLogId(Long logId);

    /**
     * 根据入职单ID查询审核日志列表（按审核时间正序）
     *
     * @param entryId 入职单ID
     * @return 入职审核日志集合
     */
    public List<HrEntryAuditLog> selectHrEntryAuditLogByEntryId(Long entryId);

    /**
     * 新增入职审核日志
     *
     * @param hrEntryAuditLog 入职审核日志
     * @return 结果
     */
    public int insertHrEntryAuditLog(HrEntryAuditLog hrEntryAuditLog);

    /**
     * 删除入职审核日志
     *
     * @param logId 入职审核日志主键
     * @return 结果
     */
    public int deleteHrEntryAuditLogByLogId(Long logId);

    /**
     * 根据入职单ID删除审核日志
     *
     * @param entryId 入职单ID
     * @return 结果
     */
    public int deleteHrEntryAuditLogByEntryId(Long entryId);
}
