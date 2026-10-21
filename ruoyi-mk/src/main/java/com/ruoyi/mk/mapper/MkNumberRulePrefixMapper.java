package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkNumberRulePrefix;

/**
 * 编号规则动态前缀映射 Mapper接口
 *
 * @author ruoyi
 */
public interface MkNumberRulePrefixMapper
{
    /** 根据规则ID查询前缀映射列表 */
    public List<MkNumberRulePrefix> selectPrefixListByRuleId(Long ruleId);

    /** 新增前缀映射 */
    public int insertPrefix(MkNumberRulePrefix prefix);

    /** 根据规则ID删除前缀映射 */
    public int deletePrefixByRuleId(Long ruleId);

    /** 根据规则ID和字段值查询启用的前缀映射 */
    public MkNumberRulePrefix selectEnabledPrefix(Long ruleId, String fieldValue);

    /** 根据规则ID和字段值查询启用的前缀映射（加行锁，用于按动态前缀重置时） */
    public MkNumberRulePrefix selectEnabledPrefixForUpdate(Long ruleId, String fieldValue);

    /** 更新前缀映射的当前序列号和日期串 */
    public int updatePrefixSeq(MkNumberRulePrefix prefix);
}
