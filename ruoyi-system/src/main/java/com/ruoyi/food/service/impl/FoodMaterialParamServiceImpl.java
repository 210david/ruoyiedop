package com.ruoyi.food.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodMaterialParam;
import com.ruoyi.food.mapper.FoodMaterialParamMapper;
import com.ruoyi.food.service.IFoodMaterialParamService;

/**
 * 物料行业参数 Service实现
 *
 * @author ruoyi
 */
@Service
public class FoodMaterialParamServiceImpl implements IFoodMaterialParamService
{
    @Autowired
    private FoodMaterialParamMapper paramMapper;

    @Override
    public List<FoodMaterialParam> selectParamList(FoodMaterialParam param)
    {
        return paramMapper.selectParamList(param);
    }

    @Override
    public FoodMaterialParam selectParamById(Long paramId)
    {
        return paramMapper.selectParamById(paramId);
    }

    @Override
    public FoodMaterialParam selectParamByMaterialId(Long materialId)
    {
        return paramMapper.selectParamByMaterialId(materialId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertParam(FoodMaterialParam param)
    {
        if (param.getMaterialId() == null)
        {
            throw new ServiceException("物料不能为空");
        }
        if (paramMapper.selectParamByMaterialId(param.getMaterialId()) != null)
        {
            throw new ServiceException("该物料已配置行业参数，请直接修改");
        }
        if (StringUtils.isEmpty(param.getMaterialCode()) || StringUtils.isEmpty(param.getMaterialName()))
        {
            Map<String, Object> m = paramMapper.selectMaterialInfo(param.getMaterialId());
            if (m == null)
            {
                throw new ServiceException("物料不存在或已删除");
            }
            param.setMaterialCode((String) m.get("material_code"));
            param.setMaterialName((String) m.get("material_name"));
        }
        if ("1".equals(param.getYieldModel()))
        {
            if (param.getStdYieldRate() == null || param.getStdYieldRate().compareTo(java.math.BigDecimal.ZERO) <= 0
                    || param.getStdYieldRate().compareTo(new java.math.BigDecimal(100)) > 0)
            {
                throw new ServiceException("产出率模型的基准产出率必须在(0,100]之间");
            }
        }
        param.setDelFlag("0");
        param.setCreateBy(SecurityUtils.getUsername());
        param.setCreateTime(DateUtils.getNowDate());
        return paramMapper.insertParam(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateParam(FoodMaterialParam param)
    {
        if (param.getParamId() == null)
        {
            throw new ServiceException("参数ID不能为空");
        }
        if ("1".equals(param.getYieldModel())
                && param.getStdYieldRate() != null
                && (param.getStdYieldRate().compareTo(java.math.BigDecimal.ZERO) <= 0
                    || param.getStdYieldRate().compareTo(new java.math.BigDecimal(100)) > 0))
        {
            throw new ServiceException("产出率模型的基准产出率必须在(0,100]之间");
        }
        param.setUpdateBy(SecurityUtils.getUsername());
        param.setUpdateTime(DateUtils.getNowDate());
        return paramMapper.updateParam(param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteParamByIds(Long[] paramIds)
    {
        return paramMapper.deleteParamByIds(paramIds);
    }
}
