package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkActivityChannelLink;

/**
 * 归因链接 Service接口
 *
 * @author ruoyi
 */
public interface IMkChannelLinkService
{
    /** 按活动/渠道查询归因链接列表（join 渠道 + 计数） */
    public List<MkActivityChannelLink> selectLinkList(MkActivityChannelLink link);

    /**
     * 批量生成归因链接（活动×渠道），重复渠道跳过
     *
     * @return 生成的链接列表（含 registerUrl）
     */
    public List<MkActivityChannelLink> generateLinks(Long activityId, Long[] channelIds);

    /** 归因跟踪：点击数+1，返回链接信息 */
    public MkActivityChannelLink trackByCode(String channelCode);

    public int deleteLinkByIds(Long[] linkIds);
}
