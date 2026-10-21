package com.ruoyi.framework.aspectj;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.annotation.EnableUndo;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 操作撤销AOP切面
 * 在 @EnableUndo 标记的方法执行前，将实体当前状态保存JSON快照到 sys_undo_log
 * 
 * @author ruoyi
 */
@Aspect
@Component
public class UndoLogAspect
{
    private static final Logger log = LoggerFactory.getLogger(UndoLogAspect.class);

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 环绕通知：在方法执行前保存数据快照
     */
    @Around("@annotation(enableUndo)")
    public Object around(ProceedingJoinPoint joinPoint, EnableUndo enableUndo) throws Throwable
    {
        // 执行前保存快照
        try
        {
            saveSnapshot(joinPoint, enableUndo);
        }
        catch (Exception e)
        {
            log.error("撤销日志-保存快照异常: {}", e.getMessage(), e);
        }

        // 执行原方法
        return joinPoint.proceed();
    }

    /**
     * 保存数据快照到 sys_undo_log
     */
    private void saveSnapshot(ProceedingJoinPoint joinPoint, EnableUndo enableUndo)
    {
        // 从方法参数中提取实体ID
        Object entityId = extractEntityId(joinPoint, enableUndo.key());
        if (entityId == null || StringUtils.isEmpty(entityId.toString()))
        {
            log.debug("撤销日志-无法提取实体ID，跳过快照保存（可能是新增操作）");
            return;
        }

        String entityType = getEntityType(joinPoint, enableUndo);
        // 查询当前数据库记录并转为JSON快照
        String snapshotJson = queryCurrentData(entityType, enableUndo.key(), entityId.toString());
        if (snapshotJson == null)
        {
            log.debug("撤销日志-未查询到实体[{}]ID[{}]的当前数据（可能已被删除）", entityType, entityId);
            return;
        }

        String operateBy = getCurrentUserName();
        LocalDateTime now = LocalDateTime.now();
        // 过期时间默认24小时后
        LocalDateTime expireTime = now.plusHours(24);

        String insertSql = "INSERT INTO sys_undo_log (entity_type, entity_id, data_snapshot, operate_type, create_by, create_time, expire_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true))
        {
            Connection conn = sqlSession.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(insertSql))
            {
                ps.setString(1, entityType);
                ps.setString(2, entityId.toString());
                ps.setString(3, snapshotJson);
                ps.setString(4, enableUndo.operateType());
                ps.setString(5, operateBy);
                ps.setObject(6, now);
                ps.setObject(7, expireTime);
                ps.executeUpdate();
                log.info("撤销日志-已保存实体[{}]ID[{}]的快照，操作人: {}", entityType, entityId, operateBy);
            }
        }
        catch (Exception e)
        {
            log.error("撤销日志-写入快照异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 查询当前数据库记录，返回JSON字符串
     */
    private String queryCurrentData(String tableName, String keyColumn, String entityId)
    {
        String selectSql = "SELECT * FROM " + tableName + " WHERE " + keyColumn + " = ?";
        try (SqlSession sqlSession = sqlSessionFactory.openSession())
        {
            Connection conn = sqlSession.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(selectSql))
            {
                ps.setObject(1, entityId);
                try (ResultSet rs = ps.executeQuery())
                {
                    if (rs.next())
                    {
                        Map<String, Object> rowMap = new LinkedHashMap<>();
                        ResultSetMetaData metaData = rs.getMetaData();
                        for (int i = 1; i <= metaData.getColumnCount(); i++)
                        {
                            String columnName = metaData.getColumnLabel(i);
                            Object value = rs.getObject(i);
                            rowMap.put(columnName, value);
                        }
                        return JSON.toJSONString(rowMap);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("撤销日志-查询当前数据异常: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 从方法参数中提取实体ID
     */
    private Object extractEntityId(ProceedingJoinPoint joinPoint, String keyField)
    {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0)
        {
            return null;
        }
        String getterName = "get" + Character.toUpperCase(keyField.charAt(0)) + keyField.substring(1);
        for (Object arg : args)
        {
            if (arg == null)
            {
                continue;
            }
            // 跳过框架类型参数
            Class<?> clazz = arg.getClass();
            if (clazz.getName().startsWith("jakarta.servlet")
                    || clazz.getName().startsWith("javax.servlet")
                    || clazz.getName().startsWith("org.springframework"))
            {
                continue;
            }
            // 如果是基本类型或String，直接返回
            if (clazz.isPrimitive() || clazz == String.class || Number.class.isAssignableFrom(clazz))
            {
                return arg;
            }
            // 尝试通过getter方法获取主键值
            try
            {
                Method getter = clazz.getMethod(getterName);
                return getter.invoke(arg);
            }
            catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException e)
            {
                // 继续查找
            }
        }
        return null;
    }

    /**
     * 获取实体类型（表名），驼峰转下划线
     */
    private String getEntityType(ProceedingJoinPoint joinPoint, EnableUndo enableUndo)
    {
        if (StringUtils.isNotEmpty(enableUndo.entityType()))
        {
            return enableUndo.entityType();
        }
        // 从方法签名推断：取第一个非框架参数的类名转下划线
        Object[] args = joinPoint.getArgs();
        if (args != null)
        {
            for (Object arg : args)
            {
                if (arg == null) continue;
                Class<?> clazz = arg.getClass();
                if (clazz.getName().startsWith("jakarta") || clazz.getName().startsWith("javax")
                        || clazz.getName().startsWith("org.springframework"))
                {
                    continue;
                }
                if (!clazz.isPrimitive() && clazz != String.class && !Number.class.isAssignableFrom(clazz))
                {
                    return camelToUnderscore(clazz.getSimpleName());
                }
            }
        }
        return "unknown";
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUserName()
    {
        try
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null)
            {
                return loginUser.getUsername();
            }
        }
        catch (Exception e)
        {
            // 未登录或获取失败
        }
        return "系统";
    }

    /**
     * 驼峰命名转下划线命名
     */
    private String camelToUnderscore(String camelCase)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++)
        {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c))
            {
                if (i > 0)
                {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            }
            else
            {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
