package com.ruoyi.safety.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.safety.domain.SafetyRule;
import com.ruoyi.safety.domain.SafetyRuleLearn;
import com.ruoyi.safety.domain.SafetyRuleLearnTask;
import com.ruoyi.safety.mapper.SafetyRuleLearnMapper;
import com.ruoyi.safety.mapper.SafetyRuleMapper;
import com.ruoyi.safety.service.ISafetyRuleLearnService;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.system.domain.SysMessage;
import com.ruoyi.system.service.ISysMessageService;

/**
 * 安全守则学习任务/签阅 Service业务层处理（需求一）
 *
 * 业务规则：
 * SR-03 学习任务发起后，名单不可减少只可追加，保证追溯完整
 */
@Service
public class SafetyRuleLearnServiceImpl implements ISafetyRuleLearnService {
    @Autowired
    private SafetyRuleLearnMapper mapper;

    @Autowired
    private SafetyRuleMapper ruleMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private ISysMessageService messageService;

    @Override
    public List<SafetyRuleLearnTask> selectTaskList(SafetyRuleLearnTask task) { return mapper.selectTaskList(task); }

    @Override
    public SafetyRuleLearnTask selectTaskById(Long taskId) { return mapper.selectTaskById(taskId); }

    @Override
    public int insertTask(SafetyRuleLearnTask task) {
        if (task.getRuleId() == null) throw new ServiceException("请选择守则");
        SafetyRule rule = ruleMapper.selectRuleById(task.getRuleId());
        if (rule == null) throw new ServiceException("守则不存在");
        // 仅生效守则可发起学习任务
        if (!"1".equals(rule.getRuleStatus())) throw new ServiceException("仅「生效」状态的守则可发起学习任务");
        if (task.getDeadline() == null) throw new ServiceException("请设置完成期限");
        if (StringUtils.isEmpty(task.getTaskNo())) {
            // 编号生成失败不影响主流程（在事务外尝试，避免内部事务异常导致外层 rollback-only）
            try {
                task.setTaskNo(mkNumberRuleService.generateNumber("biz_safety_rule_learn_task"));
            } catch (Exception e) {
                task.setTaskNo("XXRW-" + DateUtils.dateTimeNow("yyyy") + "-" + DateUtils.dateTimeNow("HHmmssSSS"));
            }
        }
        task.setRuleName(rule.getRuleName());
        task.setRuleVersionNo(rule.getVersionNo());
        task.setTaskStatus("0");
        task.setDelFlag("0");
        task.setCreateBy(SecurityUtils.getUsername());
        int rows = mapper.insertTask(task);
        // 学习对象人员名单随任务保存（SR-03 名单只可追加）
        if (task.getLearns() != null && !task.getLearns().isEmpty()) {
            task.getLearns().forEach(l -> { l.setTaskId(task.getTaskId()); l.setRuleId(task.getRuleId()); l.setCreateBy(SecurityUtils.getUsername()); });
            mapper.appendLearnBatch(task.getLearns());
            sendLearnRemind(task, task.getLearns());
        }
        return rows;
    }

    /** 向学习对象发送点对点学习提醒消息（失败不影响主流程） */
    private void sendLearnRemind(SafetyRuleLearnTask task, List<SafetyRuleLearn> learns) {
        try {
            for (SafetyRuleLearn l : learns) {
                if (l.getUserId() == null) continue;
                SysMessage msg = new SysMessage();
                msg.setMessageTitle("安全守则学习任务：" + task.getTaskName());
                msg.setMessageContent("守则《" + task.getRuleName() + "》（v" + task.getRuleVersionNo() + "）学习任务，请于 " + DateUtils.parseDateToStr("yyyy-MM-dd", task.getDeadline()) + " 前完成阅读并签阅");
                msg.setMessageType("3");
                msg.setMessageLevel("2");
                msg.setBizSource("safety");
                msg.setBizId(task.getTaskId());
                msg.setRedirectUrl("/safety/rule/learn");
                msg.setRecipientId(l.getUserId());
                msg.setStatus("1");
                messageService.insertMessage(msg);
            }
        } catch (Exception e) {
            // 消息发送失败不影响主业务流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int appendLearnBatch(SafetyRuleLearnTask task, List<SafetyRuleLearn> learns) {
        if (task == null || task.getTaskId() == null) throw new ServiceException("学习任务不能为空");
        SafetyRuleLearnTask existing = mapper.selectTaskById(task.getTaskId());
        if (existing == null) throw new ServiceException("学习任务不存在");
        // SR-03：名单只可追加，不允许减少
        if (task.getTargetDesc() != null || task.getDeadline() != null) mapper.updateTask(task);
        if (learns == null || learns.isEmpty()) return 0;
        List<SafetyRuleLearn> existList = mapper.selectLearnList(new SafetyRuleLearn() {{ setTaskId(task.getTaskId()); }});
        List<SafetyRuleLearn> toAdd = new ArrayList<>();
        for (SafetyRuleLearn item : learns) {
            boolean exists = existList.stream().anyMatch(l -> item.getUserId() != null && item.getUserId().equals(l.getUserId()));
            if (!exists) toAdd.add(item);
        }
        if (toAdd.isEmpty()) return 0;
        toAdd.forEach(l -> { l.setTaskId(task.getTaskId()); l.setRuleId(existing.getRuleId()); l.setCreateBy(SecurityUtils.getUsername()); });
        int rows = mapper.appendLearnBatch(toAdd);
        sendLearnRemind(existing, toAdd);
        return rows;
    }

    @Override
    public List<SafetyRuleLearn> selectLearnList(SafetyRuleLearn learn) { return mapper.selectLearnList(learn); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int signLearn(SafetyRuleLearn learn) {
        if (learn.getLearnId() == null) throw new ServiceException("签阅记录不能为空");
        SafetyRuleLearn existing = mapper.selectLearnById(learn.getLearnId());
        if (existing == null) throw new ServiceException("签阅记录不存在");
        if ("1".equals(existing.getSignStatus())) throw new ServiceException("该人员已签阅，请勿重复签阅");
        learn.setSignStatus("1");
        learn.setSignTime(DateUtils.getNowDate());
        if (StringUtils.isEmpty(learn.getSignTerminal())) learn.setSignTerminal("PC");
        learn.setUpdateBy(SecurityUtils.getUsername());
        int rows = mapper.signLearn(learn);
        // 全部签阅后任务自动完成
        mapper.completeTasksIfAllSigned();
        return rows;
    }

    @Override
    public List<SafetyRuleLearnTask> selectTaskStatList() { return mapper.selectTaskStatList(); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleOverdue() {
        int rows = mapper.markLearnOverdue();
        // TODO 消息中心：对逾期未签阅人员逐级上报提醒（接入消息中心组件后补充）
        return rows;
    }
}
