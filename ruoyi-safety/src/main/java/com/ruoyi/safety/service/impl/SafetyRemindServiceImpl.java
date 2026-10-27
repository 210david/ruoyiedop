package com.ruoyi.safety.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.safety.domain.SafetyRemind;
import com.ruoyi.safety.mapper.SafetyRemindMapper;
import com.ruoyi.safety.service.ISafetyRemindService;

@Service
public class SafetyRemindServiceImpl implements ISafetyRemindService
{
    @Autowired
    private SafetyRemindMapper safetyRemindMapper;

    @Override
    public SafetyRemind selectSafetyRemindById(Long remindId) { return safetyRemindMapper.selectSafetyRemindById(remindId); }

    @Override
    public List<SafetyRemind> selectSafetyRemindList(SafetyRemind safetyRemind) { return safetyRemindMapper.selectSafetyRemindList(safetyRemind); }

    @Override
    public int insertSafetyRemind(SafetyRemind safetyRemind) { return safetyRemindMapper.insertSafetyRemind(safetyRemind); }

    @Override
    public int updateSafetyRemind(SafetyRemind safetyRemind) { return safetyRemindMapper.updateSafetyRemind(safetyRemind); }

    @Override
    public int deleteSafetyRemindByIds(Long[] remindIds) { return safetyRemindMapper.deleteSafetyRemindByIds(remindIds); }

    @Override
    public int deleteSafetyRemindById(Long remindId) { return safetyRemindMapper.deleteSafetyRemindById(remindId); }

    @Override
    public int handleRemind(SafetyRemind safetyRemind)
    {
        safetyRemind.setRemindStatus("2");
        safetyRemind.setHandleBy(SecurityUtils.getUsername());
        safetyRemind.setHandleTime(DateUtils.getNowDate());
        return safetyRemindMapper.updateSafetyRemind(safetyRemind);
    }

    @Override
    public int countByStatus(String remindStatus)
    {
        return safetyRemindMapper.countByStatus(remindStatus);
    }

    @Override
    public List<Map<String, Object>> getRemindCountByType()
    {
        return safetyRemindMapper.selectRemindCountByType();
    }

    @Override
    public List<Map<String, Object>> getUpcomingReminds()
    {
        return safetyRemindMapper.selectUpcomingReminds();
    }
}