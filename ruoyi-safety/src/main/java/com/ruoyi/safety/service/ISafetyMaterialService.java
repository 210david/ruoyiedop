package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyMaterial;

public interface ISafetyMaterialService {
    public SafetyMaterial selectSafetyMaterialById(Long materialId);
    public List<SafetyMaterial> selectSafetyMaterialList(SafetyMaterial safetyMaterial);
    public int insertSafetyMaterial(SafetyMaterial safetyMaterial);
    public int updateSafetyMaterial(SafetyMaterial safetyMaterial);
    public int deleteSafetyMaterialByIds(Long[] materialIds);
    public int deleteSafetyMaterialById(Long materialId);

    /** 库存预警查询 */
    public List<SafetyMaterial> selectStockAlertList();

    /** 更新库存（供出入库模块调用） */
    public int updateCurrentStock(Long materialId, java.math.BigDecimal afterStock, String updateBy);
}