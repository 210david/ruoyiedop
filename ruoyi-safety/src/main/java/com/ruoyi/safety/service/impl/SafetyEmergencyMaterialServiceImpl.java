package com.ruoyi.safety.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyEmergencyMaterial;
import com.ruoyi.safety.mapper.SafetyEmergencyMaterialMapper;
import com.ruoyi.safety.service.ISafetyEmergencyMaterialService;

@Service
public class SafetyEmergencyMaterialServiceImpl implements ISafetyEmergencyMaterialService
{
    @Autowired
    private SafetyEmergencyMaterialMapper safetyEmergencyMaterialMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyEmergencyMaterial selectSafetyEmergencyMaterialById(Long materialId) { return safetyEmergencyMaterialMapper.selectSafetyEmergencyMaterialById(materialId); }

    @Override
    public List<SafetyEmergencyMaterial> selectSafetyEmergencyMaterialList(SafetyEmergencyMaterial safetyEmergencyMaterial) { return safetyEmergencyMaterialMapper.selectSafetyEmergencyMaterialList(safetyEmergencyMaterial); }

    @Override
    public int insertSafetyEmergencyMaterial(SafetyEmergencyMaterial safetyEmergencyMaterial) {
        if (StringUtils.isEmpty(safetyEmergencyMaterial.getMaterialCode())) {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(safetyEmergencyMaterial.getMaterialType())) { params.put("materialType", safetyEmergencyMaterial.getMaterialType()); }
        safetyEmergencyMaterial.setMaterialCode(mkNumberRuleService.generateNumber("safety_emergency_material", params));
        }
        return safetyEmergencyMaterialMapper.insertSafetyEmergencyMaterial(safetyEmergencyMaterial); }

    @Override
    public int updateSafetyEmergencyMaterial(SafetyEmergencyMaterial safetyEmergencyMaterial) { return safetyEmergencyMaterialMapper.updateSafetyEmergencyMaterial(safetyEmergencyMaterial); }

    @Override
    public int deleteSafetyEmergencyMaterialByIds(Long[] materialIds) { return safetyEmergencyMaterialMapper.deleteSafetyEmergencyMaterialByIds(materialIds); }

    @Override
    public int deleteSafetyEmergencyMaterialById(Long materialId) { return safetyEmergencyMaterialMapper.deleteSafetyEmergencyMaterialById(materialId); }
}