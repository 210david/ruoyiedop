package com.ruoyi.mk.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 内容主档对象 mk_content
 *
 * @author ruoyi
 */
public class MkContent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 内容ID */
    private Long contentId;

    /** 内容编号（CNT前缀） */
    @Excel(name = "内容编号")
    private String contentNo;

    /** 内容标题 */
    @Excel(name = "内容标题")
    private String title;

    /** 内容类型（0活动宣传 1产品介绍 2案例故事 3干货知识 4品牌宣传 5其他） */
    @Excel(name = "内容类型", dictType = "marketing_content_type")
    private String contentType;

    /** 创作主题/要点（AI生成输入） */
    private String theme;

    /** 关联活动ID（可选） */
    private Long activityId;

    /** 计划发布日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划发布日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date planPublishDate;

    /** 审核状态（0草稿 1待审核 2已通过 3已驳回） */
    @Excel(name = "审核状态", dictType = "marketing_content_audit_status")
    private String auditStatus;

    /** 审核人ID */
    private Long auditUserId;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 创建人ID */
    private Long userId;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 活动名称（关联查询） */
    private String activityName;

    /** 创建人昵称（关联查询） */
    private String userName;

    /** 审核人昵称（关联查询） */
    private String auditUserName;

    /** 内容包集合（子表） */
    private List<MkContentPackage> packageList;

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getContentNo() { return contentNo; }
    public void setContentNo(String contentNo) { this.contentNo = contentNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Date getPlanPublishDate() { return planPublishDate; }
    public void setPlanPublishDate(Date planPublishDate) { this.planPublishDate = planPublishDate; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public Long getAuditUserId() { return auditUserId; }
    public void setAuditUserId(Long auditUserId) { this.auditUserId = auditUserId; }
    public Date getAuditTime() { return auditTime; }
    public void setAuditTime(Date auditTime) { this.auditTime = auditTime; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getAuditUserName() { return auditUserName; }
    public void setAuditUserName(String auditUserName) { this.auditUserName = auditUserName; }
    /** 审核记录列表（非表字段） */
    private List<MkContentAuditLog> auditLogList;

    public List<MkContentAuditLog> getAuditLogList() { return auditLogList; }
    public void setAuditLogList(List<MkContentAuditLog> auditLogList) { this.auditLogList = auditLogList; }

    public List<MkContentPackage> getPackageList() { return packageList; }
    public void setPackageList(List<MkContentPackage> packageList) { this.packageList = packageList; }
}
