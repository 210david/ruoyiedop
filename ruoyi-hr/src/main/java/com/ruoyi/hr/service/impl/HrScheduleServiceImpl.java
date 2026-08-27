package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.mapper.HrScheduleMapper;
import com.ruoyi.hr.service.IHrScheduleService;

@Service
public class HrScheduleServiceImpl implements IHrScheduleService
{
    @Autowired
    private HrScheduleMapper hrScheduleMapper;

    @Override
    public HrSchedule selectHrScheduleById(Long scheduleId) { return hrScheduleMapper.selectHrScheduleById(scheduleId); }

    @Override
    public List<HrSchedule> selectHrScheduleList(HrSchedule hrSchedule) { return hrScheduleMapper.selectHrScheduleList(hrSchedule); }

    @Override
    public int insertHrSchedule(HrSchedule hrSchedule) { return hrScheduleMapper.insertHrSchedule(hrSchedule); }

    @Override
    public int updateHrSchedule(HrSchedule hrSchedule) { return hrScheduleMapper.updateHrSchedule(hrSchedule); }

    @Override
    public int deleteHrScheduleByIds(Long[] scheduleIds) { return hrScheduleMapper.deleteHrScheduleByIds(scheduleIds); }

    @Override
    public int deleteHrScheduleById(Long scheduleId) { return hrScheduleMapper.deleteHrScheduleById(scheduleId); }
}
