package com.ruoyi.wms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.wms.domain.WmsMaterial;
import com.ruoyi.wms.mapper.WmsMaterialMapper;
import com.ruoyi.wms.service.IWmsMaterialService;

@Service
public class WmsMaterialServiceImpl implements IWmsMaterialService
{
    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Override
    public List<WmsMaterial> selectMaterialList(WmsMaterial material)
    {
        return wmsMaterialMapper.selectMaterialList(material);
    }

    @Override
    public WmsMaterial selectMaterialById(Long materialId)
    {
        return wmsMaterialMapper.selectMaterialById(materialId);
    }

    @Override
    public WmsMaterial selectMaterialByCode(String materialCode)
    {
        return wmsMaterialMapper.selectMaterialByCode(materialCode);
    }

    @Override
    public int insertMaterial(WmsMaterial material)
    {
        if (!checkMaterialCodeUnique(material))
        {
            throw new ServiceException("新增物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        material.setDelFlag("0");
        return wmsMaterialMapper.insertMaterial(material);
    }

    @Override
    public int updateMaterial(WmsMaterial material)
    {
        if (!checkMaterialCodeUnique(material))
        {
            throw new ServiceException("修改物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        return wmsMaterialMapper.updateMaterial(material);
    }

    @Override
    public int deleteMaterialByIds(Long[] materialIds)
    {
        return wmsMaterialMapper.deleteMaterialByIds(materialIds);
    }

    @Override
    public boolean checkMaterialCodeUnique(WmsMaterial material)
    {
        Long materialId = material.getMaterialId() == null ? -1L : material.getMaterialId();
        WmsMaterial info = wmsMaterialMapper.selectMaterialByCode(material.getMaterialCode());
        if (info != null && info.getMaterialId().longValue() != materialId.longValue())
        {
            return false;
        }
        return true;
    }
}
