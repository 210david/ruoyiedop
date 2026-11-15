package com.ruoyi.oa.mapper;

import java.util.List;
import com.ruoyi.oa.domain.OaVehicleInsurance;

public interface OaVehicleInsuranceMapper
{
    public OaVehicleInsurance selectOaVehicleInsuranceById(Long insuranceId);

    public OaVehicleInsurance selectOaVehicleInsuranceByPolicyNo(String policyNo);

    public List<OaVehicleInsurance> selectOaVehicleInsuranceList(OaVehicleInsurance oaVehicleInsurance);

    public int insertOaVehicleInsurance(OaVehicleInsurance oaVehicleInsurance);

    public int updateOaVehicleInsurance(OaVehicleInsurance oaVehicleInsurance);

    public int deleteOaVehicleInsuranceByIds(Long[] insuranceIds);

    public int deleteOaVehicleInsuranceById(Long insuranceId);
}
