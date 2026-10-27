package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 内容包对象 mk_content_package
 *
 * @author ruoyi
 */
public class MkContentPackage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 内容包ID */
    private Long packageId;

    /** 内容ID */
    private Long contentId;

    /** 渠道ID */
    private Long channelId;

    /** 渠道类型（冗余） */
    @Excel(name = "渠道类型", dictType = "marketing_channel_type")
    private String channelType;

    /** 内容包标题 */
    @Excel(name = "内容包标题")
    private String packageTitle;

    /** 内容包正文 */
    private String packageBody;

    /** 话题标签（逗号分隔） */
    @Excel(name = "话题标签")
    private String topicTags;

    /** 封面图URL */
    private String coverUrl;

    /** 引用素材ID（逗号分隔） */
    private String materialIds;

    /** 内容包状态（0待发布 1已发布 2已取消） */
    @Excel(name = "内容包状态", dictType = "marketing_package_status")
    private String packageStatus;

    /** 发布链接 */
    private String publishUrl;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 渠道账号名（关联查询） */
    private String channelName;

    /** 内容标题（关联查询） */
    private String contentTitle;

    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
    public String getPackageTitle() { return packageTitle; }
    public void setPackageTitle(String packageTitle) { this.packageTitle = packageTitle; }
    public String getPackageBody() { return packageBody; }
    public void setPackageBody(String packageBody) { this.packageBody = packageBody; }
    public String getTopicTags() { return topicTags; }
    public void setTopicTags(String topicTags) { this.topicTags = topicTags; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getMaterialIds() { return materialIds; }
    public void setMaterialIds(String materialIds) { this.materialIds = materialIds; }
    public String getPackageStatus() { return packageStatus; }
    public void setPackageStatus(String packageStatus) { this.packageStatus = packageStatus; }
    public String getPublishUrl() { return publishUrl; }
    public void setPublishUrl(String publishUrl) { this.publishUrl = publishUrl; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }
}
