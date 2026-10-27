package com.ruoyi.mk.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 推广效果数据对象 mk_promotion_data
 *
 * @author ruoyi
 */
public class MkPromotionData extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 数据ID */
    private Long dataId;

    /** 内容包ID */
    @Excel(name = "内容包ID")
    private Long packageId;

    /** 内容包标题（导入用，内容包ID留空时按标题匹配） */
    @Excel(name = "内容包标题")
    private String importPackageTitle;

    /** 数据日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "数据日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dataDate;

    /** 曝光量 */
    @Excel(name = "曝光量")
    private Integer exposureCount;

    /** 阅读量 */
    @Excel(name = "阅读量")
    private Integer readCount;

    /** 点赞量 */
    @Excel(name = "点赞量")
    private Integer likeCount;

    /** 评论量 */
    @Excel(name = "评论量")
    private Integer commentCount;

    /** 带来线索量 */
    @Excel(name = "带来线索量")
    private Integer leadCount;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 内容包标题（关联查询） */
    private String packageTitle;

    /** 所属内容ID（关联查询，筛选/回显用） */
    private Long contentId;

    /** 所属内容标题（关联查询） */
    private String contentTitle;

    /** 渠道ID（关联查询，来自 mk_content_package） */
    private Long channelId;

    /** 渠道类型（关联查询） */
    private String channelType;

    public Long getDataId() { return dataId; }
    public void setDataId(Long dataId) { this.dataId = dataId; }
    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
    public Date getDataDate() { return dataDate; }
    public void setDataDate(Date dataDate) { this.dataDate = dataDate; }
    public Integer getExposureCount() { return exposureCount; }
    public void setExposureCount(Integer exposureCount) { this.exposureCount = exposureCount; }
    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getLeadCount() { return leadCount; }
    public void setLeadCount(Integer leadCount) { this.leadCount = leadCount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getPackageTitle() { return packageTitle; }
    public void setPackageTitle(String packageTitle) { this.packageTitle = packageTitle; }
    public String getImportPackageTitle() { return importPackageTitle; }
    public void setImportPackageTitle(String importPackageTitle) { this.importPackageTitle = importPackageTitle; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }
    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }
    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
}
