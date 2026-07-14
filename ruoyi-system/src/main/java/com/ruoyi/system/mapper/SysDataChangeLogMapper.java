package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysDataChangeLog;

/**
 * 数据变更日志 数据层
 * 
 * @author ruoyi
 */
public interface SysDataChangeLogMapper
{
    /**
     * 新增数据变更日志
     * 
     * @param log 数据变更日志对象
     */
    public void insertChangeLog(SysDataChangeLog log);

    /**
     * 批量新增数据变更日志
     * 
     * @param logList 变更日志列表
     */
    public void batchInsertChangeLog(List<SysDataChangeLog> logList);

    /**
     * 根据实体名称和ID查询变更记录
     * 
     * @param entityName 实体名称
     * @param entityId 实体ID
     * @return 变更日志集合
     */
    public List<SysDataChangeLog> selectByEntity(String entityName, String entityId);
}
