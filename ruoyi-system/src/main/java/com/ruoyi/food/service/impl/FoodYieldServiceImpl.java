package com.ruoyi.food.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.food.domain.FoodYieldRow;
import com.ruoyi.food.mapper.FoodYieldMapper;
import com.ruoyi.food.service.IFoodYieldService;

/**
 * 产出率损耗核算 Service实现
 *
 * 产出率模型（火腿）：产出率 = 实际完工 / 投入鲜腿 × 100；理论产出 = 投入 × 基准产出率(配置数据，如76%)；
 * 产出率低于告警下限标记告警；分环节损耗来自工序转移单（修割/盐渍/发酵失重/其他）。
 *
 * @author ruoyi
 */
@Service
public class FoodYieldServiceImpl implements IFoodYieldService
{
    private static final BigDecimal HUNDRED = new BigDecimal(100);

    @Autowired
    private FoodYieldMapper yieldMapper;

    @Override
    public List<FoodYieldRow> selectYieldAnalysis(FoodYieldRow query)
    {
        List<FoodYieldRow> rows = yieldMapper.selectYieldAnalysis(query);
        for (FoodYieldRow row : rows)
        {
            calculateRow(row);
        }
        return rows;
    }

    private void calculateRow(FoodYieldRow row)
    {
        row.setAlarmFlag("0");
        // 仅产出率模型核算产出率/损耗率；配比配方走BOM，不在此核算
        if (!"1".equals(row.getYieldModel()))
        {
            return;
        }
        BigDecimal input = row.getInputQty();
        BigDecimal actual = row.getActualQty();
        if (row.getStdYieldRate() != null && input != null && input.compareTo(BigDecimal.ZERO) > 0)
        {
            row.setTheoryQty(input.multiply(row.getStdYieldRate()).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        }
        if (input != null && input.compareTo(BigDecimal.ZERO) > 0 && actual != null)
        {
            BigDecimal yieldRate = actual.multiply(HUNDRED).divide(input, 2, RoundingMode.HALF_UP);
            row.setYieldRate(yieldRate);
            row.setLossRate(HUNDRED.subtract(yieldRate));
            if (row.getYieldAlarmRate() != null && yieldRate.compareTo(row.getYieldAlarmRate()) < 0)
            {
                row.setAlarmFlag("1");
            }
        }
    }
}
