package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyDrill;

public interface SafetyDrillMapper {
    public SafetyDrill selectSafetyDrillById(Long drillId);
    public List<SafetyDrill> selectSafetyDrillList(SafetyDrill safetyDrill);
    public int insertSafetyDrill(SafetyDrill safetyDrill);
    public int updateSafetyDrill(SafetyDrill safetyDrill);
    public int deleteSafetyDrillByIds(Long[] drillIds);
    public int deleteSafetyDrillById(Long drillId);
}