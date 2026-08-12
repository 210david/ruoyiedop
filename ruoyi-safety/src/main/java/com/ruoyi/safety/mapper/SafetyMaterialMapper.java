package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyMaterial;

public interface SafetyMaterialMapper {
    public SafetyMaterial selectSafetyMaterialById(Long materialId);
    public SafetyMaterial selectSafetyMaterialByCode(String materialCode);
    public List<SafetyMaterial> selectSafetyMaterialList(SafetyMaterial safetyMaterial);
    public int insertSafetyMaterial(SafetyMaterial safetyMaterial);
    public int updateSafetyMaterial(SafetyMaterial safetyMaterial);
    public int deleteSafetyMaterialByIds(Long[] materialIds);
    public int deleteSafetyMaterialById(Long materialId);
}