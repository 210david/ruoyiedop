package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.domain.MkActivityParticipant;
import com.ruoyi.mk.domain.MkLead;
import com.ruoyi.mk.mapper.MkActivityParticipantMapper;
import com.ruoyi.mk.service.IMkActivityParticipantService;
import com.ruoyi.mk.service.IMkLeadService;

/**
 * 活动参与人 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkActivityParticipantServiceImpl implements IMkActivityParticipantService
{
    @Autowired
    private MkActivityParticipantMapper mkActivityParticipantMapper;

    @Autowired
    private com.ruoyi.mk.mapper.MkActivityMapper mkActivityMapper;

    @Autowired
    private com.ruoyi.mk.mapper.MkChannelMapper mkChannelMapper;

    @Autowired
    private com.ruoyi.mk.mapper.MkActivityChannelLinkMapper mkActivityChannelLinkMapper;

    @Autowired
    private IMkLeadService mkLeadService;

    @Override
    public List<MkActivityParticipant> selectParticipantList(MkActivityParticipant participant)
    {
        return mkActivityParticipantMapper.selectParticipantList(participant);
    }

    @Override
    public MkActivityParticipant selectParticipantById(Long participantId)
    {
        return mkActivityParticipantMapper.selectParticipantById(participantId);
    }

    @Override
    public int insertParticipant(MkActivityParticipant participant)
    {
        checkActivityJoinable(participant.getActivityId());
        if (participant.getParticipateStatus() == null)
        {
            participant.setParticipateStatus("0");
        }
        if (participant.getSource() == null)
        {
            participant.setSource("0");
        }
        return mkActivityParticipantMapper.insertParticipant(participant);
    }

    /** 活动已结束(3)/已取消(4)时禁止报名，已结束(3)同时禁止签到 */
    private void checkActivityJoinable(Long activityId)
    {
        if (activityId == null)
        {
            return;
        }
        com.ruoyi.mk.domain.MkActivity activity = mkActivityMapper.selectActivityById(activityId);
        String status = activity != null ? activity.getActivityStatus() : null;
        if ("3".equals(status))
        {
            throw new ServiceException("活动【" + (activity != null ? activity.getActivityName() : activityId) + "】已结束，不允许报名");
        }
        if ("4".equals(status))
        {
            throw new ServiceException("活动【" + (activity != null ? activity.getActivityName() : activityId) + "】已取消，不允许报名");
        }
    }

    /** 活动已结束(3)/已取消(4)时禁止签到 */
    private void checkActivitySignable(Long activityId)
    {
        if (activityId == null)
        {
            return;
        }
        com.ruoyi.mk.domain.MkActivity activity = mkActivityMapper.selectActivityById(activityId);
        String status = activity != null ? activity.getActivityStatus() : null;
        if ("3".equals(status) || "4".equals(status))
        {
            throw new ServiceException("活动【" + (activity != null ? activity.getActivityName() : activityId) + "】已结束或已取消，不允许签到");
        }
    }

    @Override
    public int updateParticipant(MkActivityParticipant participant)
    {
        return mkActivityParticipantMapper.updateParticipant(participant);
    }

    @Override
    public int deleteParticipantByIds(Long[] participantIds)
    {
        return mkActivityParticipantMapper.deleteParticipantByIds(participantIds);
    }

    /** P0-3: 按活动ID查询参与人 */
    @Override
    public List<MkActivityParticipant> selectByActivityId(Long activityId)
    {
        return mkActivityParticipantMapper.selectByActivityId(activityId);
    }

    /** P1-5: 签到 */
    @Override
    public int signIn(Long participantId)
    {
        MkActivityParticipant p = mkActivityParticipantMapper.selectParticipantById(participantId);
        if (p == null)
        {
            throw new ServiceException("参与人不存在");
        }
        checkActivitySignable(p.getActivityId());
        return mkActivityParticipantMapper.signIn(participantId, SecurityUtils.getUsername());
    }

    /** P1-5: 转线索 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convertToLead(Long participantId)
    {
        MkActivityParticipant p = mkActivityParticipantMapper.selectParticipantById(participantId);
        if (p == null)
        {
            throw new ServiceException("参与人不存在");
        }
        if (p.getLeadId() != null)
        {
            throw new ServiceException("该参与人已转化线索，请勿重复操作");
        }
        // 创建线索
        MkLead lead = new MkLead();
        lead.setCompanyName(p.getCompanyName());
        lead.setContactName(p.getContactName());
        lead.setContactPhone(p.getContactPhone());
        lead.setContactEmail(p.getContactEmail());
        lead.setPosition(p.getPosition());
        lead.setLeadSource("5"); // 线索来源字典 marketing_customer_source：5=活动
        lead.setActivityId(p.getActivityId());
        // 推广归因：参与人带渠道时传递给线索，并累加归因链接线索数
        if (p.getChannelId() != null)
        {
            lead.setChannelId(p.getChannelId());
            com.ruoyi.mk.domain.MkChannel channel = mkChannelMapper.selectChannelById(p.getChannelId());
            if (channel != null)
            {
                lead.setChannelType(channel.getChannelType());
            }
            try
            {
                com.ruoyi.mk.domain.MkActivityChannelLink linkQuery = new com.ruoyi.mk.domain.MkActivityChannelLink();
                linkQuery.setActivityId(p.getActivityId());
                linkQuery.setChannelId(p.getChannelId());
                java.util.List<com.ruoyi.mk.domain.MkActivityChannelLink> links = mkActivityChannelLinkMapper.selectActivityChannelLinkList(linkQuery);
                if (links != null && !links.isEmpty())
                {
                    mkActivityChannelLinkMapper.increaseLeadCount(links.get(0).getLinkId());
                }
            }
            catch (Exception ignored)
            {
                // 归因计数失败不影响线索转化
            }
        }
        lead.setCreateBy(SecurityUtils.getUsername());
        mkLeadService.insertLead(lead);
        // 回写leadId
        mkActivityParticipantMapper.updateLeadId(participantId, lead.getLeadId());
        return lead.getLeadId();
    }
}
