package com.ruoyi.oa.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.oa.domain.OaVehicle;

public interface IOaVehicleService
{
    public OaVehicle selectOaVehicleById(Long vehicleId);

    public List<OaVehicle> selectOaVehicleList(OaVehicle oaVehicle);

    public AjaxResult importVehicle(List<OaVehicle> vehicleList, boolean updateSupport, String operName);

    public int insertOaVehicle(OaVehicle oaVehicle);

    public int updateOaVehicle(OaVehicle oaVehicle);

    public int deleteOaVehicleByIds(Long[] vehicleIds);

    public int deleteOaVehicleById(Long vehicleId);
}
