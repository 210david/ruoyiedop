package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrClockRepair;

public interface HrClockRepairMapper {
    public HrClockRepair selectHrClockRepairById(Long clockRepairId);
    public List<HrClockRepair> selectHrClockRepairList(HrClockRepair hrClockRepair);
    public int insertHrClockRepair(HrClockRepair hrClockRepair);
    public int updateHrClockRepair(HrClockRepair hrClockRepair);
    public int deleteHrClockRepairByIds(Long[] clockRepairIds);
    public int deleteHrClockRepairById(Long clockRepairId);
}
