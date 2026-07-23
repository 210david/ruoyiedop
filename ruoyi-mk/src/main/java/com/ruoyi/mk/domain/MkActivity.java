package com.ruoyi.mk.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 营销活动 mk_activity
 *
 * @author ruoyi
 */
public class MkActivity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 活动ID */
    private Long activityId;

    /** 活动编号 */
    @Excel(name = "活动编号")
    private String activityNo;

    /** 活动名称 */
    @Excel(name = "活动名称")
    private String activityName;

    /** 活动类型 */
    @Excel(name = "活动类型", dictType = "marketing_activity_type")
    private String activityType;

    /** 活动形式 */
    @Excel(name = "活动形式", dictType = "marketing_activity_form")
    private String activityForm;

    /** 活动状态 */
    @Excel(name = "活动状态", dictType = "marketing_activity_status")
    private String activityStatus;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 活动地点/链接 */
    private String location;

    /** 活动预算 */
    @Excel(name = "预算")
    private BigDecimal budget;

    /** 实际花费 */
    @Excel(name = "实际花费")
    private BigDecimal cost;

    /** 目标人数 */
    private Integer targetCount;

    /** 目标线索数 */
    private Integer targetLeadCount;

    /** 负责人ID */
    private Long userId;

    /** 负责人名称 */
    @Excel(name = "负责人")
    private String userName;

    /** 负责人联系方式 */
    @Excel(name = "联系方式")
    private String ownerPhone;

/** 部门ID */
private Long deptId;

/** 部门名称 */
@Excel(name = "部门")
private String deptName;

/** 活动简介 */
    private String summary;

    /** 活动详情 */
    private String content;

    /** 实际参与人数 */
    private Integer actualCount;

    /** 获取线索数 */
    private Integer leadCount;

    /** MQL数量 */
    private Integer mqlCount;

    /** SQL数量 */
    private Integer sqlCount;

    /** 转化商机数 */
    private Integer opportunityCount;

    /** 成交金额 */
    private BigDecimal dealAmount;

    /** 实际ROI(%) */
    private BigDecimal actualRoi;

    /** 删除标志 */
    private String delFlag;

    /** 附件资料（逗号分隔的文件路径） */
    private String attachments;

    /** 报名链接安全令牌 */
    private String registerToken;

    // ====== 以下为非数据库字段，用于列表汇总指标 (P2-9) ======
    /** 报名人数 */
    private Integer signupCount;
    /** 签到人数 */
    private Integer signedCount;
    /** 转化线索数 */
    private Integer convertedLeadCount;

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public String getActivityNo() { return activityNo; }
    public void setActivityNo(String activityNo) { this.activityNo = activityNo; }
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getActivityForm() { return activityForm; }
    public void setActivityForm(String activityForm) { this.activityForm = activityForm; }
    public String getActivityStatus() { return activityStatus; }
    public void setActivityStatus(String activityStatus) { this.activityStatus = activityStatus; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public Integer getTargetCount() { return targetCount; }
    public void setTargetCount(Integer targetCount) { this.targetCount = targetCount; }
    public Integer getTargetLeadCount() { return targetLeadCount; }
    public void setTargetLeadCount(Integer targetLeadCount) { this.targetLeadCount = targetLeadCount; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }
public Long getDeptId() { return deptId; }
public void setDeptId(Long deptId) { this.deptId = deptId; }
public String getDeptName() { return deptName; }
public void setDeptName(String deptName) { this.deptName = deptName; }
public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getActualCount() { return actualCount; }
    public void setActualCount(Integer actualCount) { this.actualCount = actualCount; }
    public Integer getLeadCount() { return leadCount; }
    public void setLeadCount(Integer leadCount) { this.leadCount = leadCount; }
    public Integer getMqlCount() { return mqlCount; }
    public void setMqlCount(Integer mqlCount) { this.mqlCount = mqlCount; }
    public Integer getSqlCount() { return sqlCount; }
    public void setSqlCount(Integer sqlCount) { this.sqlCount = sqlCount; }
    public Integer getOpportunityCount() { return opportunityCount; }
    public void setOpportunityCount(Integer opportunityCount) { this.opportunityCount = opportunityCount; }
    public BigDecimal getDealAmount() { return dealAmount; }
    public void setDealAmount(BigDecimal dealAmount) { this.dealAmount = dealAmount; }
    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
    public BigDecimal getActualRoi() { return actualRoi; }
    public void setActualRoi(BigDecimal actualRoi) { this.actualRoi = actualRoi; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public Integer getSignupCount() { return signupCount; }
    public void setSignupCount(Integer signupCount) { this.signupCount = signupCount; }
    public Integer getSignedCount() { return signedCount; }
    public void setSignedCount(Integer signedCount) { this.signedCount = signedCount; }
    public Integer getConvertedLeadCount() { return convertedLeadCount; }
    public void setConvertedLeadCount(Integer convertedLeadCount) { this.convertedLeadCount = convertedLeadCount; }
    public String getRegisterToken() { return registerToken; }
    public void setRegisterToken(String registerToken) { this.registerToken = registerToken; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
}
