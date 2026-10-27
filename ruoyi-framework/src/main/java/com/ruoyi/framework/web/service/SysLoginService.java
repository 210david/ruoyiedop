package com.ruoyi.framework.web.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.BlackListException;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.exception.user.UserNotExistsException;
import com.ruoyi.common.exception.user.UserPasswordExpireException;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysLoginService
{
    private static final Logger log = LoggerFactory.getLogger(SysLoginService.class);

    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private SysPasswordService passwordService;

    @Value("${user.password.onlineCount:1}")
    private int onlineCount;

    @Value("${user.password.onlineMode:kick}")
    private String onlineMode;

    /** 默认允许多终端登录 */
    private static final String DEFAULT_MULTI_LOGIN = "true";

    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid)
    {
        // 验证码校验
        validateCaptcha(username, code, uuid);
        // 登录前置校验
        loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        // 首次登录检测：如果用户从未登录过（login_date为null），标记为首次登录
        if (loginUser.getUser().getLoginDate() == null)
        {
            loginUser.setFirstLogin(true);
            log.info("用户[{}]为首次登录", username);
        }
        recordLoginInfo(loginUser.getUserId());
        // 密码过期检查（过期不改抛异常，标记pwdExpired标志）
        try
        {
            passwordService.checkPasswordExpired(loginUser.getUser());
        }
        catch (UserPasswordExpireException e)
        {
            loginUser.setPwdExpired(true);
            log.warn("用户[{}]密码已过期", username);
        }
        // 并发登录控制（从系统参数读取多终端登录开关）
        handleConcurrentLogin(username);
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 判断是否允许同一账号多终端登录
     * 从系统参数 sys.login.multiLoginEnabled 读取
     * 
     * @return true=允许多终端登录，false=不允许（单点登录）
     */
    private boolean isMultiLoginEnabled()
    {
        String value = configService.selectConfigByKey("sys.login.multiLoginEnabled");
        if (StringUtils.isEmpty(value))
        {
            return Convert.toBool(DEFAULT_MULTI_LOGIN);
        }
        return Convert.toBool(value);
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        userService.updateLoginInfo(userId, IpUtils.getIpAddr(), DateUtils.getNowDate());
    }

    /**
     * 并发登录控制（限制同时在线用户数）
     * 多终端登录由系统参数 sys.login.multiLoginEnabled 控制：
     *   true  = 允许多终端登录（不做踢出/拒绝处理）
     *   false = 不允许多终端登录（走 onlineCount/onlineMode 逻辑）
     * 
     * @param username 用户名
     */
    private void handleConcurrentLogin(String username)
    {
        // 从系统参数读取多终端登录开关
        if (isMultiLoginEnabled())
        {
            // 允许多终端登录，不做任何处理
            return;
        }
        // 不允许多终端登录时，使用 onlineCount=1 限制
        int maxOnline = 1;
        // 扫描所有在线token，找出属于当前用户的
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        if (keys == null || keys.isEmpty())
        {
            return;
        }
        List<LoginUser> sameUserTokens = new ArrayList<>();
        for (String key : keys)
        {
            LoginUser cachedUser = redisCache.getCacheObject(key);
            if (cachedUser != null && username.equals(cachedUser.getUsername()))
            {
                sameUserTokens.add(cachedUser);
            }
        }
        int currentOnlineCount = sameUserTokens.size();
        if (currentOnlineCount >= maxOnline)
        {
            // 踢出模式：删除最旧的登录session（单点登录场景）
            sameUserTokens.sort(Comparator.comparingLong(LoginUser::getLoginTime));
            int needRemove = currentOnlineCount - maxOnline + 1;
            for (int i = 0; i < needRemove && i < sameUserTokens.size(); i++)
            {
                LoginUser toRemove = sameUserTokens.get(i);
                if (StringUtils.isNotEmpty(toRemove.getToken()))
                {
                    String userKey = CacheConstants.LOGIN_TOKEN_KEY + toRemove.getToken();
                    redisCache.deleteObject(userKey);
                    // [Token排查] 记录被踢会话的设备信息，便于定位"登录状态已过期"由互踢导致
                    // 说明：logback 的 file_info/file_error 均为 LevelFilter 精确匹配（只收 INFO/ERROR），WARN 不落盘，故用 error
                    log.error("[Token排查] 并发登录踢出 用户[{}] 设备[{}({})] ip[{}] 登录时间[{}]",
                            username, toRemove.getBrowser(), toRemove.getOs(), toRemove.getIpaddr(),
                            toRemove.getLoginTime() == null ? "-" : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(toRemove.getLoginTime())));
                }
            }
        }
    }
}
