package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.safety.domain.SafetyEnterprise;
import com.ruoyi.safety.mapper.SafetyEnterpriseMapper;
import com.ruoyi.safety.service.ISafetyEnterpriseService;

@Service
public class SafetyEnterpriseServiceImpl implements ISafetyEnterpriseService
{
    @Autowired
    private SafetyEnterpriseMapper safetyEnterpriseMapper;

    @Override
    public SafetyEnterprise selectSafetyEnterpriseById(Long enterpriseId)
    {
        return safetyEnterpriseMapper.selectSafetyEnterpriseById(enterpriseId);
    }

    @Override
    public List<SafetyEnterprise> selectSafetyEnterpriseList(SafetyEnterprise safetyEnterprise)
    {
        return safetyEnterpriseMapper.selectSafetyEnterpriseList(safetyEnterprise);
    }

    @Override
    public int insertSafetyEnterprise(SafetyEnterprise safetyEnterprise)
    {
        return safetyEnterpriseMapper.insertSafetyEnterprise(safetyEnterprise);
    }

    @Override
    public int updateSafetyEnterprise(SafetyEnterprise safetyEnterprise)
    {
        return safetyEnterpriseMapper.updateSafetyEnterprise(safetyEnterprise);
    }

    @Override
    public int deleteSafetyEnterpriseByIds(Long[] enterpriseIds)
    {
        return safetyEnterpriseMapper.deleteSafetyEnterpriseByIds(enterpriseIds);
    }

    @Override
    public int deleteSafetyEnterpriseById(Long enterpriseId)
    {
        return safetyEnterpriseMapper.deleteSafetyEnterpriseById(enterpriseId);
    }
}