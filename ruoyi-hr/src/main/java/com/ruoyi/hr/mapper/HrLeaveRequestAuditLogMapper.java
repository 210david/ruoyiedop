package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrLeaveRequestAuditLog;

/**
 * 请假审核日志Mapper接口
 *
 * @author ruoyi
 */
public interface HrLeaveRequestAuditLogMapper
{
    /**
     * 查询请假审核日志
     *
     * @param logId 请假审核日志主键
     * @return 请假审核日志
     */
    public HrLeaveRequestAuditLog selectHrLeaveRequestAuditLogByLogId(Long logId);

    /**
     * 根据请假单ID查询审核日志列表（按审核时间正序）
     *
     * @param leaveReqId 请假单ID
     * @return 请假审核日志集合
     */
    public List<HrLeaveRequestAuditLog> selectHrLeaveRequestAuditLogByLeaveReqId(Long leaveReqId);

    /**
     * 新增请假审核日志
     *
     * @param hrLeaveRequestAuditLog 请假审核日志
     * @return 结果
     */
    public int insertHrLeaveRequestAuditLog(HrLeaveRequestAuditLog hrLeaveRequestAuditLog);

    /**
     * 删除请假审核日志
     *
     * @param logId 请假审核日志主键
     * @return 结果
     */
    public int deleteHrLeaveRequestAuditLogByLogId(Long logId);

    /**
     * 根据请假单ID删除审核日志
     *
     * @param leaveReqId 请假单ID
     * @return 结果
     */
    public int deleteHrLeaveRequestAuditLogByLeaveReqId(Long leaveReqId);
}
