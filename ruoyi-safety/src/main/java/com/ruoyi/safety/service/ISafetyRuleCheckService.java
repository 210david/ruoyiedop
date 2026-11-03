package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyRuleCheck;

/**
 * 安全守则执行检查 Service接口（需求一）
 */
public interface ISafetyRuleCheckService {
    public List<SafetyRuleCheck> selectCheckList(SafetyRuleCheck check);

    public SafetyRuleCheck selectCheckById(Long checkId);

    /** 新增检查记录：SR-04 不符合项必须填整改责任人/期限 */
    public int insertCheck(SafetyRuleCheck check);

    /** 修改检查记录：SR-04 不符合项必须填整改责任人/期限/结果，闭环前不允许归档 */
    public int updateCheck(SafetyRuleCheck check);

    /** 整改闭环：不符合项填写整改结果后复查闭环 */
    public int reviewCheck(SafetyRuleCheck check);

    /** SR-04 归档：不符合项必须整改闭环后才能归档 */
    public int archiveCheck(Long checkId);

    public int deleteCheckByIds(Long[] checkIds);

    /** 检查符合率统计：符合数/总检查数 */
    public int countChecks();

    public int countQualified();
}
