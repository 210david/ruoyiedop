package com.ruoyi.food.service;

import java.util.List;
import com.ruoyi.food.domain.FoodBatchRule;

/**
 * 食品批次号规则Service接口
 *
 * @author ruoyi
 */
public interface IFoodBatchRuleService
{
    public FoodBatchRule selectRuleById(Long ruleId);

    public List<FoodBatchRule> selectRuleList(FoodBatchRule rule);

    public int insertRule(FoodBatchRule rule);

    public int updateRule(FoodBatchRule rule);

    public int deleteRuleByIds(Long[] ruleIds);

    /**
     * 原子取号生成批次号（跨日流水自动重置）
     *
     * @param materialId 物料ID
     * @param materialCode 物料编码（{materialCode}占位符展开用）
     * @return 批次号
     */
    public String nextBatchCode(Long materialId, String materialCode);

    /**
     * 规则试算预览（不消耗流水号）
     */
    public String previewPattern(String rulePattern, String materialCode);
}
