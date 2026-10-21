package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkChannel;

/**
 * 渠道账号 Service接口
 *
 * @author ruoyi
 */
public interface IMkChannelService
{
    public List<MkChannel> selectChannelList(MkChannel channel);

    public MkChannel selectChannelById(Long channelId);

    public int insertChannel(MkChannel channel);

    public int updateChannel(MkChannel channel);

    public int deleteChannelByIds(Long[] channelIds);

    /**
     * 查询全部正常状态渠道（供选择器使用）
     */
    public List<MkChannel> selectAllChannels();
}
