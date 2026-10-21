package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkNumberRule;

/**
 * 编号规则 Mapper接口
 *
 * @author ruoyi
 */
public interface MkNumberRuleMapper
{
    public List<MkNumberRule> selectRuleList(MkNumberRule rule);

    public MkNumberRule selectRuleById(Long ruleId);

    public MkNumberRule selectRuleByCode(String ruleCode);

    /**
     * 查询规则（加行锁，用于生成编号时保证线程安全）
     */
    public MkNumberRule selectRuleForUpdate(String ruleCode);

    public int insertRule(MkNumberRule rule);

    public int updateRule(MkNumberRule rule);

    /**
     * 更新当前序列号和日期串
     */
    public int updateCurrentSeq(MkNumberRule rule);

    public int deleteRuleByIds(Long[] ruleIds);

    /**
     * 校验规则编码是否唯一
     */
    public MkNumberRule checkRuleCodeUnique(String ruleCode);
}
