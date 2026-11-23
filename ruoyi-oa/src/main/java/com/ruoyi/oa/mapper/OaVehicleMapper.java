package com.ruoyi.oa.mapper;

import java.util.List;
import com.ruoyi.oa.domain.OaVehicle;

public interface OaVehicleMapper
{
    public OaVehicle selectOaVehicleById(Long vehicleId);

    public OaVehicle selectOaVehicleByPlateNo(String plateNo);

    public List<OaVehicle> selectOaVehicleList(OaVehicle oaVehicle);

    public int insertOaVehicle(OaVehicle oaVehicle);

    public int updateOaVehicle(OaVehicle oaVehicle);

    public int deleteOaVehicleByIds(Long[] vehicleIds);

    public int deleteOaVehicleById(Long vehicleId);

    /** 按部门名称精确匹配部门ID（同名部门可能返回多条，调用方需判定唯一性） */
    public java.util.List<Long> selectDeptIdsByExactName(String deptName);
}
