package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyRectify;

public interface SafetyRectifyMapper {
    public SafetyRectify selectSafetyRectifyById(Long rectifyId);
    public List<SafetyRectify> selectSafetyRectifyList(SafetyRectify safetyRectify);
    public int insertSafetyRectify(SafetyRectify safetyRectify);
    public int updateSafetyRectify(SafetyRectify safetyRectify);
    public int deleteSafetyRectifyByIds(Long[] rectifyIds);
    public int deleteSafetyRectifyById(Long rectifyId);
}