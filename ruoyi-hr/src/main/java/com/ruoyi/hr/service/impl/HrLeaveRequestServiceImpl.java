package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrLeaveRequest;
import com.ruoyi.hr.mapper.HrLeaveRequestMapper;
import com.ruoyi.hr.service.IHrLeaveRequestService;

@Service
public class HrLeaveRequestServiceImpl implements IHrLeaveRequestService
{
    @Autowired
    private HrLeaveRequestMapper hrLeaveRequestMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrLeaveRequest selectHrLeaveRequestById(Long leaveReqId) { return hrLeaveRequestMapper.selectHrLeaveRequestById(leaveReqId); }

    @Override
    public List<HrLeaveRequest> selectHrLeaveRequestList(HrLeaveRequest hrLeaveRequest) { return hrLeaveRequestMapper.selectHrLeaveRequestList(hrLeaveRequest); }

    @Override
    public int insertHrLeaveRequest(HrLeaveRequest hrLeaveRequest)
    {
        if (StringUtils.isEmpty(hrLeaveRequest.getLeaveReqNo()))
        {
            hrLeaveRequest.setLeaveReqNo(mkNumberRuleService.generateNumber("hr_leave_request"));
        }
        computeLeaveDays(hrLeaveRequest);
        return hrLeaveRequestMapper.insertHrLeaveRequest(hrLeaveRequest);
    }

    @Override
    public int updateHrLeaveRequest(HrLeaveRequest hrLeaveRequest)
    {
        computeLeaveDays(hrLeaveRequest);
        return hrLeaveRequestMapper.updateHrLeaveRequest(hrLeaveRequest);
    }

    /** 根据开始/结束日期自动计算请假天数（含首尾，服务端兑底） */
    private void computeLeaveDays(HrLeaveRequest leave)
    {
        if (leave.getStartTime() == null || leave.getEndTime() == null
                || leave.getEndTime().before(leave.getStartTime()))
        {
            return;
        }
        long millis = leave.getEndTime().getTime() - leave.getStartTime().getTime();
        long days = millis / 86400000L;
        if (millis % 86400000L != 0)
        {
            days++;
        }
        leave.setLeaveDays(java.math.BigDecimal.valueOf(days));
    }

    @Override
    public int deleteHrLeaveRequestByIds(Long[] leaveReqIds) { return hrLeaveRequestMapper.deleteHrLeaveRequestByIds(leaveReqIds); }

    @Override
    public int deleteHrLeaveRequestById(Long leaveReqId) { return hrLeaveRequestMapper.deleteHrLeaveRequestById(leaveReqId); }
}
