package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkActivityParticipant;

/**
 * 活动参与人 Service接口
 *
 * @author ruoyi
 */
public interface IMkActivityParticipantService
{
    public List<MkActivityParticipant> selectParticipantList(MkActivityParticipant participant);
    public MkActivityParticipant selectParticipantById(Long participantId);
    public int insertParticipant(MkActivityParticipant participant);
    public int updateParticipant(MkActivityParticipant participant);
    public int deleteParticipantByIds(Long[] participantIds);

    /** P0-3: 按活动ID查询参与人 */
    public List<MkActivityParticipant> selectByActivityId(Long activityId);

    /** P1-5: 签到 */
    public int signIn(Long participantId);

    /** P1-5: 转线索 */
    public Long convertToLead(Long participantId);
}
