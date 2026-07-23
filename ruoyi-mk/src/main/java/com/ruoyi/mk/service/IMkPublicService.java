package com.ruoyi.mk.service;

import java.util.Map;
import com.ruoyi.mk.domain.MkActivityParticipant;

/**
 * 营销活动公开接口 Service接口（P3-15: 报名链接）
 *
 * @author ruoyi
 */
public interface IMkPublicService
{
    /**
     * 获取活动公开信息（脱敏后，仅返回报名所需字段）
     *
     * @param activityId 活动ID
     * @param token 安全令牌
     * @return 活动信息（脱敏）
     */
    public Map<String, Object> getPublicActivityInfo(Long activityId, String token);

    /**
     * 公开报名提交
     *
     * @param participant 报名信息
     * @param token 安全令牌
     * @return 报名结果（含签到码）
     */
    public Map<String, Object> register(MkActivityParticipant participant, String token);

    /**
     * 签到码签到（现场扫码签到）
     *
     * @param signInCode 签到码
     * @return 签到结果
     */
    public Map<String, Object> signInByCode(String signInCode);

    /**
     * 为活动生成报名令牌（管理端调用）
     *
     * @param activityId 活动ID
     * @return 令牌
     */
    public String generateRegisterToken(Long activityId);
}
