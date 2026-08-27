package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrAttendanceMonthly;

public interface HrAttendanceMonthlyMapper {
    public HrAttendanceMonthly selectHrAttendanceMonthlyById(Long monthlyId);
    public List<HrAttendanceMonthly> selectHrAttendanceMonthlyList(HrAttendanceMonthly hrAttendanceMonthly);
    public int insertHrAttendanceMonthly(HrAttendanceMonthly hrAttendanceMonthly);
    public int updateHrAttendanceMonthly(HrAttendanceMonthly hrAttendanceMonthly);
    public int deleteHrAttendanceMonthlyByIds(Long[] monthlyIds);
    public int deleteHrAttendanceMonthlyById(Long monthlyId);
}
