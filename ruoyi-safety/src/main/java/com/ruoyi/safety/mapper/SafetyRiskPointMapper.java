package com.ruoyi.safety.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.safety.domain.SafetyRiskPoint;

public interface SafetyRiskPointMapper {
    public SafetyRiskPoint selectSafetyRiskPointById(Long riskPointId);
    public List<SafetyRiskPoint> selectSafetyRiskPointList(SafetyRiskPoint safetyRiskPoint);
    public int insertSafetyRiskPoint(SafetyRiskPoint safetyRiskPoint);
    public int updateSafetyRiskPoint(SafetyRiskPoint safetyRiskPoint);
    public int deleteSafetyRiskPointByIds(Long[] riskPointIds);
    public int deleteSafetyRiskPointById(Long riskPointId);
public int countRiskPointByAreaId(Long areaId);
public int countHazardByRiskPointId(Long riskPointId);
public int countTaskByRiskPointId(Long riskPointId);
    public List<Map<String, Object>> selectFourColorMapData(Long enterpriseId);
    public int countByRiskLevel(String riskLevel);
}