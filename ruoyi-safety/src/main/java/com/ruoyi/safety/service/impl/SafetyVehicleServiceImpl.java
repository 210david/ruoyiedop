package com.ruoyi.safety.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.safety.mapper.SafetyVehicleMapper;
import com.ruoyi.safety.domain.SafetyVehicle;
import com.ruoyi.safety.service.ISafetyVehicleService;

@Service
public class SafetyVehicleServiceImpl implements ISafetyVehicleService
{
    @Autowired
    private SafetyVehicleMapper safetyVehicleMapper;

    @Override
    public SafetyVehicle selectSafetyVehicleById(Long vehicleId)
    {
        return safetyVehicleMapper.selectSafetyVehicleById(vehicleId);
    }

    @Override
    public List<SafetyVehicle> selectSafetyVehicleList(SafetyVehicle vehicle)
    {
        return safetyVehicleMapper.selectSafetyVehicleList(vehicle);
    }

    @Override
    public int insertSafetyVehicle(SafetyVehicle vehicle)
    {
        return safetyVehicleMapper.insertSafetyVehicle(vehicle);
    }

    @Override
    public int updateSafetyVehicle(SafetyVehicle vehicle)
    {
        return safetyVehicleMapper.updateSafetyVehicle(vehicle);
    }

    @Override
    public int deleteSafetyVehicleByIds(Long[] vehicleIds)
    {
        return safetyVehicleMapper.deleteSafetyVehicleByIds(vehicleIds);
    }

    @Override
    public int deleteSafetyVehicleById(Long vehicleId)
    {
        return safetyVehicleMapper.deleteSafetyVehicleById(vehicleId);
    }
}
