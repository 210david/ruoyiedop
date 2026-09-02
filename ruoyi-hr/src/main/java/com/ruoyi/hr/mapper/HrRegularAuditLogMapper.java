package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrRegularAuditLog;

/**
 * 转正审核日志Mapper接口
 *
 * @author ruoyi
 */
public interface HrRegularAuditLogMapper
{
    /**
     * 查询转正审核日志
     *
     * @param logId 转正审核日志主键
     * @return 转正审核日志
     */
    public HrRegularAuditLog selectHrRegularAuditLogByLogId(Long logId);

    /**
     * 根据转正单ID查询审核日志列表（按审核时间正序）
     *
     * @param regularId 转正单ID
     * @return 转正审核日志集合
     */
    public List<HrRegularAuditLog> selectHrRegularAuditLogByRegularId(Long regularId);

    /**
     * 新增转正审核日志
     *
     * @param hrRegularAuditLog 转正审核日志
     * @return 结果
     */
    public int insertHrRegularAuditLog(HrRegularAuditLog hrRegularAuditLog);

    /**
     * 删除转正审核日志
     *
     * @param logId 转正审核日志主键
     * @return 结果
     */
    public int deleteHrRegularAuditLogByLogId(Long logId);

    /**
     * 根据转正单ID删除审核日志
     *
     * @param regularId 转正单ID
     * @return 结果
     */
    public int deleteHrRegularAuditLogByRegularId(Long regularId);
}
