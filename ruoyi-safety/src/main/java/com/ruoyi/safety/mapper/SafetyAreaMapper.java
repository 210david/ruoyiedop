package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyArea;

public interface SafetyAreaMapper {
    public SafetyArea selectSafetyAreaById(Long areaId);
    public List<SafetyArea> selectSafetyAreaList(SafetyArea safetyArea);
    public int insertSafetyArea(SafetyArea safetyArea);
    public int updateSafetyArea(SafetyArea safetyArea);
    public int deleteSafetyAreaByIds(Long[] areaIds);
    public int deleteSafetyAreaById(Long areaId);
    public int countChildByParentId(Long parentId);
}