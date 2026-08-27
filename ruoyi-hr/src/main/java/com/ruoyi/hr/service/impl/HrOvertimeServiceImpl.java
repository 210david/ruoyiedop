package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrOvertime;
import com.ruoyi.hr.mapper.HrOvertimeMapper;
import com.ruoyi.hr.service.IHrOvertimeService;

@Service
public class HrOvertimeServiceImpl implements IHrOvertimeService
{
    @Autowired
    private HrOvertimeMapper hrOvertimeMapper;

    @Override
    public HrOvertime selectHrOvertimeById(Long overtimeId) { return hrOvertimeMapper.selectHrOvertimeById(overtimeId); }

    @Override
    public List<HrOvertime> selectHrOvertimeList(HrOvertime hrOvertime) { return hrOvertimeMapper.selectHrOvertimeList(hrOvertime); }

    @Override
    public int insertHrOvertime(HrOvertime hrOvertime) { return hrOvertimeMapper.insertHrOvertime(hrOvertime); }

    @Override
    public int updateHrOvertime(HrOvertime hrOvertime) { return hrOvertimeMapper.updateHrOvertime(hrOvertime); }

    @Override
    public int deleteHrOvertimeByIds(Long[] overtimeIds) { return hrOvertimeMapper.deleteHrOvertimeByIds(overtimeIds); }

    @Override
    public int deleteHrOvertimeById(Long overtimeId) { return hrOvertimeMapper.deleteHrOvertimeById(overtimeId); }
}
