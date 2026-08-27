package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrLeaveBalance;

public interface IHrLeaveBalanceService {
    public HrLeaveBalance selectHrLeaveBalanceById(Long balanceId);
    public List<HrLeaveBalance> selectHrLeaveBalanceList(HrLeaveBalance hrLeaveBalance);
    public int insertHrLeaveBalance(HrLeaveBalance hrLeaveBalance);
    public int updateHrLeaveBalance(HrLeaveBalance hrLeaveBalance);
    public int deleteHrLeaveBalanceByIds(Long[] balanceIds);
    public int deleteHrLeaveBalanceById(Long balanceId);
}
