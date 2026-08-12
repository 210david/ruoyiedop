package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.safety.domain.SafetyMaterial;
import com.ruoyi.safety.mapper.SafetyMaterialMapper;
import com.ruoyi.safety.service.ISafetyMaterialService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class SafetyMaterialServiceImpl implements ISafetyMaterialService
{
    @Autowired
    private SafetyMaterialMapper safetyMaterialMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyMaterial selectSafetyMaterialById(Long materialId) { return safetyMaterialMapper.selectSafetyMaterialById(materialId); }

    @Override
    public List<SafetyMaterial> selectSafetyMaterialList(SafetyMaterial safetyMaterial) { return safetyMaterialMapper.selectSafetyMaterialList(safetyMaterial); }

    @Override
    public int insertSafetyMaterial(SafetyMaterial safetyMaterial)
    {
        if (StringUtils.isEmpty(safetyMaterial.getMaterialCode()))
        {
            safetyMaterial.setMaterialCode(mkNumberRuleService.generateNumber("safety_material"));
        }
        if (!checkMaterialCodeUnique(safetyMaterial))
        {
            throw new ServiceException("新增危化品'" + safetyMaterial.getMaterialName() + "'失败，危化品编号已存在");
        }
        return safetyMaterialMapper.insertSafetyMaterial(safetyMaterial);
    }

    @Override
    public int updateSafetyMaterial(SafetyMaterial safetyMaterial)
    {
        if (!checkMaterialCodeUnique(safetyMaterial))
        {
            throw new ServiceException("修改危化品'" + safetyMaterial.getMaterialName() + "'失败，危化品编号已存在");
        }
        return safetyMaterialMapper.updateSafetyMaterial(safetyMaterial);
    }

    @Override
    public int deleteSafetyMaterialByIds(Long[] materialIds) { return safetyMaterialMapper.deleteSafetyMaterialByIds(materialIds); }

    @Override
    public int deleteSafetyMaterialById(Long materialId) { return safetyMaterialMapper.deleteSafetyMaterialById(materialId); }

    private boolean checkMaterialCodeUnique(SafetyMaterial material)
    {
        Long materialId = material.getMaterialId() == null ? -1L : material.getMaterialId();
        SafetyMaterial info = safetyMaterialMapper.selectSafetyMaterialByCode(material.getMaterialCode());
        if (info != null && info.getMaterialId().longValue() != materialId.longValue())
        {
            return false;
        }
        return true;
    }
}
