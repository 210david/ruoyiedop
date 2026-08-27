package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrLeaveBalance;

public interface HrLeaveBalanceMapper {
    public HrLeaveBalance selectHrLeaveBalanceById(Long balanceId);
    public List<HrLeaveBalance> selectHrLeaveBalanceList(HrLeaveBalance hrLeaveBalance);
    public int insertHrLeaveBalance(HrLeaveBalance hrLeaveBalance);
    public int updateHrLeaveBalance(HrLeaveBalance hrLeaveBalance);
    public int deleteHrLeaveBalanceByIds(Long[] balanceIds);
    public int deleteHrLeaveBalanceById(Long balanceId);
}
