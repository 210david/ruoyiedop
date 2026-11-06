package com.ruoyi.safety.service;
import java.util.List;
import com.ruoyi.safety.domain.SafetyVehicle;

public interface ISafetyVehicleService {
    public SafetyVehicle selectSafetyVehicleById(Long vehicleId);
    public List<SafetyVehicle> selectSafetyVehicleList(SafetyVehicle vehicle);
    public int insertSafetyVehicle(SafetyVehicle vehicle);
    public int updateSafetyVehicle(SafetyVehicle vehicle);
    public int deleteSafetyVehicleByIds(Long[] vehicleIds);
    public int deleteSafetyVehicleById(Long vehicleId);
}
