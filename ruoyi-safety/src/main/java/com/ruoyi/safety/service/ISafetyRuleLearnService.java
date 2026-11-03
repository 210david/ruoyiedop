package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyRuleLearn;
import com.ruoyi.safety.domain.SafetyRuleLearnTask;

/**
 * 安全守则学习任务/签阅 Service接口（需求一）
 */
public interface ISafetyRuleLearnService {
    public List<SafetyRuleLearnTask> selectTaskList(SafetyRuleLearnTask task);

    public SafetyRuleLearnTask selectTaskById(Long taskId);

    /** 发起学习任务：仅生效守则可发起，SR-03 名单只可追加 */
    public int insertTask(SafetyRuleLearnTask task);

    /** SR-03 名单追加（已存在的人员跳过，不减少不覆盖） */
    public int appendLearnBatch(SafetyRuleLearnTask task, List<SafetyRuleLearn> learns);

    /** 签阅名单查询 */
    public List<SafetyRuleLearn> selectLearnList(SafetyRuleLearn learn);

    /** 签阅确认（记录签阅人/时间/IP/终端，本期固定 PC） */
    public int signLearn(SafetyRuleLearn learn);

    /** 学习任务统计：任务总数/已签阅 */
    public List<SafetyRuleLearnTask> selectTaskStatList();

    /** 逾期检查（SR-03 配套定时任务） */
    public int handleOverdue();
}
