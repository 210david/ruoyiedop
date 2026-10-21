package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkChannel;
import com.ruoyi.mk.mapper.MkChannelMapper;
import com.ruoyi.mk.service.IMkChannelService;

/**
 * 渠道账号 Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MkChannelServiceImpl implements IMkChannelService
{
    @Autowired
    private MkChannelMapper mkChannelMapper;

    @Override
    public List<MkChannel> selectChannelList(MkChannel channel)
    {
        return mkChannelMapper.selectChannelList(channel);
    }

    @Override
    public MkChannel selectChannelById(Long channelId)
    {
        return mkChannelMapper.selectChannelById(channelId);
    }

    @Override
    public int insertChannel(MkChannel channel)
    {
        channel.setCreateBy(SecurityUtils.getUsername());
        return mkChannelMapper.insertChannel(channel);
    }

    @Override
    public int updateChannel(MkChannel channel)
    {
        channel.setUpdateBy(SecurityUtils.getUsername());
        return mkChannelMapper.updateChannel(channel);
    }

    @Override
    public int deleteChannelByIds(Long[] channelIds)
    {
        return mkChannelMapper.deleteChannelByIds(channelIds);
    }

    @Override
    public List<MkChannel> selectAllChannels()
    {
        MkChannel query = new MkChannel();
        query.setChannelStatus("0");
        return mkChannelMapper.selectChannelList(query);
    }
}
