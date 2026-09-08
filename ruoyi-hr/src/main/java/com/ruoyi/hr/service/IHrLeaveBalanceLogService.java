package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrLeaveBalanceLog;

public interface IHrLeaveBalanceLogService {
    public List<HrLeaveBalanceLog> selectHrLeaveBalanceLogList(HrLeaveBalanceLog hrLeaveBalanceLog);
    public int insertHrLeaveBalanceLog(HrLeaveBalanceLog hrLeaveBalanceLog);
}
