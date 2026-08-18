package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsShift;

public interface MmsShiftMapper {
    public List<MmsShift> selectShiftList(MmsShift shift);
    public MmsShift selectShiftById(Long shiftId);
    public int insertShift(MmsShift shift);
    public int updateShift(MmsShift shift);
    public int deleteShiftByIds(Long[] shiftIds);
}