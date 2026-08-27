package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrPayslip;
import com.ruoyi.hr.mapper.HrPayslipMapper;
import com.ruoyi.hr.service.IHrPayslipService;

@Service
public class HrPayslipServiceImpl implements IHrPayslipService
{
    @Autowired
    private HrPayslipMapper hrPayslipMapper;

    @Override
    public HrPayslip selectHrPayslipById(Long payslipId) { return hrPayslipMapper.selectHrPayslipById(payslipId); }

    @Override
    public List<HrPayslip> selectHrPayslipList(HrPayslip hrPayslip) { return hrPayslipMapper.selectHrPayslipList(hrPayslip); }

    @Override
    public int insertHrPayslip(HrPayslip hrPayslip) { return hrPayslipMapper.insertHrPayslip(hrPayslip); }

    @Override
    public int updateHrPayslip(HrPayslip hrPayslip) { return hrPayslipMapper.updateHrPayslip(hrPayslip); }

    @Override
    public int deleteHrPayslipByIds(Long[] payslipIds) { return hrPayslipMapper.deleteHrPayslipByIds(payslipIds); }

    @Override
    public int deleteHrPayslipById(Long payslipId) { return hrPayslipMapper.deleteHrPayslipById(payslipId); }
}
