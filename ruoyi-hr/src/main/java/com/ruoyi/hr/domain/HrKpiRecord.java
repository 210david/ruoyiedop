package com.ruoyi.hr.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * KPI考核记录对象 biz_hr_kpi_record
 *
 * 需求三：绩效考核线上流程（发起—自评—评分—确认—申诉）
 */
public class HrKpiRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 考核记录ID */
    private Long recordId;

    /** 方案ID */
    private Long schemeId;

    /** 方案名称 */
    @Excel(name = "考核方案")
    private String schemeName;

    /** 考核周期（yyyy-MM） */
    @Excel(name = "考核周期")
    private String period;

    /** 员工ID */
    private Long employeeId;

    /** 员工姓名 */
    @Excel(name = "员工姓名")
    private String employeeName;

    /** 自评分 */
    private BigDecimal selfScore;

    /** 自评说明 */
    private String selfComment;

    /** 上级评分 */
    private BigDecimal superiorScore;

    /** 上级评语 */
    private String superiorComment;

    /** 最终得分 */
    @Excel(name = "最终得分")
    private BigDecimal finalScore;

    /** 状态（0发起 1已自评 2已评分 3已确认 4申诉中 5已完成） */
    @Excel(name = "状态", dictType = "hr_kpi_status")
    private String kpiStatus;

    /** 申诉理由 */
    private String appealReason;

    /** 申诉处理结果 */
    private String appealResult;

    /** 删除标志 */
    private String delFlag;

    /** 考核对象员工ID列表（发起考核批量传参用，非表字段） */
    private java.util.List<Long> employeeIds;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }
    public String getSchemeName() { return schemeName; }
    public void setSchemeName(String schemeName) { this.schemeName = schemeName; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public BigDecimal getSelfScore() { return selfScore; }
    public void setSelfScore(BigDecimal selfScore) { this.selfScore = selfScore; }
    public String getSelfComment() { return selfComment; }
    public void setSelfComment(String selfComment) { this.selfComment = selfComment; }
    public BigDecimal getSuperiorScore() { return superiorScore; }
    public void setSuperiorScore(BigDecimal superiorScore) { this.superiorScore = superiorScore; }
    public String getSuperiorComment() { return superiorComment; }
    public void setSuperiorComment(String superiorComment) { this.superiorComment = superiorComment; }
    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }
    public String getKpiStatus() { return kpiStatus; }
    public void setKpiStatus(String kpiStatus) { this.kpiStatus = kpiStatus; }
    public String getAppealReason() { return appealReason; }
    public void setAppealReason(String appealReason) { this.appealReason = appealReason; }
    public String getAppealResult() { return appealResult; }
    public void setAppealResult(String appealResult) { this.appealResult = appealResult; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public java.util.List<Long> getEmployeeIds() { return employeeIds; }
    public void setEmployeeIds(java.util.List<Long> employeeIds) { this.employeeIds = employeeIds; }
}
