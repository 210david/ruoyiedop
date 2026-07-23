package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 商机 mk_opportunity
 *
 * @author ruoyi
 */
public class MkOpportunity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商机ID */
    private Long opportunityId;

    /** 商机编号 */
    @Excel(name = "商机编号")
    private String opportunityNo;

    /** 商机名称 */
    @Excel(name = "商机名称")
    private String opportunityName;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 主要联系人ID */
    private Long contactId;

    /** 联系人名称 */
    private String contactName;

    /** 来源线索ID */
    private Long leadId;

    /** 商机来源 */
    @Excel(name = "商机来源")
    private String opportunitySource;

    /** 预计金额 */
    @Excel(name = "预计金额")
    private BigDecimal expectedAmount;

    /** 预计成交日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "预计成交日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date expectedDate;

    /** 销售阶段编码 */
    private String stageCode;

    /** 销售阶段名称 */
    @Excel(name = "销售阶段")
    private String stageName;

    /** 赢率 */
    @Excel(name = "赢率(%)")
    private Integer winRate;

    /** 加权金额 */
    @Excel(name = "加权金额")
    private BigDecimal weightedAmount;

    /** 商机状态 */
    @Excel(name = "商机状态", dictType = "marketing_opportunity_status")
    private String opportunityStatus;

    /** 竞争对手 */
    private String competitor;

    /** 客户痛点 */
    private String painPoint;

    /** 解决方案 */
    private String solution;

    /** 输单原因 */
    private String lostReason;

    /** 输单说明 */
    private String lostRemark;

    /** 实际成交金额 */
    private BigDecimal actualAmount;

    /** 实际成交日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualDate;

    /** 负责人ID */
    private Long userId;

    /** 负责人名称 */
    @Excel(name = "负责人")
    private String userName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 下一步行动 */
    private String nextAction;

    /** 下次行动时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextTime;

    /** 进入当前阶段时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date stageEnterTime;

    /** 删除标志 */
    private String delFlag;

    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public String getOpportunityNo() { return opportunityNo; }
    public void setOpportunityNo(String opportunityNo) { this.opportunityNo = opportunityNo; }
    public String getOpportunityName() { return opportunityName; }
    public void setOpportunityName(String opportunityName) { this.opportunityName = opportunityName; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public String getOpportunitySource() { return opportunitySource; }
    public void setOpportunitySource(String opportunitySource) { this.opportunitySource = opportunitySource; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(BigDecimal expectedAmount) { this.expectedAmount = expectedAmount; }
    public Date getExpectedDate() { return expectedDate; }
    public void setExpectedDate(Date expectedDate) { this.expectedDate = expectedDate; }
    public String getStageCode() { return stageCode; }
    public void setStageCode(String stageCode) { this.stageCode = stageCode; }
    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }
    public Integer getWinRate() { return winRate; }
    public void setWinRate(Integer winRate) { this.winRate = winRate; }
    public BigDecimal getWeightedAmount() { return weightedAmount; }
    public void setWeightedAmount(BigDecimal weightedAmount) { this.weightedAmount = weightedAmount; }
    public String getOpportunityStatus() { return opportunityStatus; }
    public void setOpportunityStatus(String opportunityStatus) { this.opportunityStatus = opportunityStatus; }
    public String getCompetitor() { return competitor; }
    public void setCompetitor(String competitor) { this.competitor = competitor; }
    public String getPainPoint() { return painPoint; }
    public void setPainPoint(String painPoint) { this.painPoint = painPoint; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    public String getLostReason() { return lostReason; }
    public void setLostReason(String lostReason) { this.lostReason = lostReason; }
    public String getLostRemark() { return lostRemark; }
    public void setLostRemark(String lostRemark) { this.lostRemark = lostRemark; }
    public BigDecimal getActualAmount() { return actualAmount; }
    public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
    public Date getActualDate() { return actualDate; }
    public void setActualDate(Date actualDate) { this.actualDate = actualDate; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getNextAction() { return nextAction; }
    public void setNextAction(String nextAction) { this.nextAction = nextAction; }
    public Date getNextTime() { return nextTime; }
    public void setNextTime(Date nextTime) { this.nextTime = nextTime; }
    public Date getStageEnterTime() { return stageEnterTime; }
    public void setStageEnterTime(Date stageEnterTime) { this.stageEnterTime = stageEnterTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
