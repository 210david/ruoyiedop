package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkActivityParticipant;
import org.apache.ibatis.annotations.Param;

public interface MkActivityParticipantMapper
{
    public List<MkActivityParticipant> selectParticipantList(MkActivityParticipant participant);
    public MkActivityParticipant selectParticipantById(Long participantId);
    public int insertParticipant(MkActivityParticipant participant);
    public int updateParticipant(MkActivityParticipant participant);
    public int deleteParticipantByIds(Long[] participantIds);

    /** 按活动ID查询参与人列表 (P0-3) */
    public List<MkActivityParticipant> selectByActivityId(@Param("activityId") Long activityId);

    /** 签到 (P1-5前置) */
    public int signIn(@Param("participantId") Long participantId, @Param("updateBy") String updateBy);

    /** 回写leadId (P1-5) */
    public int updateLeadId(@Param("participantId") Long participantId, @Param("leadId") Long leadId);

    /** P3-15: 根据活动ID和手机号查询参与人（报名去重） */
    public MkActivityParticipant selectByActivityAndPhone(@Param("activityId") Long activityId, @Param("contactPhone") String contactPhone);

    /** P3-15: 根据签到码查询参与人（现场签到） */
    public MkActivityParticipant selectBySignInCode(@Param("signInCode") String signInCode);

    /** P3-15: 统计活动报名人数 */
    public int countByActivityId(@Param("activityId") Long activityId);
}
