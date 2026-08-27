package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrSchedule;

public interface IHrScheduleService {
    public HrSchedule selectHrScheduleById(Long scheduleId);
    public List<HrSchedule> selectHrScheduleList(HrSchedule hrSchedule);
    public int insertHrSchedule(HrSchedule hrSchedule);
    public int updateHrSchedule(HrSchedule hrSchedule);
    public int deleteHrScheduleByIds(Long[] scheduleIds);
    public int deleteHrScheduleById(Long scheduleId);
}
