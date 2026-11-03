package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 安全守则执行检查记录对象 biz_safety_rule_check
 *
 * 需求一：守则执行检查记录（整改闭环）
 * SR-04 检查结果“不符合”项必须填写整改责任人、期限与整改结果，闭环前不允许归档
 */
public class SafetyRuleCheck extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 检查记录ID */
    private Long checkId;

    /** 检查编号 */
    @Excel(name = "检查编号")
    private String checkNo;

    /** 检查日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "检查日期", dateFormat = "yyyy-MM-dd")
    private Date checkDate;

    /** 检查人 */
    @Excel(name = "检查人")
    private String checker;

    /** 被检查部门ID */
    private Long deptId;

    /** 被检查部门/岗位 */
    @Excel(name = "被检查部门/岗位")
    private String deptName;

    /** 关联守则ID */
    private Long ruleId;

    /** 关联守则名称 */
    @Excel(name = "关联守则")
    private String ruleName;

    /** 检查结果（1符合 2不符合 3不涉及） */
    @Excel(name = "检查结果", dictType = "safety_rule_check_result")
    private String checkResult;

    /** 不符合描述 */
    private String problemDesc;

    /** 整改要求 */
    private String rectifyRequirement;

    /** 整改责任人 */
    @Excel(name = "整改责任人")
    private String rectifyUser;

    /** 整改期限 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "整改期限", dateFormat = "yyyy-MM-dd")
    private Date rectifyDeadline;

    /** 整改结果 */
    private String rectifyResult;

    /** 复查人 */
    private String reviewer;

    /** 复查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    /** 状态（0待整改 1待复查 2已闭环 3已归档） */
    @Excel(name = "状态")
    private String checkStatus;

    /** 删除标志 */
    private String delFlag;

    public Long getCheckId() { return checkId; }
    public void setCheckId(Long checkId) { this.checkId = checkId; }
    public String getCheckNo() { return checkNo; }
    public void setCheckNo(String checkNo) { this.checkNo = checkNo; }
    public Date getCheckDate() { return checkDate; }
    public void setCheckDate(Date checkDate) { this.checkDate = checkDate; }
    public String getChecker() { return checker; }
    public void setChecker(String checker) { this.checker = checker; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getCheckResult() { return checkResult; }
    public void setCheckResult(String checkResult) { this.checkResult = checkResult; }
    public String getProblemDesc() { return problemDesc; }
    public void setProblemDesc(String problemDesc) { this.problemDesc = problemDesc; }
    public String getRectifyRequirement() { return rectifyRequirement; }
    public void setRectifyRequirement(String rectifyRequirement) { this.rectifyRequirement = rectifyRequirement; }
    public String getRectifyUser() { return rectifyUser; }
    public void setRectifyUser(String rectifyUser) { this.rectifyUser = rectifyUser; }
    public Date getRectifyDeadline() { return rectifyDeadline; }
    public void setRectifyDeadline(Date rectifyDeadline) { this.rectifyDeadline = rectifyDeadline; }
    public String getRectifyResult() { return rectifyResult; }
    public void setRectifyResult(String rectifyResult) { this.rectifyResult = rectifyResult; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public Date getReviewTime() { return reviewTime; }
    public void setReviewTime(Date reviewTime) { this.reviewTime = reviewTime; }
    public String getCheckStatus() { return checkStatus; }
    public void setCheckStatus(String checkStatus) { this.checkStatus = checkStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
