package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrLeaveRequest;

public interface HrLeaveRequestMapper {
    public HrLeaveRequest selectHrLeaveRequestById(Long leaveReqId);
    public List<HrLeaveRequest> selectHrLeaveRequestList(HrLeaveRequest hrLeaveRequest);
    public int insertHrLeaveRequest(HrLeaveRequest hrLeaveRequest);
    public int updateHrLeaveRequest(HrLeaveRequest hrLeaveRequest);
    public int deleteHrLeaveRequestByIds(Long[] leaveReqIds);
    public int deleteHrLeaveRequestById(Long leaveReqId);
}
