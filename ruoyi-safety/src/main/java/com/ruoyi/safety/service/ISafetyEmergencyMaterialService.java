package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyEmergencyMaterial;

public interface ISafetyEmergencyMaterialService {
    public SafetyEmergencyMaterial selectSafetyEmergencyMaterialById(Long materialId);
    public List<SafetyEmergencyMaterial> selectSafetyEmergencyMaterialList(SafetyEmergencyMaterial safetyEmergencyMaterial);
    public int insertSafetyEmergencyMaterial(SafetyEmergencyMaterial safetyEmergencyMaterial);
    public int updateSafetyEmergencyMaterial(SafetyEmergencyMaterial safetyEmergencyMaterial);
    public int deleteSafetyEmergencyMaterialByIds(Long[] materialIds);
    public int deleteSafetyEmergencyMaterialById(Long materialId);
}