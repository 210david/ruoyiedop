package com.ruoyi.safety.domain;

import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class SafetyRemind extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long remindId;
    @Excel(name = "提醒类型")
    private String remindType;
    private Long relatedId;
    @Excel(name = "关联名称")
    private String relatedName;
    @Excel(name = "提醒内容")
    private String remindContent;
    @Excel(name = "到期日期", width = 120)
    private Date expireDate;
    @Excel(name = "提前天数")
    private Integer remindDays;
    @Excel(name = "提醒状态", readConverterExp = "0=未读,1=已读,2=已处理,3=已忽略")
    private String remindStatus;
    @Excel(name = "通知层级")
    private String notifyLevel;
    private Long toPersonId;
    @Excel(name = "接收人")
    private String toPersonName;
    @Excel(name = "已读时间", width = 160)
    private Date readTime;
    @Excel(name = "处理人")
    private String handleBy;
    @Excel(name = "处理时间", width = 160)
    private Date handleTime;
    @Excel(name = "处理结果")
    private String handleResult;
    private Long enterpriseId;
    private String delFlag;

    public Long getRemindId() { return remindId; }
    public void setRemindId(Long remindId) { this.remindId = remindId; }
    public String getRemindType() { return remindType; }
    public void setRemindType(String remindType) { this.remindType = remindType; }
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    public String getRelatedName() { return relatedName; }
    public void setRelatedName(String relatedName) { this.relatedName = relatedName; }
    public String getRemindContent() { return remindContent; }
    public void setRemindContent(String remindContent) { this.remindContent = remindContent; }
    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }
    public Integer getRemindDays() { return remindDays; }
    public void setRemindDays(Integer remindDays) { this.remindDays = remindDays; }
    public String getRemindStatus() { return remindStatus; }
    public void setRemindStatus(String remindStatus) { this.remindStatus = remindStatus; }
    public String getNotifyLevel() { return notifyLevel; }
    public void setNotifyLevel(String notifyLevel) { this.notifyLevel = notifyLevel; }
    public Long getToPersonId() { return toPersonId; }
    public void setToPersonId(Long toPersonId) { this.toPersonId = toPersonId; }
    public String getToPersonName() { return toPersonName; }
    public void setToPersonName(String toPersonName) { this.toPersonName = toPersonName; }
    public Date getReadTime() { return readTime; }
    public void setReadTime(Date readTime) { this.readTime = readTime; }
    public String getHandleBy() { return handleBy; }
    public void setHandleBy(String handleBy) { this.handleBy = handleBy; }
    public Date getHandleTime() { return handleTime; }
    public void setHandleTime(Date handleTime) { this.handleTime = handleTime; }
    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String handleResult) { this.handleResult = handleResult; }
    public Long getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(Long enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}