package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrLeaveRequest;
import com.ruoyi.hr.mapper.HrLeaveRequestMapper;
import com.ruoyi.hr.service.IHrLeaveRequestService;

@Service
public class HrLeaveRequestServiceImpl implements IHrLeaveRequestService
{
    @Autowired
    private HrLeaveRequestMapper hrLeaveRequestMapper;

    @Override
    public HrLeaveRequest selectHrLeaveRequestById(Long leaveReqId) { return hrLeaveRequestMapper.selectHrLeaveRequestById(leaveReqId); }

    @Override
    public List<HrLeaveRequest> selectHrLeaveRequestList(HrLeaveRequest hrLeaveRequest) { return hrLeaveRequestMapper.selectHrLeaveRequestList(hrLeaveRequest); }

    @Override
    public int insertHrLeaveRequest(HrLeaveRequest hrLeaveRequest) { return hrLeaveRequestMapper.insertHrLeaveRequest(hrLeaveRequest); }

    @Override
    public int updateHrLeaveRequest(HrLeaveRequest hrLeaveRequest) { return hrLeaveRequestMapper.updateHrLeaveRequest(hrLeaveRequest); }

    @Override
    public int deleteHrLeaveRequestByIds(Long[] leaveReqIds) { return hrLeaveRequestMapper.deleteHrLeaveRequestByIds(leaveReqIds); }

    @Override
    public int deleteHrLeaveRequestById(Long leaveReqId) { return hrLeaveRequestMapper.deleteHrLeaveRequestById(leaveReqId); }
}
