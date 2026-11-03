package com.ruoyi.safety.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.safety.service.ISafetyRuleCheckService;
import com.ruoyi.safety.service.ISafetyRuleLearnService;

/**
 * 守则学习/检查逾期定时任务（需求一）
 *
 * 学习任务：逾期未签阅人员逐级上报提醒（SR-03 配套）
 * 执行检查：整改期限已过，状态 待整改→待复查（SR-04 配套）
 */
@Component("safetyRuleOverdueTask")
public class SafetyRuleOverdueTask {
    @Autowired
    private ISafetyRuleLearnService learnService;

    @Autowired
    private ISafetyRuleCheckService checkService;

    public void handle() {
        learnService.handleOverdue();
        // 执行检查：整改期限已过，待整改→待复查
        // TODO 检查逾期提醒（接入消息中心组件后补充）
    }
}
