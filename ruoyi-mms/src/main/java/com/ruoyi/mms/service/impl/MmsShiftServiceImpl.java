package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mms.domain.MmsShift;
import com.ruoyi.mms.mapper.MmsShiftMapper;
import com.ruoyi.mms.service.IMmsShiftService;

/**
 * 班次 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsShiftServiceImpl implements IMmsShiftService
{
    @Autowired
    private MmsShiftMapper shiftMapper;

    @Override
    public List<MmsShift> selectShiftList(MmsShift shift)
    {
        return shiftMapper.selectShiftList(shift);
    }

    @Override
    public MmsShift selectShiftById(Long ShiftId)
    {
        return shiftMapper.selectShiftById(ShiftId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertShift(MmsShift shift)
    {
        shift.setDelFlag("0");
        return shiftMapper.insertShift(shift);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateShift(MmsShift shift)
    {
        return shiftMapper.updateShift(shift);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteShiftByIds(Long[] ShiftIds)
    {
        return shiftMapper.deleteShiftByIds(ShiftIds);
    }
}
