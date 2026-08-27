package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrOvertime;

public interface HrOvertimeMapper {
    public HrOvertime selectHrOvertimeById(Long overtimeId);
    public List<HrOvertime> selectHrOvertimeList(HrOvertime hrOvertime);
    public int insertHrOvertime(HrOvertime hrOvertime);
    public int updateHrOvertime(HrOvertime hrOvertime);
    public int deleteHrOvertimeByIds(Long[] overtimeIds);
    public int deleteHrOvertimeById(Long overtimeId);
}
