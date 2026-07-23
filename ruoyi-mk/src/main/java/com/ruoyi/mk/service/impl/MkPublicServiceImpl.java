package com.ruoyi.mk.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkActivity;
import com.ruoyi.mk.domain.MkActivityParticipant;
import com.ruoyi.mk.mapper.MkActivityMapper;
import com.ruoyi.mk.mapper.MkActivityParticipantMapper;
import com.ruoyi.mk.service.IMkPublicService;

/**
 * 营销活动公开接口 Service实现（P3-15: 报名链接）
 *
 * <p>安全防护体系：
 * <ol>
 *   <li>令牌验证：活动ID + register_token 双重校验，防篡改</li>
 *   <li>状态校验：活动必须处于可报名状态</li>
 *   <li>时间校验：活动未结束才能报名</li>
 *   <li>去重校验：同一手机号不可重复报名</li>
 *   <li>容量校验：报名人数不超过目标人数</li>
 *   <li>并发锁：Redis分布式锁防并发重复提交</li>
 *   <li>输入校验：手机号/邮箱格式、长度限制</li>
 *   <li>信息脱敏：公开接口仅返回必要字段</li>
 * </ol>
 *
 * @author ruoyi
 */
@Service
public class MkPublicServiceImpl implements IMkPublicService
{
    private static final Logger log = LoggerFactory.getLogger(MkPublicServiceImpl.class);

    /** 手机号正则（中国大陆） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /** 签到码字符集（去除易混淆字符） */
    private static final String SIGN_IN_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    /** Redis报名锁前缀 */
    private static final String REGISTER_LOCK_PREFIX = "mk:register:lock:";

    /** Redis报名锁过期时间（秒） */
    private static final int REGISTER_LOCK_TTL = 30;

    @Autowired
    private MkActivityMapper mkActivityMapper;

    @Autowired
    private MkActivityParticipantMapper mkActivityParticipantMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取活动公开信息（脱敏后）
     */
    @Override
    public Map<String, Object> getPublicActivityInfo(Long activityId, String token)
    {
        // 安全层1：参数校验
        if (activityId == null || StringUtils.isEmpty(token))
        {
            throw new ServiceException("无效的访问链接");
        }

        // 安全层2：令牌验证
        MkActivity activity = mkActivityMapper.selectActivityByToken(activityId, token);
        if (activity == null)
        {
            throw new ServiceException("活动不存在或链接已失效");
        }

        // 安全层3：状态校验
        String status = activity.getActivityStatus();
        if (!"1".equals(status) && !"2".equals(status))
        {
            throw new ServiceException("当前活动暂不支持报名");
        }

        // 安全层8：信息脱敏 - 仅返回报名所需字段
        Map<String, Object> info = new HashMap<>();
        info.put("activityId", activity.getActivityId());
        info.put("activityName", activity.getActivityName());
        info.put("activityType", activity.getActivityType());
        info.put("activityForm", activity.getActivityForm());
        info.put("startTime", activity.getStartTime());
        info.put("endTime", activity.getEndTime());
        info.put("location", activity.getLocation());
        info.put("targetCount", activity.getTargetCount());
        info.put("summary", activity.getSummary());
        info.put("content", activity.getContent());
        info.put("signupCount", activity.getSignupCount());

        // 计算剩余名额
        if (activity.getTargetCount() != null && activity.getTargetCount() > 0)
        {
            int remaining = activity.getTargetCount() - (activity.getSignupCount() != null ? activity.getSignupCount() : 0);
            info.put("remainingCount", Math.max(0, remaining));
        }

        return info;
    }

    /**
     * 公开报名提交
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(MkActivityParticipant participant, String token)
    {
        Long activityId = participant.getActivityId();

        // 安全层1：参数校验
        if (activityId == null || StringUtils.isEmpty(token))
        {
            throw new ServiceException("无效的访问链接");
        }

        // 安全层7：输入校验
        validateRegistrationInput(participant);

        // 安全层2：令牌验证
        MkActivity activity = mkActivityMapper.selectActivityByToken(activityId, token);
        if (activity == null)
        {
            throw new ServiceException("活动不存在或链接已失效");
        }

        // 安全层3：状态校验
        String status = activity.getActivityStatus();
        if (!"1".equals(status) && !"2".equals(status))
        {
            throw new ServiceException("当前活动暂不支持报名");
        }

        // 安全层4：时间校验 - 活动已结束不能报名
        if (activity.getEndTime() != null && activity.getEndTime().before(new Date()))
        {
            throw new ServiceException("活动已结束，无法报名");
        }

        // 安全层5：去重校验 - 同一手机号不可重复报名
        MkActivityParticipant existing = mkActivityParticipantMapper.selectByActivityAndPhone(activityId, participant.getContactPhone());
        if (existing != null)
        {
            throw new ServiceException("您已报名该活动，请勿重复提交");
        }

        // 安全层6：容量校验
        if (activity.getTargetCount() != null && activity.getTargetCount() > 0)
        {
            int currentCount = mkActivityParticipantMapper.countByActivityId(activityId);
            if (currentCount >= activity.getTargetCount())
            {
                throw new ServiceException("活动报名人数已满");
            }
        }

        // 安全层6：Redis并发锁 - 防止并发重复提交
        String lockKey = REGISTER_LOCK_PREFIX + activityId + ":" + participant.getContactPhone();
        Boolean lockAcquired = redisCache.setIfAbsent(lockKey, "1", REGISTER_LOCK_TTL, TimeUnit.SECONDS);
        if (lockAcquired == null || !lockAcquired)
        {
            throw new ServiceException("正在处理中，请稍候再试");
        }

        try
        {
            // 再次检查去重（双重保障）
            existing = mkActivityParticipantMapper.selectByActivityAndPhone(activityId, participant.getContactPhone());
            if (existing != null)
            {
                throw new ServiceException("您已报名该活动，请勿重复提交");
            }

            // 生成签到码
            String signInCode = generateSignInCode();

            // 设置报名信息
            participant.setParticipateStatus("0"); // 报名状态
            participant.setSource("1"); // 来源：报名链接
            participant.setSignInCode(signInCode);
            participant.setCreateBy("报名链接");
            participant.setRemark(null); // 清除用户输入的备注，防止XSS

            // 插入数据库
            mkActivityParticipantMapper.insertParticipant(participant);

            log.info("活动报名成功: activityId={}, phone={}, signInCode={}", activityId, participant.getContactPhone(), signInCode);

            // 返回报名结果（脱敏）
            Map<String, Object> result = new HashMap<>();
            result.put("participantId", participant.getParticipantId());
            result.put("signInCode", signInCode);
            result.put("activityName", activity.getActivityName());
            result.put("contactName", participant.getContactName());
            result.put("contactPhone", maskPhone(participant.getContactPhone()));
            return result;
        }
        finally
        {
            // 释放锁
            redisCache.deleteObject(lockKey);
        }
    }

    /**
     * 签到码签到
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> signInByCode(String signInCode)
    {
        // 参数校验
        if (StringUtils.isEmpty(signInCode) || signInCode.length() != 8)
        {
            throw new ServiceException("无效的签到码");
        }

        // 查询参与人
        MkActivityParticipant participant = mkActivityParticipantMapper.selectBySignInCode(signInCode);
        if (participant == null)
        {
            throw new ServiceException("签到码无效，请检查后重试");
        }

        // 状态校验
        if ("1".equals(participant.getParticipateStatus()))
        {
            throw new ServiceException("您已签到，请勿重复操作");
        }

        if ("2".equals(participant.getParticipateStatus()))
        {
            throw new ServiceException("您的参与状态为「未出席」，无法签到");
        }

        // 活动状态校验
        MkActivity activity = mkActivityMapper.selectActivityById(participant.getActivityId());
        if (activity == null)
        {
            throw new ServiceException("活动不存在");
        }

        // 签到时间窗口校验：活动开始前1小时 ~ 活动结束
        Date now = new Date();
        if (activity.getStartTime() != null)
        {
            long oneHourBeforeStart = activity.getStartTime().getTime() - 3600000L;
            if (now.getTime() < oneHourBeforeStart)
            {
                throw new ServiceException("签到尚未开放，请在活动开始前1小时内签到");
            }
        }
        if (activity.getEndTime() != null && now.after(activity.getEndTime()))
        {
            throw new ServiceException("活动已结束，无法签到");
        }

        // 执行签到
        mkActivityParticipantMapper.signIn(participant.getParticipantId(), "现场签到");

        log.info("签到成功: participantId={}, signInCode={}", participant.getParticipantId(), signInCode);

        Map<String, Object> result = new HashMap<>();
        result.put("participantId", participant.getParticipantId());
        result.put("activityName", activity.getActivityName());
        result.put("contactName", participant.getContactName());
        result.put("signTime", now);
        return result;
    }

    /**
     * 为活动生成报名令牌
     */
    @Override
    public String generateRegisterToken(Long activityId)
    {
        if (activityId == null)
        {
            throw new ServiceException("活动ID不能为空");
        }

        MkActivity activity = mkActivityMapper.selectActivityById(activityId);
        if (activity == null)
        {
            throw new ServiceException("活动不存在");
        }

        // 生成新的令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        mkActivityMapper.updateRegisterToken(activityId, token);

        log.info("生成活动报名令牌: activityId={}, token={}", activityId, token);
        return token;
    }

    // ==================== 私有安全方法 ====================

    /**
     * 报名输入校验
     */
    private void validateRegistrationInput(MkActivityParticipant participant)
    {
        // 企业名称：2-100字符
        String companyName = participant.getCompanyName();
        if (StringUtils.isEmpty(companyName) || companyName.length() < 2 || companyName.length() > 100)
        {
            throw new ServiceException("企业名称长度应为2-100个字符");
        }

        // 联系人姓名：2-20字符
        String contactName = participant.getContactName();
        if (StringUtils.isEmpty(contactName) || contactName.length() < 2 || contactName.length() > 20)
        {
            throw new ServiceException("姓名长度应为2-20个字符");
        }

        // 手机号：中国大陆手机格式
        String contactPhone = participant.getContactPhone();
        if (StringUtils.isEmpty(contactPhone) || !PHONE_PATTERN.matcher(contactPhone).matches())
        {
            throw new ServiceException("请输入有效的手机号码");
        }

        // 邮箱：可选，但填写了必须格式正确
        String contactEmail = participant.getContactEmail();
        if (StringUtils.isNotEmpty(contactEmail))
        {
            if (contactEmail.length() > 100 || !EMAIL_PATTERN.matcher(contactEmail).matches())
            {
                throw new ServiceException("请输入有效的邮箱地址");
            }
        }

        // 职位：可选，最大50字符
        String position = participant.getPosition();
        if (StringUtils.isNotEmpty(position) && position.length() > 50)
        {
            throw new ServiceException("职位长度不能超过50个字符");
        }
    }

    /**
     * 生成8位签到码（去除易混淆字符）
     */
    private String generateSignInCode()
    {
        StringBuilder sb = new StringBuilder(8);
        UUID uuid = UUID.randomUUID();
        long bits = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++)
        {
            int index = (int) (Math.abs(bits) % SIGN_IN_CODE_CHARS.length());
            sb.append(SIGN_IN_CODE_CHARS.charAt(index));
            bits = bits >> 3;
        }
        return sb.toString();
    }

    /**
     * 手机号脱敏（中间4位用*代替）
     */
    private String maskPhone(String phone)
    {
        if (phone == null || phone.length() != 11)
        {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
