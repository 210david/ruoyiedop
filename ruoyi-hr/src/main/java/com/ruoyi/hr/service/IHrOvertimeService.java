package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrOvertime;

public interface IHrOvertimeService {
    public HrOvertime selectHrOvertimeById(Long overtimeId);
    public List<HrOvertime> selectHrOvertimeList(HrOvertime hrOvertime);
    public int insertHrOvertime(HrOvertime hrOvertime);
    public int updateHrOvertime(HrOvertime hrOvertime);
    public int deleteHrOvertimeByIds(Long[] overtimeIds);
    public int deleteHrOvertimeById(Long overtimeId);
}
