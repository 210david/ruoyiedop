package com.ruoyi.pms.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购计划表 pms_purchase_plan
 *
 * @author ruoyi
 */
public class PmsPurchasePlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 计划ID */
    private Long planId;

    /** 计划单号 */
    @Excel(name = "计划单号")
    private String planNo;

    /** 计划标题 */
    @Excel(name = "计划标题")
    private String title;

    /** 状态（0-草稿 1-待审批 2-审批通过 3-执行中 4-已完成 5-已关闭 6-已驳回） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=待审批,2=审批通过,3=执行中,4=已完成,5=已关闭,6=已驳回")
    private String status;

    /** 计划类型（0-年度 1-季度 2-月度 3-临时） */
    @Excel(name = "计划类型", readConverterExp = "0=年度,1=季度,2=月度,3=临时")
    private String planType;

    /** 计划开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划开始日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date startDate;

    /** 计划结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划结束日期", width = 20, dateFormat = "yyyy-MM-dd")
    private Date endDate;

    /** 预算总金额 */
    @Excel(name = "预算总金额")
    private BigDecimal budgetAmount;

    /** 实际采购金额 */
    @Excel(name = "实际采购金额")
    private BigDecimal actualAmount;

    /** 计划说明 */
    private String description;

    /** 编制人ID */
    private Long plannerId;

    /** 编制人姓名 */
    @Excel(name = "编制人")
    private String plannerName;

    /** 编制部门ID */
    private Long deptId;

    /** 编制部门名称 */
    @Excel(name = "编制部门")
    private String deptName;

    /** 审批人 */
    @Excel(name = "审批人")
    private String auditBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审批时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审批意见 */
    private String auditOpinion;

    /** 删除标志 */
    private String delFlag;

    /** 采购计划明细列表 */
    private List<PmsPurchasePlanDetail> detailList;

    /** 审核日志列表 */
    private List<PmsPlanAuditLog> auditLogList;

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public String getPlanNo() { return planNo; }
    public void setPlanNo(String planNo) { this.planNo = planNo; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getPlannerId() { return plannerId; }
    public void setPlannerId(Long plannerId) { this.plannerId = plannerId; }

    public String getPlannerName() { return plannerName; }
    public void setPlannerName(String plannerName) { this.plannerName = plannerName; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String auditBy) { this.auditBy = auditBy; }

    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }

    public String getAuditOpinion() { return auditOpinion; }
    public void setAuditOpinion(String auditOpinion) { this.auditOpinion = auditOpinion; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    public List<PmsPurchasePlanDetail> getDetailList() { return detailList; }
    public void setDetailList(List<PmsPurchasePlanDetail> detailList) { this.detailList = detailList; }

    public List<PmsPlanAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<PmsPlanAuditLog> auditLogList) { this.auditLogList = auditLogList; }

    @Override
    public String toString() {
        return "PmsPurchasePlan{" +
                "planId=" + planId +
                ", planNo='" + planNo + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", planType='" + planType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", budgetAmount=" + budgetAmount +
                ", actualAmount=" + actualAmount +
                ", description='" + description + '\'' +
                ", plannerId=" + plannerId +
                ", plannerName='" + plannerName + '\'' +
                ", deptId=" + deptId +
                ", deptName='" + deptName + '\'' +
                ", auditBy='" + auditBy + '\'' +
                ", auditTime=" + auditTime +
                ", auditOpinion='" + auditOpinion + '\'' +
                ", delFlag='" + delFlag + '\'' +
                ", detailList=" + detailList +
                '}';
    }
}
