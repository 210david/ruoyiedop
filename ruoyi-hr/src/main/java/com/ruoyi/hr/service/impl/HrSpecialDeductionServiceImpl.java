package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrSpecialDeduction;
import com.ruoyi.hr.mapper.HrSpecialDeductionMapper;
import com.ruoyi.hr.service.IHrSpecialDeductionService;

@Service
public class HrSpecialDeductionServiceImpl implements IHrSpecialDeductionService
{
    @Autowired
    private HrSpecialDeductionMapper hrSpecialDeductionMapper;

    @Override
    public HrSpecialDeduction selectHrSpecialDeductionById(Long sdId) { return hrSpecialDeductionMapper.selectHrSpecialDeductionById(sdId); }

    @Override
    public List<HrSpecialDeduction> selectHrSpecialDeductionList(HrSpecialDeduction hrSpecialDeduction) { return hrSpecialDeductionMapper.selectHrSpecialDeductionList(hrSpecialDeduction); }

    @Override
    public int insertHrSpecialDeduction(HrSpecialDeduction hrSpecialDeduction) { return hrSpecialDeductionMapper.insertHrSpecialDeduction(hrSpecialDeduction); }

    @Override
    public int updateHrSpecialDeduction(HrSpecialDeduction hrSpecialDeduction) { return hrSpecialDeductionMapper.updateHrSpecialDeduction(hrSpecialDeduction); }

    @Override
    public int deleteHrSpecialDeductionByIds(Long[] sdIds) { return hrSpecialDeductionMapper.deleteHrSpecialDeductionByIds(sdIds); }

    @Override
    public int deleteHrSpecialDeductionById(Long sdId) { return hrSpecialDeductionMapper.deleteHrSpecialDeductionById(sdId); }
}
