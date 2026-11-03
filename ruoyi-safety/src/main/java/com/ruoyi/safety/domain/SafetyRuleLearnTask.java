package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 安全守则学习任务对象 biz_safety_rule_learn_task
 *
 * 需求一：守则发布后发起学习任务，指定对象与完成期限
 * SR-03 学习任务发起后，名单不可减少只可追加，保证追溯完整
 */
public class SafetyRuleLearnTask extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 学习任务ID */
    private Long taskId;

    /** 任务编号 */
    @Excel(name = "任务编号")
    private String taskNo;

    /** 任务名称 */
    @Excel(name = "任务名称")
    private String taskName;

    /** 守则ID */
    private Long ruleId;

    /** 守则名称 */
    @Excel(name = "守则名称")
    private String ruleName;

    /** 学习版本号 */
    private Integer ruleVersionNo;

    /** 学习对象说明 */
    @Excel(name = "学习对象")
    private String targetDesc;

    /** 完成期限 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "完成期限", dateFormat = "yyyy-MM-dd")
    private Date deadline;

    /** 状态（0进行中 1已完成 2已逾期） */
    @Excel(name = "状态", dictType = "safety_rule_learn_status")
    private String taskStatus;

    /** 删除标志 */
    private String delFlag;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public Integer getRuleVersionNo() { return ruleVersionNo; }
    public void setRuleVersionNo(Integer ruleVersionNo) { this.ruleVersionNo = ruleVersionNo; }
    public String getTargetDesc() { return targetDesc; }
    public void setTargetDesc(String targetDesc) { this.targetDesc = targetDesc; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    /** 学习对象人员名单（发起/追加时随任务提交，非表字段） */
    private java.util.List<SafetyRuleLearn> learns;
    public java.util.List<SafetyRuleLearn> getLearns() { return learns; }
    public void setLearns(java.util.List<SafetyRuleLearn> learns) { this.learns = learns; }
}
