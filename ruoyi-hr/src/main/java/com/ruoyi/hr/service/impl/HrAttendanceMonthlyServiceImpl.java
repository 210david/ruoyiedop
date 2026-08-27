package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrAttendanceMonthly;
import com.ruoyi.hr.mapper.HrAttendanceMonthlyMapper;
import com.ruoyi.hr.service.IHrAttendanceMonthlyService;

@Service
public class HrAttendanceMonthlyServiceImpl implements IHrAttendanceMonthlyService
{
    @Autowired
    private HrAttendanceMonthlyMapper hrAttendanceMonthlyMapper;

    @Override
    public HrAttendanceMonthly selectHrAttendanceMonthlyById(Long monthlyId) { return hrAttendanceMonthlyMapper.selectHrAttendanceMonthlyById(monthlyId); }

    @Override
    public List<HrAttendanceMonthly> selectHrAttendanceMonthlyList(HrAttendanceMonthly hrAttendanceMonthly) { return hrAttendanceMonthlyMapper.selectHrAttendanceMonthlyList(hrAttendanceMonthly); }

    @Override
    public int insertHrAttendanceMonthly(HrAttendanceMonthly hrAttendanceMonthly) { return hrAttendanceMonthlyMapper.insertHrAttendanceMonthly(hrAttendanceMonthly); }

    @Override
    public int updateHrAttendanceMonthly(HrAttendanceMonthly hrAttendanceMonthly) { return hrAttendanceMonthlyMapper.updateHrAttendanceMonthly(hrAttendanceMonthly); }

    @Override
    public int deleteHrAttendanceMonthlyByIds(Long[] monthlyIds) { return hrAttendanceMonthlyMapper.deleteHrAttendanceMonthlyByIds(monthlyIds); }

    @Override
    public int deleteHrAttendanceMonthlyById(Long monthlyId) { return hrAttendanceMonthlyMapper.deleteHrAttendanceMonthlyById(monthlyId); }
}
