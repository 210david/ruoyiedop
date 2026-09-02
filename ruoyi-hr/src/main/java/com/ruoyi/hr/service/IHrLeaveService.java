package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrLeave;

public interface IHrLeaveService {
    public HrLeave selectHrLeaveById(Long leaveId);
    public List<HrLeave> selectHrLeaveList(HrLeave hrLeave);
    public int insertHrLeave(HrLeave hrLeave);
    public int updateHrLeave(HrLeave hrLeave);
    public int deleteHrLeaveByIds(Long[] leaveIds);
    public int deleteHrLeaveById(Long leaveId);
    public int auditHrLeave(Long leaveId, String auditAction, String auditRemark);
}
