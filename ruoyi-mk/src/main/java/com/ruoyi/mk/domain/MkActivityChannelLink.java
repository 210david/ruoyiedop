package com.ruoyi.mk.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 活动归因链接对象 mk_activity_channel_link
 *
 * @author ruoyi
 */
public class MkActivityChannelLink extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 链接ID */
    private Long linkId;

    /** 活动ID */
    private Long activityId;

    /** 渠道ID */
    private Long channelId;

    /** 归因码（8位随机码） */
    @Excel(name = "归因码")
    private String channelCode;

    /** 点击数 */
    @Excel(name = "点击数")
    private Integer clickCount;

    /** 报名数 */
    @Excel(name = "报名数")
    private Integer signupCount;

    /** 线索数 */
    @Excel(name = "线索数")
    private Integer leadCount;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 活动名称（关联查询） */
    private String activityName;

    /** 渠道账号名（关联查询） */
    private String channelName;

    /** 渠道类型（关联查询，来自 mk_channel） */
    private String channelTypeQuery;

    /** 报名链接（生成后返回，非持久化） */
    private String registerUrl;

    public Long getLinkId() { return linkId; }
    public void setLinkId(Long linkId) { this.linkId = linkId; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public Integer getClickCount() { return clickCount; }
    public void setClickCount(Integer clickCount) { this.clickCount = clickCount; }
    public Integer getSignupCount() { return signupCount; }
    public void setSignupCount(Integer signupCount) { this.signupCount = signupCount; }
    public Integer getLeadCount() { return leadCount; }
    public void setLeadCount(Integer leadCount) { this.leadCount = leadCount; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }
    public String getChannelTypeQuery() { return channelTypeQuery; }
    public void setChannelTypeQuery(String channelTypeQuery) { this.channelTypeQuery = channelTypeQuery; }
    public String getRegisterUrl() { return registerUrl; }
    public void setRegisterUrl(String registerUrl) { this.registerUrl = registerUrl; }
}
