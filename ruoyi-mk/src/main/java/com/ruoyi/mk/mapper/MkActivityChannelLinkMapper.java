package com.ruoyi.mk.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkActivityChannelLink;

/**
 * 活动归因链接 数据层
 *
 * @author ruoyi
 */
public interface MkActivityChannelLinkMapper
{
    public MkActivityChannelLink selectActivityChannelLinkById(Long linkId);

    public List<MkActivityChannelLink> selectActivityChannelLinkList(MkActivityChannelLink activityChannelLink);

    public int insertActivityChannelLink(MkActivityChannelLink activityChannelLink);

    public int updateActivityChannelLink(MkActivityChannelLink activityChannelLink);

    public int deleteActivityChannelLinkByIds(Long[] linkIds);

    /** 根据归因码查询链接（报名归因） */
    public MkActivityChannelLink selectByChannelCode(@Param("channelCode") String channelCode);

    /** 点击数+1 */
    public int increaseClickCount(@Param("linkId") Long linkId);

    /** 报名数+1 */
    public int increaseSignupCount(@Param("linkId") Long linkId);

    /** 线索数+1 */
    public int increaseLeadCount(@Param("linkId") Long linkId);
}
