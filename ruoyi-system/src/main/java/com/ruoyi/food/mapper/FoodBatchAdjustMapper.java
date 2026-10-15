package com.ruoyi.food.mapper;

import java.util.List;
import com.ruoyi.food.domain.FoodBatchAdjust;
import com.ruoyi.food.domain.FoodBatchAdjustItem;

/**
 * 食品批次调整单Mapper接口（主单+明细）
 *
 * @author ruoyi
 */
public interface FoodBatchAdjustMapper
{
    public FoodBatchAdjust selectAdjustById(Long adjustId);

    public List<FoodBatchAdjust> selectAdjustList(FoodBatchAdjust adjust);

    public int insertAdjust(FoodBatchAdjust adjust);

    public int updateAdjust(FoodBatchAdjust adjust);

    public int deleteAdjustByIds(Long[] adjustIds);

    // ---------------- 明细 ----------------

    public int insertAdjustItem(FoodBatchAdjustItem item);

    public List<FoodBatchAdjustItem> selectItemsByAdjustId(Long adjustId);

    public int updateAdjustItem(FoodBatchAdjustItem item);

    public int deleteItemsByAdjustId(Long adjustId);
}
