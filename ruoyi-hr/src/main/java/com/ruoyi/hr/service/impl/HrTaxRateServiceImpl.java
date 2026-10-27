package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrTaxRate;
import com.ruoyi.hr.mapper.HrTaxRateMapper;
import com.ruoyi.hr.service.IHrTaxRateService;

@Service
public class HrTaxRateServiceImpl implements IHrTaxRateService
{
    @Autowired
    private HrTaxRateMapper hrTaxRateMapper;

    @Override
    public HrTaxRate selectHrTaxRateById(Long taxRateId) { return hrTaxRateMapper.selectHrTaxRateById(taxRateId); }

    @Override
    public List<HrTaxRate> selectHrTaxRateList(HrTaxRate hrTaxRate) { return hrTaxRateMapper.selectHrTaxRateList(hrTaxRate); }

    @Override
    public int insertHrTaxRate(HrTaxRate hrTaxRate) { return hrTaxRateMapper.insertHrTaxRate(hrTaxRate); }

    @Override
    public int updateHrTaxRate(HrTaxRate hrTaxRate) { return hrTaxRateMapper.updateHrTaxRate(hrTaxRate); }

    @Override
    public int deleteHrTaxRateByIds(Long[] taxRateIds) { return hrTaxRateMapper.deleteHrTaxRateByIds(taxRateIds); }

    @Override
    public int deleteHrTaxRateById(Long taxRateId) { return hrTaxRateMapper.deleteHrTaxRateById(taxRateId); }
}
