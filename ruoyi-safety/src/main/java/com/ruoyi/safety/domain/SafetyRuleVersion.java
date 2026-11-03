package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 安全守则历史版本对象 biz_safety_rule_version
 *
 * 需求一：守则条目变更时自动生成新版本，保留历史版本并可查看、回溯
 */
public class SafetyRuleVersion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    private Long versionId;

    /** 守则ID */
    private Long ruleId;

    /** 守则编号 */
    private String ruleNo;

    /** 版本号 */
    private Integer versionNo;

    /** 守则名称 */
    private String ruleName;

    /** 适用部门 */
    private String deptName;

    /** 适用岗位 */
    private String postName;

    /** 适用作业场景 */
    private String workScene;

    /** 守则内容 */
    private String ruleContent;

    /** 附件 */
    private String attachment;

    /** 关联制度名称 */
    private String regulationName;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    /** 当时状态 */
    private String ruleStatus;

    /** 变更说明 */
    private String changeNote;

    /** 变更人 */
    private String changeBy;

    /** 变更时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date changeTime;

    public Long getVersionId() { return versionId; }
    public void setVersionId(Long versionId) { this.versionId = versionId; }
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleNo() { return ruleNo; }
    public void setRuleNo(String ruleNo) { this.ruleNo = ruleNo; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getPostName() { return postName; }
    public void setPostName(String postName) { this.postName = postName; }
    public String getWorkScene() { return workScene; }
    public void setWorkScene(String workScene) { this.workScene = workScene; }
    public String getRuleContent() { return ruleContent; }
    public void setRuleContent(String ruleContent) { this.ruleContent = ruleContent; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getRegulationName() { return regulationName; }
    public void setRegulationName(String regulationName) { this.regulationName = regulationName; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getRuleStatus() { return ruleStatus; }
    public void setRuleStatus(String ruleStatus) { this.ruleStatus = ruleStatus; }
    public String getChangeNote() { return changeNote; }
    public void setChangeNote(String changeNote) { this.changeNote = changeNote; }
    public String getChangeBy() { return changeBy; }
    public void setChangeBy(String changeBy) { this.changeBy = changeBy; }
    public Date getChangeTime() { return changeTime; }
    public void setChangeTime(Date changeTime) { this.changeTime = changeTime; }
}
