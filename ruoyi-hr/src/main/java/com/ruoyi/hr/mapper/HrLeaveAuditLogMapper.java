package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrLeaveAuditLog;

/**
 * 离职审核日志Mapper接口
 *
 * @author ruoyi
 */
public interface HrLeaveAuditLogMapper
{
    /**
     * 查询离职审核日志
     *
     * @param logId 离职审核日志主键
     * @return 离职审核日志
     */
    public HrLeaveAuditLog selectHrLeaveAuditLogByLogId(Long logId);

    /**
     * 根据离职单ID查询审核日志列表（按审核时间正序）
     *
     * @param leaveId 离职单ID
     * @return 离职审核日志集合
     */
    public List<HrLeaveAuditLog> selectHrLeaveAuditLogByLeaveId(Long leaveId);

    /**
     * 新增离职审核日志
     *
     * @param hrLeaveAuditLog 离职审核日志
     * @return 结果
     */
    public int insertHrLeaveAuditLog(HrLeaveAuditLog hrLeaveAuditLog);

    /**
     * 删除离职审核日志
     *
     * @param logId 离职审核日志主键
     * @return 结果
     */
    public int deleteHrLeaveAuditLogByLogId(Long logId);

    /**
     * 根据离职单ID删除审核日志
     *
     * @param leaveId 离职单ID
     * @return 结果
     */
    public int deleteHrLeaveAuditLogByLeaveId(Long leaveId);
}
