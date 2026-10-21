package com.ruoyi.safety.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.safety.domain.SafetyRiskPoint;

public interface ISafetyRiskPointService {
    public SafetyRiskPoint selectSafetyRiskPointById(Long riskPointId);
    public List<SafetyRiskPoint> selectSafetyRiskPointList(SafetyRiskPoint safetyRiskPoint);
    public int insertSafetyRiskPoint(SafetyRiskPoint safetyRiskPoint);
    public int updateSafetyRiskPoint(SafetyRiskPoint safetyRiskPoint);
    public int deleteSafetyRiskPointByIds(Long[] riskPointIds);
    public int deleteSafetyRiskPointById(Long riskPointId);
    public List<Map<String, Object>> selectFourColorMapData(Long enterpriseId);
    public int countByRiskLevel(String riskLevel);
}