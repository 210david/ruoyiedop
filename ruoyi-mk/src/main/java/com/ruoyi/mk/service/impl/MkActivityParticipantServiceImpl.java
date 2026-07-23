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
        lead.setLeadSource("活动");
        lead.setActivityId(p.getActivityId());
        lead.setCreateBy(SecurityUtils.getUsername());
        mkLeadService.insertLead(lead);
        // 回写leadId
        mkActivityParticipantMapper.updateLeadId(participantId, lead.getLeadId());
        return lead.getLeadId();
    }
}
