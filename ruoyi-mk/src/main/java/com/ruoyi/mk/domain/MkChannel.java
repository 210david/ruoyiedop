package com.ruoyi.mk.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 渠道账号对象 mk_channel
 *
 * @author ruoyi
 */
public class MkChannel extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 渠道ID */
    private Long channelId;

    /** 渠道类型（0抖音 1视频号 2小红书 3公众号 4官网 5其他） */
    @Excel(name = "渠道类型", dictType = "marketing_channel_type")
    private String channelType;

    /** 账号名称 */
    @Excel(name = "账号名称")
    private String accountName;

    /** 平台账号ID */
    private String accountId;

    /** 账号主页链接 */
    private String accountUrl;

    /** 粉丝数 */
    @Excel(name = "粉丝数")
    private Integer fansCount;

    /** 负责人ID */
    private Long userId;

    /** 渠道状态（0正常 1停用） */
    @Excel(name = "渠道状态", dictType = "sys_normal_disable")
    private String channelStatus;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 负责人昵称（关联查询） */
    private String userName;

    public Long getChannelId() { return channelId; }
    public void setChannelId(Long channelId) { this.channelId = channelId; }
    public String getChannelType() { return channelType; }
    public void setChannelType(String channelType) { this.channelType = channelType; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getAccountUrl() { return accountUrl; }
    public void setAccountUrl(String accountUrl) { this.accountUrl = accountUrl; }
    public Integer getFansCount() { return fansCount; }
    public void setFansCount(Integer fansCount) { this.fansCount = fansCount; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getChannelStatus() { return channelStatus; }
    public void setChannelStatus(String channelStatus) { this.channelStatus = channelStatus; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}
