package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyCert;

public interface ISafetyCertService {
    public SafetyCert selectSafetyCertById(Long certId);
    public List<SafetyCert> selectSafetyCertList(SafetyCert safetyCert);
    public int insertSafetyCert(SafetyCert safetyCert);
    public int updateSafetyCert(SafetyCert safetyCert);
    public int deleteSafetyCertByIds(Long[] certIds);
    public int deleteSafetyCertById(Long certId);
}