package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 互动记录 mk_interaction
 *
 * @author ruoyi
 */
public class MkInteraction extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long recordId;

    /** 客户ID */
    private Long customerId;

    /** 客户名称 */
    @Excel(name = "客户名称")
    private String customerName;

    /** 联系人ID */
    private Long contactId;

    /** 联系人名称 */
    @Excel(name = "联系人")
    private String contactName;

    /** 商机ID */
    private Long opportunityId;

    /** 线索ID */
    private Long leadId;

    /** 互动类型 */
    @Excel(name = "互动类型")
    private String interactType;

    /** 互动时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "互动时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date interactTime;

    /** 互动内容 */
    @Excel(name = "互动内容")
    private String content;

    /** 跟进人ID */
    private Long userId;

    /** 跟进人名称 */
    @Excel(name = "跟进人")
    private String userName;

    /** 下次跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextTime;

    /** 下次跟进内容 */
    private String nextContent;

    /** 附件 */
    private String attachment;

    /** 删除标志 */
    private String delFlag;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public Long getOpportunityId() { return opportunityId; }
    public void setOpportunityId(Long opportunityId) { this.opportunityId = opportunityId; }
    public Long getLeadId() { return leadId; }
    public void setLeadId(Long leadId) { this.leadId = leadId; }
    public String getInteractType() { return interactType; }
    public void setInteractType(String interactType) { this.interactType = interactType; }
    public Date getInteractTime() { return interactTime; }
    public void setInteractTime(Date interactTime) { this.interactTime = interactTime; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public Date getNextTime() { return nextTime; }
    public void setNextTime(Date nextTime) { this.nextTime = nextTime; }
    public String getNextContent() { return nextContent; }
    public void setNextContent(String nextContent) { this.nextContent = nextContent; }
    public String getAttachment() { return attachment; }
    public void setAttachment(String attachment) { this.attachment = attachment; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
