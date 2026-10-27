package com.ruoyi.food.mapper;

import java.util.List;
import com.ruoyi.food.domain.FoodYieldRow;

/**
 * 产出率损耗核算 Mapper接口
 *
 * @author ruoyi
 */
public interface FoodYieldMapper
{
    /** 工单产出率核算列表（投入=领料明细合计，实际产出=工单合格数，分环节损耗=工序转移汇总） */
    public List<FoodYieldRow> selectYieldAnalysis(FoodYieldRow query);
}
