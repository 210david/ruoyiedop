package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 线索 mk_lead
 *
 * @author ruoyi
 */
public class MkLead extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 线索ID */
    private Long leadId;

    /** 线索编号 */
    @Excel(name = "线索编号")
    private String leadNo;

    /** 企业名称 */
    @Excel(name = "企业名称")
    private String companyName;

    /** 联系人姓名 */
    @Excel(name = "联系人")
    private String contactName;

    /** 联系人手机号 */
    @Excel(name = "手机号")
    private String contactPhone;

    /** 联系人邮箱 */
    @Excel(name = "邮箱")
    private String contactEmail;

    /** 职位 */
    @Excel(name = "职位")
    private String position;

    /** 所属行业 */
    @Excel(name = "所属行业", dictType = "marketing_industry")
    private String industry;

    /** 企业规模 */
    @Excel(name = "企业规模", dictType = "marketing_company_size")
    private String companySize;

    /** 详细地址 */
    @Excel(name = "详细地址")
    private String address;

    /** 线索来源 */
    @Excel(name = "线索来源", dictType = "marketing_customer_source")
    private String leadSource;

    /** 来源活动ID */
    private Long activityId;

    /** 需求描述 */
    @Excel(name = "需求描述")
    private String requirementDesc;

    /** 线索评分 */
    @Excel(name = "评分")
    private Integer leadScore;

    /** 线索等级 */
    @Excel(name = "等级", dictType = "marketing_lead_grade")
    private String leadGrade;

    /** 线索状态 */
    @Excel(name = "状态", dictType = "marketing_lead_status")
    private String leadStatus;

    /** 是否在公海 */
    private String isPublic;

    /** 负责人ID */
    private Long userId;

    /** 负责人名称 */
    @Excel(name = "负责人")
    private String userName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 领取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;

    /** 领取审批状态（0未申请 1待审批 2已批准 3已拒绝） */
    private String receiveStatus;

    /** 领取审批备注 */
    private String receiveRemark;

    /** 领取申请人ID */
    private Long receiveApplyUserId;

    /** 领取申请人名称 */
    private String receiveApplyUserName;

    /** 领取申请时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receiveApplyTime;

    /** 最后跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastFollowTime;

    /** 无效原因 */
    private String ineffectiveReason;

    /** 无效说明 */
    private String ineffectiveRemark;

    /** 转化后的客户ID */
    private Long convertCustomerId;

    /** 转化时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date convertTime;

    /** 删除标志 */
    private String delFlag;

    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public String getLeadNo() { return leadNo; }
    public void setLeadNo(String leadNo) { this.leadNo = leadNo; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLeadSource() { return leadSource; }
    public void setLeadSource(String leadSource) { this.leadSource = leadSource; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public String getRequirementDesc() { return requirementDesc; }
    public void setRequirementDesc(String requirementDesc) { this.requirementDesc = requirementDesc; }
    public Integer getLeadScore() { return leadScore; }
    public void setLeadScore(Integer leadScore) { this.leadScore = leadScore; }
    public String getLeadGrade() { return leadGrade; }
    public void setLeadGrade(String leadGrade) { this.leadGrade = leadGrade; }
    public String getLeadStatus() { return leadStatus; }
    public void setLeadStatus(String leadStatus) { this.leadStatus = leadStatus; }
    public String getIsPublic() { return isPublic; }
    public void setIsPublic(String isPublic) { this.isPublic = isPublic; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }
    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }
    public Date getReceiveTime() { return receiveTime; }
    public void setReceiveTime(Date receiveTime) { this.receiveTime = receiveTime; }
    public String getReceiveStatus() { return receiveStatus; }
    public void setReceiveStatus(String receiveStatus) { this.receiveStatus = receiveStatus; }
    public String getReceiveRemark() { return receiveRemark; }
    public void setReceiveRemark(String receiveRemark) { this.receiveRemark = receiveRemark; }
    public Long getReceiveApplyUserId() { return receiveApplyUserId; }
    public void setReceiveApplyUserId(Long receiveApplyUserId) { this.receiveApplyUserId = receiveApplyUserId; }
    public String getReceiveApplyUserName() { return receiveApplyUserName; }
    public void setReceiveApplyUserName(String receiveApplyUserName) { this.receiveApplyUserName = receiveApplyUserName; }
    public Date getReceiveApplyTime() { return receiveApplyTime; }
    public void setReceiveApplyTime(Date receiveApplyTime) { this.receiveApplyTime = receiveApplyTime; }
    public Date getLastFollowTime() { return lastFollowTime; }
    public void setLastFollowTime(Date lastFollowTime) { this.lastFollowTime = lastFollowTime; }
    public String getIneffectiveReason() { return ineffectiveReason; }
    public void setIneffectiveReason(String ineffectiveReason) { this.ineffectiveReason = ineffectiveReason; }
    public String getIneffectiveRemark() { return ineffectiveRemark; }
    public void setIneffectiveRemark(String ineffectiveRemark) { this.ineffectiveRemark = ineffectiveRemark; }
    public Long getConvertCustomerId() { return convertCustomerId; }
    public void setConvertCustomerId(Long convertCustomerId) { this.convertCustomerId = convertCustomerId; }
    public Date getConvertTime() { return convertTime; }
    public void setConvertTime(Date convertTime) { this.convertTime = convertTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
