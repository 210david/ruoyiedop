package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrTaxRate;

public interface IHrTaxRateService {
    public HrTaxRate selectHrTaxRateById(Long taxRateId);
    public List<HrTaxRate> selectHrTaxRateList(HrTaxRate hrTaxRate);
    public int insertHrTaxRate(HrTaxRate hrTaxRate);
    public int updateHrTaxRate(HrTaxRate hrTaxRate);
    public int deleteHrTaxRateByIds(Long[] taxRateIds);
    public int deleteHrTaxRateById(Long taxRateId);
}
