package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrShift;

public interface HrShiftMapper {
    public HrShift selectHrShiftById(Long shiftId);
    public List<HrShift> selectHrShiftList(HrShift hrShift);
    public int insertHrShift(HrShift hrShift);
    public int updateHrShift(HrShift hrShift);
    public int deleteHrShiftByIds(Long[] shiftIds);
    public int deleteHrShiftById(Long shiftId);
}
