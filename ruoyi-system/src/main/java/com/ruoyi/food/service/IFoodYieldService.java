package com.ruoyi.food.service;

import java.util.List;
import com.ruoyi.food.domain.FoodYieldRow;

/**
 * 产出率损耗核算 Service接口
 *
 * @author ruoyi
 */
public interface IFoodYieldService
{
    /** 工单产出率核算（产出率/损耗率/分环节损耗/告警标志） */
    public List<FoodYieldRow> selectYieldAnalysis(FoodYieldRow query);
}
