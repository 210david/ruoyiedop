package com.ruoyi.food.mapper;

import java.util.List;
import com.ruoyi.food.domain.FoodBatchReserve;

/**
 * 食品批次预留Mapper接口
 *
 * @author ruoyi
 */
public interface FoodBatchReserveMapper
{
    public FoodBatchReserve selectReserveById(Long reserveId);

    public List<FoodBatchReserve> selectReserveList(FoodBatchReserve reserve);

    public int insertReserve(FoodBatchReserve reserve);

    public int updateReserve(FoodBatchReserve reserve);

    public int deleteReserveByIds(Long[] reserveIds);
}
