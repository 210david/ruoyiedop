package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsShift;

/**
 * 班次 Service接口
 *
 * @author ruoyi
 */
public interface IMmsShiftService
{
    public List<MmsShift> selectShiftList(MmsShift shift);
    public MmsShift selectShiftById(Long ShiftId);
    public int insertShift(MmsShift shift);
    public int updateShift(MmsShift shift);
    public int deleteShiftByIds(Long[] ShiftIds);
}
