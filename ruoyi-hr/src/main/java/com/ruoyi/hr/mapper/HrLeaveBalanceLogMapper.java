package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrLeaveBalanceLog;

public interface HrLeaveBalanceLogMapper {
    public List<HrLeaveBalanceLog> selectHrLeaveBalanceLogList(HrLeaveBalanceLog hrLeaveBalanceLog);
    public int insertHrLeaveBalanceLog(HrLeaveBalanceLog hrLeaveBalanceLog);
}
