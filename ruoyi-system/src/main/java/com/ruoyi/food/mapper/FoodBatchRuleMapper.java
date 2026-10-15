package com.ruoyi.food.mapper;

import java.util.List;
import com.ruoyi.food.domain.FoodBatchRule;

/**
 * 食品批次号规则Mapper接口
 *
 * @author ruoyi
 */
public interface FoodBatchRuleMapper
{
    public FoodBatchRule selectRuleById(Long ruleId);

    public List<FoodBatchRule> selectRuleList(FoodBatchRule rule);

    public FoodBatchRule selectRuleByMaterial(Long materialId);

    public int insertRule(FoodBatchRule rule);

    public int updateRule(FoodBatchRule rule);

    public int deleteRuleByIds(Long[] ruleIds);

    /**
     * 原子取号：跨日重置+自增一步完成
     * current_seq = LAST_INSERT_ID(IF(seq_date=#{today}, current_seq+1, 1))
     * 配合 selectLastSeq 在同一连接内读取（LAST_INSERT_ID连接级隔离，并发安全）
     */
    public int nextSeq(FoodBatchRule rule);

    public Long selectLastSeq();
}
