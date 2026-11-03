package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyRule;
import com.ruoyi.safety.domain.SafetyRuleVersion;

/**
 * 安全守则 Service接口（需求一）
 */
public interface ISafetyRuleService {
    public List<SafetyRule> selectRuleList(SafetyRule rule);

    public SafetyRule selectRuleById(Long ruleId);

    /** 新增守则（草稿/生效），自动生成守则编号，版本号从1起 */
    public int insertRule(SafetyRule rule);

    /** 修改守则：自动生成新版本快照，保留历史版本可查看、回溯 */
    public int updateRule(SafetyRule rule);

    /** 发布守则：草稿→生效 */
    public int publishRule(Long ruleId);

    /** SR-02：生效守则不允许删除，只可废止并留痕 */
    public int abolishRule(Long ruleId);

    /** 仅草稿态守则允许删除 */
    public int deleteRuleByIds(Long[] ruleIds);

    /** 历史版本列表 */
    public List<SafetyRuleVersion> selectRuleVersionList(Long ruleId);

    /** 守则覆盖率统计：应签阅人数（按守则已发布的学习任务名单） */
    public int countLearnTotalByRule(Long ruleId);

    /** 守则覆盖率统计：已签阅人数 */
    public int countLearnSignedByRule(Long ruleId);
}
