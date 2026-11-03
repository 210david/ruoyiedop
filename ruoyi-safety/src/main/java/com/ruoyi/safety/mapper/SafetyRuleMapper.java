package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyRule;
import com.ruoyi.safety.domain.SafetyRuleVersion;

/**
 * 安全守则 Mapper接口（需求一）
 */
public interface SafetyRuleMapper {
    public List<SafetyRule> selectRuleList(SafetyRule rule);

    public SafetyRule selectRuleById(Long ruleId);

    public int insertRule(SafetyRule rule);

    public int updateRule(SafetyRule rule);

    /** SR-02：生效守则不允许物理/逻辑删除，仅可废止；此处仅供草稿态删除 */
    public int deleteRuleByIds(Long[] ruleIds);

    /** 版本快照：新增历史版本 */
    public int insertRuleVersion(SafetyRuleVersion version);

    /** 历史版本列表 */
    public List<SafetyRuleVersion> selectRuleVersionList(Long ruleId);

    public SafetyRuleVersion selectRuleVersionById(Long versionId);

    /** 守则覆盖率统计：应签阅人数（按守则已发布的学习任务名单） */
    public int countLearnTotalByRule(Long ruleId);

    /** 守则覆盖率统计：已签阅人数 */
    public int countLearnSignedByRule(Long ruleId);
}
