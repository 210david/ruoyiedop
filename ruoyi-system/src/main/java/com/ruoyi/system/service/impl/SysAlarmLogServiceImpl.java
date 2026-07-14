package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysAlarmLog;
import com.ruoyi.system.mapper.SysAlarmLogMapper;
import com.ruoyi.system.service.ISysAlarmLogService;

/**
 * 系统告警日志 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysAlarmLogServiceImpl implements ISysAlarmLogService
{
    @Autowired
    private SysAlarmLogMapper alarmLogMapper;

    @Override
    public void insertAlarmLog(SysAlarmLog alarmLog)
    {
        alarmLogMapper.insertAlarmLog(alarmLog);
    }

    @Override
    public List<SysAlarmLog> selectAlarmLogList(SysAlarmLog alarmLog)
    {
        return alarmLogMapper.selectAlarmLogList(alarmLog);
    }

    @Override
    public SysAlarmLog selectAlarmLogById(Long id)
    {
        return alarmLogMapper.selectAlarmLogById(id);
    }

    @Override
    public int deleteAlarmLogByIds(Long[] ids)
    {
        return alarmLogMapper.deleteAlarmLogByIds(ids);
    }

    @Override
    public void cleanAlarmLog()
    {
        alarmLogMapper.cleanAlarmLog();
    }
}
