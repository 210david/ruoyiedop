package com.ruoyi.wms.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsMaterial;
import com.ruoyi.wms.mapper.WmsMaterialMapper;
import com.ruoyi.wms.service.IWmsMaterialService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class WmsMaterialServiceImpl implements IWmsMaterialService
{
    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

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
        if (StringUtils.isEmpty(material.getMaterialCode()))
        {
            material.setMaterialCode(generateMaterialCode(material.getMaterialType()));
        }
        if (!checkMaterialCodeUnique(material))
        {
            throw new ServiceException("新增物料'" + material.getMaterialName() + "'失败，物料编码已存在");
        }
        material.setDelFlag("0");
        return wmsMaterialMapper.insertMaterial(material);
    }

    /**
     * 生成物料编码（支持动态前缀）
     * 通过传入 materialType 参数，由编号规则服务根据配置的动态前缀映射决定前缀
     */
    private String generateMaterialCode(String materialType)
    {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(materialType))
        {
            params.put("materialType", materialType);
        }
        return mkNumberRuleService.generateNumber("wms_material", params);
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
