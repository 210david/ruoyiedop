package com.ruoyi.food.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.food.domain.FoodMaterialParam;

/**
 * 物料行业参数 Mapper接口
 *
 * @author ruoyi
 */
public interface FoodMaterialParamMapper
{
    public List<FoodMaterialParam> selectParamList(FoodMaterialParam param);

    public FoodMaterialParam selectParamById(Long paramId);

    public FoodMaterialParam selectParamByMaterialId(Long materialId);

    public int insertParam(FoodMaterialParam param);

    public int updateParam(FoodMaterialParam param);

    public int deleteParamByIds(Long[] paramIds);

    /** 物料基本信息（编码/名称），保存参数时补全 */
    public Map<String, Object> selectMaterialInfo(Long materialId);
}
