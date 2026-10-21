package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.safety.domain.SafetyCert;
import com.ruoyi.safety.mapper.SafetyCertMapper;
import com.ruoyi.safety.service.ISafetyCertService;

@Service
public class SafetyCertServiceImpl implements ISafetyCertService
{
    @Autowired
    private SafetyCertMapper safetyCertMapper;

    @Override
    public SafetyCert selectSafetyCertById(Long certId) { return safetyCertMapper.selectSafetyCertById(certId); }

    @Override
    public List<SafetyCert> selectSafetyCertList(SafetyCert safetyCert) { return safetyCertMapper.selectSafetyCertList(safetyCert); }

    @Override
    public int insertSafetyCert(SafetyCert safetyCert) {
        return safetyCertMapper.insertSafetyCert(safetyCert); }

    @Override
    public int updateSafetyCert(SafetyCert safetyCert) { return safetyCertMapper.updateSafetyCert(safetyCert); }

    @Override
    public int deleteSafetyCertByIds(Long[] certIds) { return safetyCertMapper.deleteSafetyCertByIds(certIds); }

    @Override
    public int deleteSafetyCertById(Long certId) { return safetyCertMapper.deleteSafetyCertById(certId); }
}