package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrOvertime;
import com.ruoyi.hr.mapper.HrOvertimeMapper;
import com.ruoyi.hr.service.IHrOvertimeService;

@Service
public class HrOvertimeServiceImpl implements IHrOvertimeService
{
    @Autowired
    private HrOvertimeMapper hrOvertimeMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrOvertime selectHrOvertimeById(Long overtimeId) { return hrOvertimeMapper.selectHrOvertimeById(overtimeId); }

    @Override
    public List<HrOvertime> selectHrOvertimeList(HrOvertime hrOvertime) { return hrOvertimeMapper.selectHrOvertimeList(hrOvertime); }

    @Override
    public int insertHrOvertime(HrOvertime hrOvertime)
    {
        if (StringUtils.isEmpty(hrOvertime.getOvertimeNo()))
        {
            hrOvertime.setOvertimeNo(mkNumberRuleService.generateNumber("hr_overtime"));
        }
        computeOvertimeHours(hrOvertime);
        return hrOvertimeMapper.insertHrOvertime(hrOvertime);
    }

    @Override
    public int updateHrOvertime(HrOvertime hrOvertime)
    {
        computeOvertimeHours(hrOvertime);
        return hrOvertimeMapper.updateHrOvertime(hrOvertime);
    }

    /** 根据开始/结束时间自动计算加班小时（服务端兑底，保证 API 一致性） */
    private void computeOvertimeHours(HrOvertime overtime)
    {
        if (overtime.getStartTime() == null || overtime.getEndTime() == null
                || !overtime.getEndTime().after(overtime.getStartTime()))
        {
            return;
        }
        long millis = overtime.getEndTime().getTime() - overtime.getStartTime().getTime();
        overtime.setOvertimeHours(java.math.BigDecimal.valueOf(Math.round(millis / 3600000.0 * 100.0) / 100.0));
    }

    @Override
    public int deleteHrOvertimeByIds(Long[] overtimeIds) { return hrOvertimeMapper.deleteHrOvertimeByIds(overtimeIds); }

    @Override
    public int deleteHrOvertimeById(Long overtimeId) { return hrOvertimeMapper.deleteHrOvertimeById(overtimeId); }
}
