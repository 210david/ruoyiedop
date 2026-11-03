package com.ruoyi.safety.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 安全守则签阅记录对象 biz_safety_rule_learn
 *
 * 需求一：员工完成阅读后签阅确认，记录签阅人、签阅时间、IP/终端（本期固定 PC，字段预留）
 */
public class SafetyRuleLearn extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 签阅记录ID */
    private Long learnId;

    /** 学习任务ID */
    private Long taskId;

    /** 守则ID */
    private Long ruleId;

    /** 签阅人ID */
    private Long userId;

    /** 签阅人 */
    private String userName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 状态（0待签阅 1已签阅 2逾期） */
    private String signStatus;

    /** 签阅时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date signTime;

    /** 签阅IP */
    private String signIp;

    /** 签阅终端（本期固定 PC，字段预留） */
    private String signTerminal;

    /** 测试成绩（联动AI安全培训试题） */
    private BigDecimal examScore;

    /** 是否通过（0否 1是） */
    private String isPass;

    /** 删除标志 */
    private String delFlag;

    public Long getLearnId() { return learnId; }
    public void setLearnId(Long learnId) { this.learnId = learnId; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getSignStatus() { return signStatus; }
    public void setSignStatus(String signStatus) { this.signStatus = signStatus; }
    public Date getSignTime() { return signTime; }
    public void setSignTime(Date signTime) { this.signTime = signTime; }
    public String getSignIp() { return signIp; }
    public void setSignIp(String signIp) { this.signIp = signIp; }
    public String getSignTerminal() { return signTerminal; }
    public void setSignTerminal(String signTerminal) { this.signTerminal = signTerminal; }
    public BigDecimal getExamScore() { return examScore; }
    public void setExamScore(BigDecimal examScore) { this.examScore = examScore; }
    public String getIsPass() { return isPass; }
    public void setIsPass(String isPass) { this.isPass = isPass; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
