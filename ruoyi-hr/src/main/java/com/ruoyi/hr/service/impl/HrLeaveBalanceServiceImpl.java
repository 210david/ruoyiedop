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
    public int insertHrLeaveBalance(HrLeaveBalance hrLeaveBalance)
    {
        computeRemainingDays(hrLeaveBalance);
        return hrLeaveBalanceMapper.insertHrLeaveBalance(hrLeaveBalance);
    }

    @Override
    public int updateHrLeaveBalance(HrLeaveBalance hrLeaveBalance)
    {
        computeRemainingDays(hrLeaveBalance);
        return hrLeaveBalanceMapper.updateHrLeaveBalance(hrLeaveBalance);
    }

    /** 剩余天数 = 总额度 - 已休天数（服务端兑底） */
    private void computeRemainingDays(HrLeaveBalance balance)
    {
        if (balance.getTotalDays() == null || balance.getUsedDays() == null)
        {
            return;
        }
        balance.setRemainingDays(balance.getTotalDays().subtract(balance.getUsedDays()));
    }

    @Override
    public int deleteHrLeaveBalanceByIds(Long[] balanceIds) { return hrLeaveBalanceMapper.deleteHrLeaveBalanceByIds(balanceIds); }

    @Override
    public int deleteHrLeaveBalanceById(Long balanceId) { return hrLeaveBalanceMapper.deleteHrLeaveBalanceById(balanceId); }
}
