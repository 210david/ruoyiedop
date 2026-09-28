package com.ruoyi.mk.service.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkActivity;
import com.ruoyi.mk.domain.MkActivityChannelLink;
import com.ruoyi.mk.mapper.MkActivityChannelLinkMapper;
import com.ruoyi.mk.mapper.MkActivityMapper;
import com.ruoyi.mk.service.IMkChannelLinkService;
import com.ruoyi.mk.service.IMkPublicService;

/**
 * 归因链接 Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MkChannelLinkServiceImpl implements IMkChannelLinkService
{
    /** 归因码字符集（去除易混淆字符 0O1lI） */
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private MkActivityChannelLinkMapper linkMapper;

    @Autowired
    private MkActivityMapper mkActivityMapper;

    @Autowired
    private IMkPublicService mkPublicService;

    @Override
    public List<MkActivityChannelLink> selectLinkList(MkActivityChannelLink link)
    {
        return linkMapper.selectActivityChannelLinkList(link);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MkActivityChannelLink> generateLinks(Long activityId, Long[] channelIds)
    {
        MkActivity activity = mkActivityMapper.selectActivityById(activityId);
        if (activity == null)
        {
            throw new ServiceException("活动不存在");
        }
        if (!"1".equals(activity.getActivityStatus()) && !"2".equals(activity.getActivityStatus()))
        {
            throw new ServiceException("仅报名中/进行中的活动可生成归因链接");
        }
        if (channelIds == null || channelIds.length == 0)
        {
            throw new ServiceException("请选择渠道");
        }

        // 已有该活动链接的渠道跳过
        MkActivityChannelLink query = new MkActivityChannelLink();
        query.setActivityId(activityId);
        List<MkActivityChannelLink> existing = linkMapper.selectActivityChannelLinkList(query);
        java.util.Set<Long> existingChannelIds = new java.util.HashSet<>();
        for (MkActivityChannelLink l : existing)
        {
            existingChannelIds.add(l.getChannelId());
        }

        // 生成并持久化 token（报名链接安全层）
        String token = mkPublicService.generateRegisterToken(activityId);
        String username = SecurityUtils.getUsername();

        List<MkActivityChannelLink> result = new ArrayList<>();
        for (Long channelId : channelIds)
        {
            if (existingChannelIds.contains(channelId))
            {
                continue;
            }
            MkActivityChannelLink link = new MkActivityChannelLink();
            link.setActivityId(activityId);
            link.setChannelId(channelId);
            link.setChannelCode(generateUniqueCode());
            link.setCreateBy(username);
            linkMapper.insertActivityChannelLink(link);
            link.setRegisterUrl(buildRegisterUrl(activityId, token, link.getChannelCode()));
            result.add(link);
        }
        return result;
    }

    @Override
    public MkActivityChannelLink trackByCode(String channelCode)
    {
        MkActivityChannelLink link = linkMapper.selectByChannelCode(channelCode);
        if (link != null)
        {
            linkMapper.increaseClickCount(link.getLinkId());
            link.setClickCount(link.getClickCount() == null ? 1 : link.getClickCount() + 1);
        }
        return link;
    }

    @Override
    public int deleteLinkByIds(Long[] linkIds)
    {
        return linkMapper.deleteActivityChannelLinkByIds(linkIds);
    }

    /** 生成去混淆的8位随机归因码（UK 冲突重试） */
    private String generateUniqueCode()
    {
        for (int i = 0; i < 10; i++)
        {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int j = 0; j < CODE_LENGTH; j++)
            {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (linkMapper.selectByChannelCode(code) == null)
            {
                return code;
            }
        }
        throw new ServiceException("归因码生成失败，请重试");
    }

    /** 拼接 H5 报名页地址（与活动报名链接格式一致） */
    private String buildRegisterUrl(Long activityId, String token, String channelCode)
    {
        return "/h5/register/" + activityId + "?token=" + token + "&ch=" + channelCode;
    }
}
