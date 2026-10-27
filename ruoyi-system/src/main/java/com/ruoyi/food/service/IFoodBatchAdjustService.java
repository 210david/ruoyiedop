package com.ruoyi.food.service;

import java.util.List;
import com.ruoyi.food.domain.FoodBatchAdjust;

/**
 * 食品批次调整单Service接口
 *
 * @author ruoyi
 */
public interface IFoodBatchAdjustService
{
    public FoodBatchAdjust selectAdjustById(Long adjustId);

    public List<FoodBatchAdjust> selectAdjustList(FoodBatchAdjust adjust);

    /**
     * 新增调整单（待执行）
     */
    public int insertAdjust(FoodBatchAdjust adjust);

    /**
     * 执行调整单（事务：批次状态/数量/谱系/状态日志联动）
     */
    public int executeAdjust(Long adjustId);

    /**
     * 取消调整单（仅待执行可取消）
     */
    public int cancelAdjust(Long adjustId);

    public int deleteAdjustByIds(Long[] adjustIds);
}
