package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrLeaveBalanceLog;
import com.ruoyi.hr.mapper.HrLeaveBalanceLogMapper;
import com.ruoyi.hr.service.IHrLeaveBalanceLogService;

@Service
public class HrLeaveBalanceLogServiceImpl implements IHrLeaveBalanceLogService
{
    @Autowired
    private HrLeaveBalanceLogMapper hrLeaveBalanceLogMapper;

    @Override
    public List<HrLeaveBalanceLog> selectHrLeaveBalanceLogList(HrLeaveBalanceLog hrLeaveBalanceLog) {
        return hrLeaveBalanceLogMapper.selectHrLeaveBalanceLogList(hrLeaveBalanceLog);
    }

    @Override
    public int insertHrLeaveBalanceLog(HrLeaveBalanceLog hrLeaveBalanceLog) {
        return hrLeaveBalanceLogMapper.insertHrLeaveBalanceLog(hrLeaveBalanceLog);
    }
}
