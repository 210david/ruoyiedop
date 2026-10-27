package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyTrainingCert;

public interface ISafetyTrainingCertService {
    public SafetyTrainingCert selectSafetyTrainingCertById(Long certId);
    public List<SafetyTrainingCert> selectSafetyTrainingCertList(SafetyTrainingCert safetyTrainingCert);
    public int insertSafetyTrainingCert(SafetyTrainingCert safetyTrainingCert);
    public int updateSafetyTrainingCert(SafetyTrainingCert safetyTrainingCert);
    public int deleteSafetyTrainingCertByIds(Long[] certIds);
    public int deleteSafetyTrainingCertById(Long certId);
}
