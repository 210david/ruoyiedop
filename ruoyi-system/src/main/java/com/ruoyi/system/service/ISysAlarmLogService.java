package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysAlarmLog;

/**
 * 系统告警日志 服务层
 * 
 * @author ruoyi
 */
public interface ISysAlarmLogService
{
    /**
     * 新增告警日志
     * 
     * @param alarmLog 告警日志对象
     */
    public void insertAlarmLog(SysAlarmLog alarmLog);

    /**
     * 查询告警日志集合
     * 
     * @param alarmLog 告警日志对象
     * @return 告警日志集合
     */
    public List<SysAlarmLog> selectAlarmLogList(SysAlarmLog alarmLog);

    /**
     * 查询告警日志详细
     * 
     * @param id 告警日志ID
     * @return 告警日志对象
     */
    public SysAlarmLog selectAlarmLogById(Long id);

    /**
     * 批量删除告警日志
     * 
     * @param ids 需要删除的告警日志ID
     * @return 结果
     */
    public int deleteAlarmLogByIds(Long[] ids);

    /**
     * 清空告警日志
     */
    public void cleanAlarmLog();
}
