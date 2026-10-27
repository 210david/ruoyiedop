package com.ruoyi.food.service;

import java.util.List;
import com.ruoyi.food.domain.FoodMaterialParam;

/**
 * 物料行业参数 Service接口
 *
 * @author ruoyi
 */
public interface IFoodMaterialParamService
{
    public List<FoodMaterialParam> selectParamList(FoodMaterialParam param);

    public FoodMaterialParam selectParamById(Long paramId);

    public FoodMaterialParam selectParamByMaterialId(Long materialId);

    public int insertParam(FoodMaterialParam param);

    public int updateParam(FoodMaterialParam param);

    public int deleteParamByIds(Long[] paramIds);
}
