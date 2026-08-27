package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrShift;
import com.ruoyi.hr.mapper.HrShiftMapper;
import com.ruoyi.hr.service.IHrShiftService;

@Service
public class HrShiftServiceImpl implements IHrShiftService
{
    @Autowired
    private HrShiftMapper hrShiftMapper;

    @Override
    public HrShift selectHrShiftById(Long shiftId) { return hrShiftMapper.selectHrShiftById(shiftId); }

    @Override
    public List<HrShift> selectHrShiftList(HrShift hrShift) { return hrShiftMapper.selectHrShiftList(hrShift); }

    @Override
    public int insertHrShift(HrShift hrShift) { return hrShiftMapper.insertHrShift(hrShift); }

    @Override
    public int updateHrShift(HrShift hrShift) { return hrShiftMapper.updateHrShift(hrShift); }

    @Override
    public int deleteHrShiftByIds(Long[] shiftIds) { return hrShiftMapper.deleteHrShiftByIds(shiftIds); }

    @Override
    public int deleteHrShiftById(Long shiftId) { return hrShiftMapper.deleteHrShiftById(shiftId); }
}
