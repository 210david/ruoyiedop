package com.ruoyi.pdm.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 技术变更单对象 pdm_change_order
 *
 * 状态机：0草稿 → 1审批中 → 2待执行 → 3执行中 → 4已关闭；审批驳回 → 5已驳回。
 * 覆盖设计方案、图纸、技术文件、设计BOM的变更（设计方案 7.6）。
 *
 * @author ruoyi
 */
public class PdmChangeOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 变更单ID */
    private Long changeId;

    /** 变更单编号（自动生成） */
    @Excel(name = "变更单编号")
    private String changeNo;

    /** 变更标题 */
    @Excel(name = "变更标题")
    private String changeName;

    /** 变更产品档案ID */
    private Long productId;

    /** 产品编码（冗余） */
    @Excel(name = "产品编码")
    private String productCode;

    /** 产品名称（冗余） */
    @Excel(name = "产品名称")
    private String productName;

    /** 变更级别（字典 pdm_change_level：1=一般变更,2=重大变更） */
    @Excel(name = "变更级别", dictType = "pdm_change_level")
    private String changeLevel;

    /** 紧急程度（字典 pdm_urgent_level：1=普通,2=紧急,3=特急） */
    @Excel(name = "紧急程度", dictType = "pdm_urgent_level")
    private String urgentLevel;

    /** 变更内容说明 */
    private String changeContent;

    /** 变更原因 */
    private String changeReason;

    /** 影响分析 */
    private String effectAnalysis;

    /** 变更状态（字典 pdm_change_status） */
    @Excel(name = "变更状态", dictType = "pdm_change_status")
    private String changeStatus;

    /** 提交审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 审批人 */
    @Excel(name = "审批人")
    private String auditBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审批意见 */
    private String auditOpinion;

    /** 关闭时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closeTime;

    /** 关闭人 */
    private String closeBy;

    /** 部门ID（数据权限） */
    private Long deptId;

    /** 删除标志 */
    private String delFlag;

    /** 涉及资料明细列表 */
    private List<PdmChangeOrderItem> itemList;

    /** 审批记录时间轴 */
    private List<PdmChangeAuditLog> auditLogList;

    /** 审批动作（仅接收参数：true=通过 false=驳回） */
    private Boolean approved;

    /** 执行明细ID（仅接收参数） */
    private Long itemId;

    public Long getChangeId() { return changeId; }
    public void setChangeId(Long changeId) { this.changeId = changeId; }
    public String getChangeNo() { return changeNo; }
    public void setChangeNo(String changeNo) { this.changeNo = changeNo; }
    public String getChangeName() { return changeName; }
    public void setChangeName(String changeName) { this.changeName = changeName; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getChangeLevel() { return changeLevel; }
    public void setChangeLevel(String changeLevel) { this.changeLevel = changeLevel; }
    public String getUrgentLevel() { return urgentLevel; }
    public void setUrgentLevel(String urgentLevel) { this.urgentLevel = urgentLevel; }
    public String getChangeContent() { return changeContent; }
    public void setChangeContent(String changeContent) { this.changeContent = changeContent; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public String getEffectAnalysis() { return effectAnalysis; }
    public void setEffectAnalysis(String effectAnalysis) { this.effectAnalysis = effectAnalysis; }
    public String getChangeStatus() { return changeStatus; }
    public void setChangeStatus(String changeStatus) { this.changeStatus = changeStatus; }
    public Date getSubmitTime() { return submitTime; }
    public void setSubmitTime(Date submitTime) { this.submitTime = submitTime; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }
    public Date getCloseTime() { return closeTime; }
    public void setCloseTime(Date closeTime) { this.closeTime = closeTime; }
    public String getCloseBy() { return closeBy; }
    public void setCloseBy(String closeBy) { this.closeBy = closeBy; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public List<PdmChangeOrderItem> getItemList() { return itemList; }
    public void setItemList(List<PdmChangeOrderItem> itemList) { this.itemList = itemList; }
    public List<PdmChangeAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PdmChangeAuditLog> auditLogList) { this.auditLogList = auditLogList; }
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
}
