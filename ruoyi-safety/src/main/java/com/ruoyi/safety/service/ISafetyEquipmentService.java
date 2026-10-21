package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyEquipment;

public interface ISafetyEquipmentService {
    public SafetyEquipment selectSafetyEquipmentById(Long equipmentId);
    public List<SafetyEquipment> selectSafetyEquipmentList(SafetyEquipment safetyEquipment);
    public int insertSafetyEquipment(SafetyEquipment safetyEquipment);
    public int updateSafetyEquipment(SafetyEquipment safetyEquipment);
    public int deleteSafetyEquipmentByIds(Long[] equipmentIds);
    public int deleteSafetyEquipmentById(Long equipmentId);
}