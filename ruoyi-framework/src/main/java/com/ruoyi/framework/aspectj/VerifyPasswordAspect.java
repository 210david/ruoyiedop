package com.ruoyi.framework.aspectj;

import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.VerifyPassword;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 二次密码验证切面
 * 拦截 @VerifyPassword 注解的方法，验证用户密码后继续执行
 * 
 * @author ruoyi
 */
@Aspect
@Component
public class VerifyPasswordAspect
{
    private static final Logger log = LoggerFactory.getLogger(VerifyPasswordAspect.class);

    @Around("@annotation(verifyPassword)")
    public Object doAround(ProceedingJoinPoint point, VerifyPassword verifyPassword) throws Throwable
    {
        String paramName = verifyPassword.paramName();
        String verifyPwd = extractVerifyPassword(point, paramName);
        if (StringUtils.isEmpty(verifyPwd))
        {
            throw new ServiceException("二次验证密码不能为空");
        }
        // 获取当前登录用户的密码进行比对
        String currentPassword = SecurityUtils.getLoginUser().getPassword();
        if (!SecurityUtils.matchesPassword(verifyPwd, currentPassword))
        {
            log.warn("用户[{}]二次密码验证失败", SecurityUtils.getUsername());
            throw new ServiceException("密码验证失败，无法执行此操作");
        }
        log.info("用户[{}]二次密码验证通过", SecurityUtils.getUsername());
        return point.proceed();
    }

    /**
     * 从方法参数中提取密码字段
     */
    private String extractVerifyPassword(ProceedingJoinPoint point, String paramName)
    {
        Object[] args = point.getArgs();
        if (args == null || args.length == 0)
        {
            return null;
        }
        for (Object arg : args)
        {
            if (arg == null)
            {
                continue;
            }
            // 如果是String类型且参数名匹配
            if (arg instanceof String)
            {
                // 单个String参数时直接尝试作为密码
            }
            // 尝试从对象中通过反射获取字段值
            try
            {
                Field field = arg.getClass().getDeclaredField(paramName);
                field.setAccessible(true);
                Object value = field.get(arg);
                if (value != null)
                {
                    return value.toString();
                }
            }
            catch (NoSuchFieldException e)
            {
                // 字段不存在，继续尝试下一个参数
            }
            catch (Exception e)
            {
                log.debug("提取密码字段异常: {}", e.getMessage());
            }
        }
        return null;
    }
}
