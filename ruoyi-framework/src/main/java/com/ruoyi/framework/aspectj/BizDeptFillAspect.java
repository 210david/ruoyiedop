package com.ruoyi.framework.aspectj;

import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 业务数据归属自动填充切面
 *
 * 新增业务数据时，自动为实体填充 dept_id（归属部门 = 当前登录人所在部门），
 * 作为数据权限（@DataScope）的过滤依据。仅在 deptId 为空时填充，
 * 业务上显式指定归属（如分配、转移、按业务对象判定）的代码不受影响。
 *
 * 范围：各业务模块 ServiceImpl 的 insert 开头方法。
 */
@Aspect
@Component
public class BizDeptFillAspect
{
    private static final Logger log = LoggerFactory.getLogger(BizDeptFillAspect.class);

    /** 匹配各业务模块 ServiceImpl 的新增方法 */
    @Pointcut("execution(* com.ruoyi..service.impl.*ServiceImpl.insert*(..))")
    public void bizInsertPointcut()
    {
    }

    @Before("bizInsertPointcut()")
    public void fillDeptId(JoinPoint joinPoint)
    {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || args[0] == null)
        {
            return;
        }
        Object entity = args[0];
        if (!(entity instanceof BaseEntity))
        {
            return;
        }
        try
        {
            Method setter = entity.getClass().getMethod("setDeptId", Long.class);
            Method getter = entity.getClass().getMethod("getDeptId");
            Object current = getter.invoke(entity);
            if (current == null)
            {
                Long deptId = SecurityUtils.getDeptId();
                if (deptId != null)
                {
                    setter.invoke(entity, deptId);
                }
            }
        }
        catch (Exception e)
        {
            // 实体无 deptId 属性（主数据/共享表）或未登录上下文（定时任务）时静默跳过
            log.debug("BizDeptFillAspect skip: {}", e.getMessage());
        }
    }

    /** 判断是否有归属填充能力（供扩展使用） */
    public static boolean supportsDeptId(Object entity)
    {
        if (entity == null)
        {
            return false;
        }
        try
        {
            entity.getClass().getMethod("setDeptId", Long.class);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /** 供无法走 AOP 的内部调用显式填充 */
    public static void fillIfAbsent(Object entity)
    {
        if (entity instanceof BaseEntity && StringUtils.isNotNull(entity))
        {
            try
            {
                Method getter = entity.getClass().getMethod("getDeptId");
                if (getter.invoke(entity) == null)
                {
                    entity.getClass().getMethod("setDeptId", Long.class).invoke(entity, SecurityUtils.getDeptId());
                }
            }
            catch (Exception ignored)
            {
            }
        }
    }
}
