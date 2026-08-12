package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.safety.domain.SafetyArea;
import com.ruoyi.safety.mapper.SafetyAreaMapper;
import com.ruoyi.safety.service.ISafetyAreaService;

@Service
public class SafetyAreaServiceImpl implements ISafetyAreaService
{
    @Autowired
    private SafetyAreaMapper safetyAreaMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public SafetyArea selectSafetyAreaById(Long areaId)
    {
        return safetyAreaMapper.selectSafetyAreaById(areaId);
    }

    @Override
    public List<SafetyArea> selectSafetyAreaList(SafetyArea safetyArea)
    {
        return safetyAreaMapper.selectSafetyAreaList(safetyArea);
    }

    @Override
    public int insertSafetyArea(SafetyArea safetyArea)
    {
        if (StringUtils.isEmpty(safetyArea.getAreaCode()))
        {
            safetyArea.setAreaCode(mkNumberRuleService.generateNumber("safety_area"));
        }
        return safetyAreaMapper.insertSafetyArea(safetyArea);
    }

    @Override
    public int updateSafetyArea(SafetyArea safetyArea)
    {
        return safetyAreaMapper.updateSafetyArea(safetyArea);
    }

    @Override
    public int deleteSafetyAreaByIds(Long[] areaIds)
    {
        for (Long areaId : areaIds)
        {
            if (safetyAreaMapper.countChildByParentId(areaId) > 0)
            {
                throw new ServiceException("删除失败，存在子区域，请先删除子区域");
            }
        }
        return safetyAreaMapper.deleteSafetyAreaByIds(areaIds);
    }

    @Override
    public int deleteSafetyAreaById(Long areaId)
    {
        if (safetyAreaMapper.countChildByParentId(areaId) > 0)
        {
            throw new ServiceException("删除失败，存在子区域，请先删除子区域");
        }
        return safetyAreaMapper.deleteSafetyAreaById(areaId);
    }

    @Override
    public boolean checkAreaExistChild(Long areaId)
    {
        return safetyAreaMapper.countChildByParentId(areaId) > 0;
    }
}