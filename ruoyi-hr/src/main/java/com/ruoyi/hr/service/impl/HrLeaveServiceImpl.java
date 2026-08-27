package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrLeave;
import com.ruoyi.hr.mapper.HrLeaveMapper;
import com.ruoyi.hr.service.IHrLeaveService;

@Service
public class HrLeaveServiceImpl implements IHrLeaveService
{
    @Autowired
    private HrLeaveMapper hrLeaveMapper;

    @Override
    public HrLeave selectHrLeaveById(Long leaveId) { return hrLeaveMapper.selectHrLeaveById(leaveId); }

    @Override
    public List<HrLeave> selectHrLeaveList(HrLeave hrLeave) { return hrLeaveMapper.selectHrLeaveList(hrLeave); }

    @Override
    public int insertHrLeave(HrLeave hrLeave) { return hrLeaveMapper.insertHrLeave(hrLeave); }

    @Override
    public int updateHrLeave(HrLeave hrLeave) { return hrLeaveMapper.updateHrLeave(hrLeave); }

    @Override
    public int deleteHrLeaveByIds(Long[] leaveIds) { return hrLeaveMapper.deleteHrLeaveByIds(leaveIds); }

    @Override
    public int deleteHrLeaveById(Long leaveId) { return hrLeaveMapper.deleteHrLeaveById(leaveId); }
}
