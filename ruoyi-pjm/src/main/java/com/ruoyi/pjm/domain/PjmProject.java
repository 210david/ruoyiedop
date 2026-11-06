package com.ruoyi.pjm.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 项目立项表 pjm_project
 *
 * @author ruoyi
 */
public class PjmProject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 项目编号 */
    @Excel(name = "项目编号")
    private String projectNo;

    /** 项目名称 */
    @Excel(name = "项目名称")
    private String projectName;

    /** 项目类型 */
    @Excel(name = "项目类型", readConverterExp = "1=房屋建筑,2=市政工程,3=机电安装,4=装饰装修,5=园林绿化,6=基础设施,7=EPC总承包")
    private String projectType;

    /** ★ 工程类别（pjm_engineering_category）——安全生产风险管控用 */
    private String engineeringCategory;

    /** 优先级 */
    @Excel(name = "优先级", readConverterExp = "1=低,2=中,3=高,4=紧急")
    private String priority;

    /** 状态（0草稿 1待审 2通过 3执行 4完成 5关闭 6暂停 7驳回） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=待审,2=通过,3=执行中,4=已完成,5=已关闭,6=暂停,7=驳回")
    private String status;

    /** 整体进度% */
    @Excel(name = "进度%")
    private BigDecimal progress;

    /** ★ 关联 PMS 的冗余聚合字段 */
    @Excel(name = "项目预算")
    private BigDecimal budgetAmount;

    private BigDecimal plannedPurchase;
    private BigDecimal actualPurchase;
    private BigDecimal receivedAmount;
    private BigDecimal invoiceAmount;
    private BigDecimal paidAmount;
    private Integer purchaseCount;

    /** 计划开始 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划开始", width = 20, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 计划结束 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划结束", width = 20, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 实际开始 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualStartDate;

    /** 实际结束 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualEndDate;

    /** 项目经理 */
    private Long managerId;

    @Excel(name = "项目经理")
    private String managerName;

    /** 所属部门 */
    private Long deptId;

    @Excel(name = "所属部门")
    private String deptName;

    /** 建设单位（业主） */
    @Excel(name = "建设单位")
    private String ownerUnit;

    /** 施工单位 */
    @Excel(name = "施工单位")
    private String buildUnit;

    /** 监理单位 */
    @Excel(name = "监理单位")
    private String supervisorUnit;

    /** 设计单位 */
    @Excel(name = "设计单位")
    private String designUnit;

    /** 工程地点 */
    @Excel(name = "工程地点")
    private String projectAddress;

    /** 建设规模 */
    @Excel(name = "建设规模")
    private String buildScale;

    /** 合同工期（日历天） */
    @Excel(name = "合同工期(天)")
    private Integer contractDuration;

    /** 开工日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开工日期", width = 14, dateFormat = "yyyy-MM-dd")
    private Date commenceDate;

    /** 计划竣工日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划竣工日期", width = 14, dateFormat = "yyyy-MM-dd")
    private Date completionDate;

    /** 质量标准 */
    @Excel(name = "质量标准")
    private String qualityStandard;

    /** 合同总额(元) */
    @Excel(name = "合同总额")
    private java.math.BigDecimal contractAmount;

    /** 客户/甲方 */
    @Excel(name = "客户")
    private String customer;

    /** 项目描述 */
    private String description;

    /** 项目范围 */
    private String scope;

    /** 交付物清单 */
    private String deliverables;

    /** 审批人 */
    private String auditBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    private String auditOpinion;

    private String delFlag;

    /** ★ 附件列表（前端级联传递，不持久化到 pjm_project） */
    private List<PjmProjectAttachment> attachmentList;

    /** ★ 采购清单列表（前端级联传递，不持久化到 pjm_project） */
    private List<PjmProjectProcurement> procurementList;

    /** ★ 项目团队列表（前端级联传递，不持久化到 pjm_project） */
    private List<PjmProjectMember> memberList;

    /** ★ WBS 任务分解列表（前端级联传递，不持久化到 pjm_project） */
    private List<PjmWbsTask> wbsTaskList;

    public List<PjmProjectAttachment> getAttachmentList() { return attachmentList; }
    public void setAttachmentList(List<PjmProjectAttachment> attachmentList) { this.attachmentList = attachmentList; }
    public List<PjmProjectProcurement> getProcurementList() { return procurementList; }
    public void setProcurementList(List<PjmProjectProcurement> procurementList) { this.procurementList = procurementList; }
    public List<PjmProjectMember> getMemberList() { return memberList; }
    public void setMemberList(List<PjmProjectMember> memberList) { this.memberList = memberList; }
    public List<PjmWbsTask> getWbsTaskList() { return wbsTaskList; }
    public void setWbsTaskList(List<PjmWbsTask> wbsTaskList) { this.wbsTaskList = wbsTaskList; }

    // ============ getter/setter ============

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectNo() { return projectNo; }
    public void setProjectNo(String projectNo) { this.projectNo = projectNo; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getEngineeringCategory() { return engineeringCategory; }
    public void setEngineeringCategory(String engineeringCategory) { this.engineeringCategory = engineeringCategory; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getProgress() { return progress; }
    public void setProgress(BigDecimal progress) { this.progress = progress; }
    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
    public BigDecimal getPlannedPurchase() { return plannedPurchase; }
    public void setPlannedPurchase(BigDecimal plannedPurchase) { this.plannedPurchase = plannedPurchase; }
    public BigDecimal getActualPurchase() { return actualPurchase; }
    public void setActualPurchase(BigDecimal actualPurchase) { this.actualPurchase = actualPurchase; }
    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }
    public BigDecimal getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(BigDecimal invoiceAmount) { this.invoiceAmount = invoiceAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Integer getPurchaseCount() { return purchaseCount; }
    public void setPurchaseCount(Integer purchaseCount) { this.purchaseCount = purchaseCount; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Date getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(Date actualStartDate) { this.actualStartDate = actualStartDate; }
    public Date getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(Date actualEndDate) { this.actualEndDate = actualEndDate; }
    public Long getManagerId() { return managerId; }
    public void setManagerId(Long managerId) { this.managerId = managerId; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getOwnerUnit() { return ownerUnit; }
    public void setOwnerUnit(String ownerUnit) { this.ownerUnit = ownerUnit; }
    public String getBuildUnit() { return buildUnit; }
    public void setBuildUnit(String buildUnit) { this.buildUnit = buildUnit; }
    public String getSupervisorUnit() { return supervisorUnit; }
    public void setSupervisorUnit(String supervisorUnit) { this.supervisorUnit = supervisorUnit; }
    public String getDesignUnit() { return designUnit; }
    public void setDesignUnit(String designUnit) { this.designUnit = designUnit; }
    public String getProjectAddress() { return projectAddress; }
    public void setProjectAddress(String projectAddress) { this.projectAddress = projectAddress; }
    public String getBuildScale() { return buildScale; }
    public void setBuildScale(String buildScale) { this.buildScale = buildScale; }
    public Integer getContractDuration() { return contractDuration; }
    public void setContractDuration(Integer contractDuration) { this.contractDuration = contractDuration; }
    public Date getCommenceDate() { return commenceDate; }
    public void setCommenceDate(Date commenceDate) { this.commenceDate = commenceDate; }
    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }
    public String getQualityStandard() { return qualityStandard; }
    public void setQualityStandard(String qualityStandard) { this.qualityStandard = qualityStandard; }
    public java.math.BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(java.math.BigDecimal contractAmount) { this.contractAmount = contractAmount; }
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getDeliverables() { return deliverables; }
    public void setDeliverables(String deliverables) { this.deliverables = deliverables; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
