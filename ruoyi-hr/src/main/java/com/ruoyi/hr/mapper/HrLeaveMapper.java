package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrLeave;

public interface HrLeaveMapper {
    public HrLeave selectHrLeaveById(Long leaveId);
    public List<HrLeave> selectHrLeaveList(HrLeave hrLeave);
    public int insertHrLeave(HrLeave hrLeave);
    public int updateHrLeave(HrLeave hrLeave);
    public int deleteHrLeaveByIds(Long[] leaveIds);
    public int deleteHrLeaveById(Long leaveId);
}
