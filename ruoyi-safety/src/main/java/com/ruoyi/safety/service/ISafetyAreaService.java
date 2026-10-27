package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyArea;

public interface ISafetyAreaService {
    public SafetyArea selectSafetyAreaById(Long areaId);
    public List<SafetyArea> selectSafetyAreaList(SafetyArea safetyArea);
    public int insertSafetyArea(SafetyArea safetyArea);
    public int updateSafetyArea(SafetyArea safetyArea);
    public int deleteSafetyAreaByIds(Long[] areaIds);
    public int deleteSafetyAreaById(Long areaId);
    public boolean checkAreaExistChild(Long areaId);
}