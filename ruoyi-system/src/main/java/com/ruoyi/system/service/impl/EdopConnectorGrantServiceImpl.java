package com.ruoyi.system.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Date;
import java.util.Deque;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.EdopConnectorGrant;
import com.ruoyi.system.mapper.EdopConnectorGrantMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IEdopConnectorGrantService;

/**
 * 连接器逐用户授权 服务实现
 *
 * 设计要点（方案 v1.2 / 详细设计 v1.0）：
 *  - grant_token 明文仅颁发时返回一次，库存 SHA-256 哈希
 *  - 90 天滚动续期（exchange 即续期），停用/吊销立即断权
 *  - 授权绑 user_id 不绑手机号：换绑不停权，phone_snapshot 仅做检测记录
 *  - login 端点按用户名 1 分钟 5 次限流防爆破
 *
 * @author edop
 */
@Service
public class EdopConnectorGrantServiceImpl implements IEdopConnectorGrantService
{
    private static final Logger log = LoggerFactory.getLogger(EdopConnectorGrantServiceImpl.class);

    /** 授权有效期（滚动）：90 天 */
    private static final long GRANT_VALIDITY_MS = 90L * 24 * 3600 * 1000;

    /** login 防爆破：按用户名 1 分钟 5 次 */
    private static final int RATE_LIMIT = 5;
    private static final long RATE_WINDOW_MS = 60_000L;
    private static final Map<String, Deque<Long>> LOGIN_ATTEMPTS = new ConcurrentHashMap<>();

    @Autowired
    private EdopConnectorGrantMapper grantMapper;

    @Autowired
    private SysUserMapper userMapper;

    private static final SecureRandom RANDOM = new SecureRandom();

    /** 登录并创建授权（匿名场景：网关 /grant 兜底页使用） */
    @Override
    public EdopConnectorGrant createGrant(String username, String password, String clientDesc)
    {
        checkRateLimit(username);

        SysUser user = userMapper.selectUserByUserName(username);
        if (user == null || "2".equals(user.getDelFlag()))
        {
            throw new ServiceException("用户不存在或已删除");
        }
        if (!"0".equals(user.getStatus()))
        {
            throw new ServiceException("账号已停用，无法授权");
        }
        if (password == null || !SecurityUtils.matchesPassword(password, user.getPassword()))
        {
            throw new ServiceException("用户名或密码错误");
        }
        return issueGrant(user, clientDesc);
    }

    /** 为已登录用户直接创建授权（系统内入口：登录态即身份凭证，免二次输密码） */
    @Override
    public EdopConnectorGrant createGrantForUser(SysUser user, String clientDesc)
    {
        if (user == null || "2".equals(user.getDelFlag()))
        {
            throw new ServiceException("用户不存在或已删除");
        }
        if (!"0".equals(user.getStatus()))
        {
            throw new ServiceException("账号已停用，无法授权");
        }
        return issueGrant(user, clientDesc);
    }

    /**
     * 颁发授权（公共逻辑）：生成 grant_token → SHA-256 落库 → 明文仅此一次返回
     */
    private EdopConnectorGrant issueGrant(SysUser user, String clientDesc)
    {
        // 生成 grant_token：24 字节随机 → base64url（32 字符，128bit 熵）
        byte[] raw = new byte[24];
        RANDOM.nextBytes(raw);
        String grantToken = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        EdopConnectorGrant grant = new EdopConnectorGrant();
        grant.setUserId(user.getUserId());
        grant.setGrantTokenHash(sha256Hex(grantToken));
        grant.setPhoneSnapshot(user.getPhonenumber() == null ? "" : user.getPhonenumber());
        grant.setClientDesc(clientDesc == null || clientDesc.isEmpty() ? "WorkBuddy" : clientDesc);
        grant.setStatus("0");
        grant.setExpireTime(new Date(System.currentTimeMillis() + GRANT_VALIDITY_MS));
        grant.setLastUsedTime(DateUtils.getNowDate());
        grant.setCreateBy(user.getUserName());
        grantMapper.insertGrant(grant);
        // 明文 token 经临时字段带出（insert SQL 不含该字段，不落库）
        grant.setGrantToken(grantToken);

        log.info("[edop-grant] 授权创建 user={} grantId={} desc={}", user.getUserName(), grant.getGrantId(), grant.getClientDesc());
        return grant;
    }

    /** 校验 grant_token 并滚动续期 */
    @Override
    public EdopConnectorGrant validateGrant(String grantToken)
    {
        if (grantToken == null || grantToken.length() < 32)
        {
            throw new ServiceException("无效的授权凭证", 401);
        }
        EdopConnectorGrant grant = grantMapper.selectGrantByTokenHash(sha256Hex(grantToken));
        if (grant == null || !"0".equals(grant.getStatus()))
        {
            throw new ServiceException("授权已失效或被吊销，请重新授权", 401);
        }
        if (grant.getExpireTime() != null && grant.getExpireTime().before(DateUtils.getNowDate()))
        {
            grantMapper.updateGrantStatus(grant.getGrantId(), "1");
            throw new ServiceException("授权已过期，请重新授权", 401);
        }

        SysUser user = userMapper.selectUserByUserName(grant.getUserName());
        if (user == null || "2".equals(user.getDelFlag()) || !"0".equals(user.getStatus()))
        {
            throw new ServiceException("账号已停用，连接器授权同步失效", 401);
        }

        // 换绑检测（默认仅记录，不强制断权；后续可加配置开关强制重授权）
        String currentPhone = user.getPhonenumber() == null ? "" : user.getPhonenumber();
        if (!currentPhone.equals(grant.getPhoneSnapshot()))
        {
            log.warn("[edop-grant] 手机号快照不一致 grantId={} snapshot={} current={}",
                    grant.getGrantId(), grant.getPhoneSnapshot(), currentPhone);
        }

        // 滚动续期
        grantMapper.touchGrant(grant.getGrantId(), new Date(System.currentTimeMillis() + GRANT_VALIDITY_MS));
        return grant;
    }

    @Override
    public List<EdopConnectorGrant> selectGrantList(EdopConnectorGrant grant)
    {
        return grantMapper.selectGrantList(grant);
    }

    @Override
    public List<EdopConnectorGrant> selectGrantsByUserId(Long userId)
    {
        return grantMapper.selectGrantsByUserId(userId);
    }

    @Override
    public int revokeByAdmin(Long grantId)
    {
        return grantMapper.updateGrantStatus(grantId, "1");
    }

    @Override
    public int revokeByOwner(Long grantId, Long userId)
    {
        EdopConnectorGrant grant = grantMapper.selectGrantByGrantId(grantId);
        if (grant == null || !grant.getUserId().equals(userId))
        {
            throw new ServiceException("无权操作该授权");
        }
        return grantMapper.updateGrantStatus(grantId, "1");
    }

    /* ---------------- 内部工具 ---------------- */

    private void checkRateLimit(String username)
    {
        long now = System.currentTimeMillis();
        Deque<Long> deque = LOGIN_ATTEMPTS.computeIfAbsent(username, k -> new ArrayDeque<>());
        synchronized (deque)
        {
            while (!deque.isEmpty() && now - deque.peekFirst() > RATE_WINDOW_MS)
            {
                deque.pollFirst();
            }
            if (deque.size() >= RATE_LIMIT)
            {
                throw new ServiceException("尝试过于频繁，请 1 分钟后再试");
            }
            deque.addLast(now);
        }
        // 防内存膨胀：超过 1000 个用户名记录时清理过期项
        if (LOGIN_ATTEMPTS.size() > 1000)
        {
            LOGIN_ATTEMPTS.entrySet().removeIf(e -> e.getValue().isEmpty()
                    || (e.getValue().peekLast() != null && now - e.getValue().peekLast() > RATE_WINDOW_MS));
        }
    }

    private String sha256Hex(String input)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash)
            {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            throw new ServiceException("凭证哈希计算失败");
        }
    }
}
