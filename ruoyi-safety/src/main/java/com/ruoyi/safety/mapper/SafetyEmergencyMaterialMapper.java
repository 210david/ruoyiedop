package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyEmergencyMaterial;

public interface SafetyEmergencyMaterialMapper {
    public SafetyEmergencyMaterial selectSafetyEmergencyMaterialById(Long materialId);
    public List<SafetyEmergencyMaterial> selectSafetyEmergencyMaterialList(SafetyEmergencyMaterial safetyEmergencyMaterial);
    public int insertSafetyEmergencyMaterial(SafetyEmergencyMaterial safetyEmergencyMaterial);
    public int updateSafetyEmergencyMaterial(SafetyEmergencyMaterial safetyEmergencyMaterial);
    public int deleteSafetyEmergencyMaterialByIds(Long[] materialIds);
    public int deleteSafetyEmergencyMaterialById(Long materialId);
}