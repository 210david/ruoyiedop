package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 活动参与人 mk_activity_participant
 *
 * @author ruoyi
 */
public class MkActivityParticipant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 参与ID */
    private Long participantId;

    /** 活动ID */
    private Long activityId;

    /** 关联线索ID */
    private Long leadId;

    /** 关联联系人ID */
    private Long contactId;

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

    /** 参与状态 */
    @Excel(name = "参与状态", dictType = "marketing_participate_status")
    private String participateStatus;

    /** 来源(0=手动 1=报名链接 2=导入) */
    private String source;

    /** 签到时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date signTime;

    /** 签到码（报名成功后生成，用于现场扫码签到） */
    private String signInCode;

    /** 活动名称（关联查询） */
    private String activityName;

    public Long getParticipantId() { return participantId; }
    public void setParticipantId(Long participantId) { this.participantId = participantId; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
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
    public String getParticipateStatus() { return participateStatus; }
    public void setParticipateStatus(String participateStatus) { this.participateStatus = participateStatus; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Date getSignTime() { return signTime; }
    public void setSignTime(Date signTime) { this.signTime = signTime; }
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    public String getSignInCode() { return signInCode; }
    public void setSignInCode(String signInCode) { this.signInCode = signInCode; }
}
