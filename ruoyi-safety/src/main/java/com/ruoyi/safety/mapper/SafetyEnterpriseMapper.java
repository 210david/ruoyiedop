package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyEnterprise;

public interface SafetyEnterpriseMapper {
    public SafetyEnterprise selectSafetyEnterpriseById(Long enterpriseId);
    public List<SafetyEnterprise> selectSafetyEnterpriseList(SafetyEnterprise safetyEnterprise);
    public int insertSafetyEnterprise(SafetyEnterprise safetyEnterprise);
    public int updateSafetyEnterprise(SafetyEnterprise safetyEnterprise);
    public int deleteSafetyEnterpriseByIds(Long[] enterpriseIds);
    public int deleteSafetyEnterpriseById(Long enterpriseId);
}