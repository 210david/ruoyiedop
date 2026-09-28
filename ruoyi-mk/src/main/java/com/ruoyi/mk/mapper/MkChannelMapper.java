package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkChannel;

/**
 * 渠道账号 数据层
 *
 * @author ruoyi
 */
public interface MkChannelMapper
{
    public MkChannel selectChannelById(Long channelId);

    public List<MkChannel> selectChannelList(MkChannel channel);

    public int insertChannel(MkChannel channel);

    public int updateChannel(MkChannel channel);

    public int deleteChannelByIds(Long[] channelIds);
}
