package com.ruoyi.safety.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 安全守则对象 biz_safety_rule
 *
 * 需求一：安全生产台账——安全守则记录模块
 * 业务规则：
 * SR-01 守则关联“安全生产制度名称+附件”为选填弱关联（YHNY 无独立制度模块，评审决策口径）
 * SR-02 生效状态的守则不可直接删除，只可“废止”并留痕
 */
public class SafetyRule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 守则ID */
    private Long ruleId;

    /** 守则编号 */
    @Excel(name = "守则编号")
    private String ruleNo;

    /** 守则名称 */
    @Excel(name = "守则名称")
    private String ruleName;

    /** 适用部门ID */
    private Long deptId;

    /** 适用部门 */
    @Excel(name = "适用部门")
    private String deptName;

    /** 适用岗位 */
    @Excel(name = "适用岗位")
    private String postName;

    /** 适用作业场景 */
    @Excel(name = "适用作业场景")
    private String workScene;

    /** 守则内容（富文本） */
    private String ruleContent;

    /** 附件 */
    private String attachment;

    /** 关联安全生产制度名称（SR-01 弱关联，选填） */
    @Excel(name = "关联制度名称")
    private String regulationName;

    /** 关联制度附件 */
    private String regulationAttachment;

    /** 版本号 */
    @Excel(name = "版本号")
    private Integer versionNo;

    /** 生效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "生效日期", dateFormat = "yyyy-MM-dd")
    private Date effectiveDate;

    /** 失效日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "失效日期", dateFormat = "yyyy-MM-dd")
    private Date expireDate;

    /** 状态（0草稿 1生效 2废止） */
    @Excel(name = "状态", dictType = "safety_rule_status")
    private String ruleStatus;

    /** 编制人 */
    @Excel(name = "编制人")
    private String author;

    /** 审批人 */
    @Excel(name = "审批人")
    private String approver;

    /** 删除标志 */
    private String delFlag;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    public String getRuleNo() { return ruleNo; }
    public void setRuleNo(String ruleNo) { this.ruleNo = ruleNo; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
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
    public String getRegulationAttachment() { return regulationAttachment; }
    public void setRegulationAttachment(String regulationAttachment) { this.regulationAttachment = regulationAttachment; }
    public Integer getVersionNo() { return versionNo; }
    public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public String getRuleStatus() { return ruleStatus; }
    public void setRuleStatus(String ruleStatus) { this.ruleStatus = ruleStatus; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
