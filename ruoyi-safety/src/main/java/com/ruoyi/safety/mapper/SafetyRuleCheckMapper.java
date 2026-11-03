package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyRuleCheck;

/**
 * 安全守则执行检查记录 Mapper接口（需求一）
 */
public interface SafetyRuleCheckMapper {
    public List<SafetyRuleCheck> selectCheckList(SafetyRuleCheck check);

    public SafetyRuleCheck selectCheckById(Long checkId);

    public int insertCheck(SafetyRuleCheck check);

    public int updateCheck(SafetyRuleCheck check);

    public int deleteCheckByIds(Long[] checkIds);

    /** 统计：检查符合率（符合数/总检查数） */
    public int countChecks();

    public int countQualified();
}
