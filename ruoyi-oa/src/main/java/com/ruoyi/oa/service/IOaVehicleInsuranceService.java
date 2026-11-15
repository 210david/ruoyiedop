package com.ruoyi.oa.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.oa.domain.OaVehicleInsurance;

public interface IOaVehicleInsuranceService
{
    public OaVehicleInsurance selectOaVehicleInsuranceById(Long insuranceId);

    public List<OaVehicleInsurance> selectOaVehicleInsuranceList(OaVehicleInsurance oaVehicleInsurance);

    public AjaxResult importInsurance(List<OaVehicleInsurance> insuranceList, boolean updateSupport, String operName);

    public int insertOaVehicleInsurance(OaVehicleInsurance oaVehicleInsurance);

    public int updateOaVehicleInsurance(OaVehicleInsurance oaVehicleInsurance);

    public int deleteOaVehicleInsuranceByIds(Long[] insuranceIds);

    public int deleteOaVehicleInsuranceById(Long insuranceId);
}
