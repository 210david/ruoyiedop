package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.safety.domain.SafetyTrainingCert;
import com.ruoyi.safety.mapper.SafetyTrainingCertMapper;
import com.ruoyi.safety.service.ISafetyTrainingCertService;

@Service
public class SafetyTrainingCertServiceImpl implements ISafetyTrainingCertService
{
    @Autowired
    private SafetyTrainingCertMapper safetyTrainingCertMapper;

    @Override
    public SafetyTrainingCert selectSafetyTrainingCertById(Long certId) { return safetyTrainingCertMapper.selectSafetyTrainingCertById(certId); }

    @Override
    public List<SafetyTrainingCert> selectSafetyTrainingCertList(SafetyTrainingCert safetyTrainingCert) { return safetyTrainingCertMapper.selectSafetyTrainingCertList(safetyTrainingCert); }

    @Override
    public int insertSafetyTrainingCert(SafetyTrainingCert safetyTrainingCert) { return safetyTrainingCertMapper.insertSafetyTrainingCert(safetyTrainingCert); }

    @Override
    public int updateSafetyTrainingCert(SafetyTrainingCert safetyTrainingCert) { return safetyTrainingCertMapper.updateSafetyTrainingCert(safetyTrainingCert); }

    @Override
    public int deleteSafetyTrainingCertByIds(Long[] certIds) { return safetyTrainingCertMapper.deleteSafetyTrainingCertByIds(certIds); }

    @Override
    public int deleteSafetyTrainingCertById(Long certId) { return safetyTrainingCertMapper.deleteSafetyTrainingCertById(certId); }
}
