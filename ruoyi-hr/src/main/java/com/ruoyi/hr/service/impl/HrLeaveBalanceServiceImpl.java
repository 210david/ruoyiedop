package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrLeaveBalance;
import com.ruoyi.hr.mapper.HrLeaveBalanceMapper;
import com.ruoyi.hr.service.IHrLeaveBalanceService;

@Service
public class HrLeaveBalanceServiceImpl implements IHrLeaveBalanceService
{
    @Autowired
    private HrLeaveBalanceMapper hrLeaveBalanceMapper;

    @Override
    public HrLeaveBalance selectHrLeaveBalanceById(Long balanceId) { return hrLeaveBalanceMapper.selectHrLeaveBalanceById(balanceId); }

    @Override
    public List<HrLeaveBalance> selectHrLeaveBalanceList(HrLeaveBalance hrLeaveBalance) { return hrLeaveBalanceMapper.selectHrLeaveBalanceList(hrLeaveBalance); }

    @Override
    public int insertHrLeaveBalance(HrLeaveBalance hrLeaveBalance) { return hrLeaveBalanceMapper.insertHrLeaveBalance(hrLeaveBalance); }

    @Override
    public int updateHrLeaveBalance(HrLeaveBalance hrLeaveBalance) { return hrLeaveBalanceMapper.updateHrLeaveBalance(hrLeaveBalance); }

    @Override
    public int deleteHrLeaveBalanceByIds(Long[] balanceIds) { return hrLeaveBalanceMapper.deleteHrLeaveBalanceByIds(balanceIds); }

    @Override
    public int deleteHrLeaveBalanceById(Long balanceId) { return hrLeaveBalanceMapper.deleteHrLeaveBalanceById(balanceId); }
}
