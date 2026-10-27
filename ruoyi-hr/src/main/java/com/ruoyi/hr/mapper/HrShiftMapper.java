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
    /** 查询默认班次 */
    public HrShift selectDefaultShift();
    /** 清除其他班次的默认标记 */
    public int clearDefaultFlag(Long shiftId);
}
