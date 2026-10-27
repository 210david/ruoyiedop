package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkMaterial;
import com.ruoyi.mk.mapper.MkMaterialMapper;
import com.ruoyi.mk.service.IMkMaterialService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 素材库 Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MkMaterialServiceImpl implements IMkMaterialService
{
    @Autowired
    private MkMaterialMapper mkMaterialMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkMaterial> selectMaterialList(MkMaterial material)
    {
        return mkMaterialMapper.selectMaterialList(material);
    }

    @Override
    public MkMaterial selectMaterialById(Long materialId)
    {
        return mkMaterialMapper.selectMaterialById(materialId);
    }

    @Override
    public int insertMaterial(MkMaterial material)
    {
        if (StringUtils.isEmpty(material.getMaterialNo()))
        {
            material.setMaterialNo(mkNumberRuleService.generateNumber("material"));
        }
        material.setUserId(SecurityUtils.getUserId());
        material.setCreateBy(SecurityUtils.getUsername());
        return mkMaterialMapper.insertMaterial(material);
    }

    @Override
    public int updateMaterial(MkMaterial material)
    {
        material.setUpdateBy(SecurityUtils.getUsername());
        return mkMaterialMapper.updateMaterial(material);
    }

    @Override
    public int deleteMaterialByIds(Long[] materialIds)
    {
        return mkMaterialMapper.deleteMaterialByIds(materialIds);
    }

    @Override
    public int increaseUseCount(Long[] materialIds)
    {
        if (materialIds == null || materialIds.length == 0)
        {
            return 0;
        }
        return mkMaterialMapper.updateUseCount(materialIds, 1);
    }

    @Override
    public int decreaseUseCount(Long[] materialIds)
    {
        if (materialIds == null || materialIds.length == 0)
        {
            return 0;
        }
        return mkMaterialMapper.updateUseCount(materialIds, -1);
    }
}
