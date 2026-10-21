package com.ruoyi.safety.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.safety.domain.SafetyMaterial;

public interface SafetyMaterialMapper {
    public SafetyMaterial selectSafetyMaterialById(Long materialId);
    public SafetyMaterial selectSafetyMaterialByCode(String materialCode);
    public List<SafetyMaterial> selectSafetyMaterialList(SafetyMaterial safetyMaterial);
    public int insertSafetyMaterial(SafetyMaterial safetyMaterial);
    public int updateSafetyMaterial(SafetyMaterial safetyMaterial);
    public int deleteSafetyMaterialByIds(Long[] materialIds);
    public int deleteSafetyMaterialById(Long materialId);

    /** 库存预警查询 */
    public List<SafetyMaterial> selectStockAlertList();

    /** 更新库存（供出入库模块调用） */
    public int updateCurrentStock(Map<String, Object> params);
}