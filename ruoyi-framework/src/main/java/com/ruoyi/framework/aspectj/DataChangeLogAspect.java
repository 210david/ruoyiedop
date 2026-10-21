package com.ruoyi.framework.aspectj;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ruoyi.common.annotation.DataChangeLog;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysDataChangeLog;
import com.ruoyi.system.mapper.SysDataChangeLogMapper;

/**
 * 数据变更日志AOP切面，拦截@DataChangeLog注解的方法，记录数据变更
 * 
 * @author ruoyi
 */
@Aspect
@Component
public class DataChangeLogAspect
{
    private static final Logger log = LoggerFactory.getLogger(DataChangeLogAspect.class);

    /** 线程安全的Map，用于在@Before时缓存实体旧值 */
    private static final ThreadLocal<Map<String, Object>> OLD_VALUE_CACHE = ThreadLocal.withInitial(ConcurrentHashMap::new);

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Pointcut("@annotation(dataChangeLog)")
    public void dataChangeLogPointcut(DataChangeLog dataChangeLog)
    {
    }

    /**
     * 方法执行前：查询并缓存实体的旧值
     */
    @Before(value = "dataChangeLogPointcut(dataChangeLog)", argNames = "joinPoint,dataChangeLog")
    public void doBefore(JoinPoint joinPoint, DataChangeLog dataChangeLog)
    {
        try
        {
            Map<String, Object> oldValueMap = OLD_VALUE_CACHE.get();
            oldValueMap.clear();

            Object entity = findEntityParam(joinPoint.getArgs(), dataChangeLog.key());
            if (entity == null)
            {
                return;
            }

            Object entityId = getFieldValue(entity, dataChangeLog.key());
            if (entityId == null || StringUtils.isEmpty(entityId.toString()))
            {
                // 新增操作，没有旧值
                return;
            }

            // 通过MyBatis查询数据库中的旧记录
            String tableName = camelToUnderscore(entity.getClass().getSimpleName());
            String keyColumn = camelToUnderscore(dataChangeLog.key());
            String selectSql = "SELECT * FROM " + tableName + " WHERE " + keyColumn + " = ?";

            try (SqlSession sqlSession = sqlSessionFactory.openSession())
            {
                // 使用原生JDBC方式通过SqlSession查询旧记录
                java.sql.Connection conn = sqlSession.getConnection();
                try (java.sql.PreparedStatement ps = conn.prepareStatement(selectSql))
                {
                    ps.setObject(1, entityId);
                    try (java.sql.ResultSet rs = ps.executeQuery())
                    {
                        if (rs.next())
                        {
                            java.sql.ResultSetMetaData metaData = rs.getMetaData();
                            for (int i = 1; i <= metaData.getColumnCount(); i++)
                            {
                                String columnName = metaData.getColumnLabel(i);
                                Object value = rs.getObject(i);
                                oldValueMap.put(columnName, value);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("数据变更日志-查询旧值异常: {}", e.getMessage());
        }
    }

    /**
     * 方法成功返回后：对比新旧值，写入变更日志
     */
    @AfterReturning(value = "dataChangeLogPointcut(dataChangeLog)", argNames = "joinPoint,dataChangeLog")
    public void doAfterReturning(JoinPoint joinPoint, DataChangeLog dataChangeLog)
    {
        try
        {
            Object entity = findEntityParam(joinPoint.getArgs(), dataChangeLog.key());
            if (entity == null)
            {
                return;
            }

            Map<String, Object> oldValueMap = OLD_VALUE_CACHE.get();
            Object entityId = getFieldValue(entity, dataChangeLog.key());
            String entityIdStr = entityId != null ? entityId.toString() : "";

            // 判断变更类型
            String changeType;
            if (oldValueMap.isEmpty())
            {
                // 旧值为空 -> 新增操作
                changeType = "INSERT";
            }
            else if (entityId == null || StringUtils.isEmpty(entityIdStr))
            {
                changeType = "DELETE";
            }
            else
            {
                changeType = "UPDATE";
            }

            List<SysDataChangeLog> logList = buildChangeLogs(entity, oldValueMap, dataChangeLog.value(),
                    entityIdStr, changeType);

            if (!logList.isEmpty())
            {
                // 在事务提交后写入日志，确保数据已持久化
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
                {
                    @Override
                    public void afterCommit()
                    {
                        try (SqlSession sqlSession = sqlSessionFactory.openSession(true))
                        {
                            SysDataChangeLogMapper mapper = sqlSession.getMapper(SysDataChangeLogMapper.class);
                            mapper.batchInsertChangeLog(logList);
                        }
                        catch (Exception e)
                        {
                            log.error("数据变更日志-写入日志异常: {}", e.getMessage());
                        }
                    }
                });
            }

            // 清理缓存
            oldValueMap.clear();
        }
        catch (Exception e)
        {
            log.error("数据变更日志-处理异常: {}", e.getMessage());
        }
        finally
        {
            OLD_VALUE_CACHE.remove();
        }
    }

    /**
     * 构建变更日志列表
     */
    private List<SysDataChangeLog> buildChangeLogs(Object entity, Map<String, Object> oldValueMap,
            String entityName, String entityId, String changeType)
    {
        List<SysDataChangeLog> logList = new ArrayList<>();
        String operateBy = getCurrentUserName();

        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            try
            {
                // 跳过serialVersionUID等特殊字段
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        || java.lang.reflect.Modifier.isFinal(field.getModifiers()))
                {
                    continue;
                }

                field.setAccessible(true);
                Object newValue = field.get(entity);
                String fieldName = field.getName();
                String columnName = camelToUnderscore(fieldName);

                String newValueStr = newValue != null ? newValue.toString() : null;
                String oldValueStr = null;

                if (oldValueMap.containsKey(columnName))
                {
                    Object oldVal = oldValueMap.get(columnName);
                    oldValueStr = oldVal != null ? oldVal.toString() : null;
                }

                // 对于INSERT：只记录有值的新字段
                // 对于UPDATE：只记录值发生变化的字段
                // 对于DELETE：记录被删除的旧值
                boolean shouldRecord = false;
                if ("INSERT".equals(changeType) && StringUtils.isNotEmpty(newValueStr))
                {
                    shouldRecord = true;
                }
                else if ("UPDATE".equals(changeType))
                {
                    // 新旧值不同才记录
                    if (!StringUtils.equals(oldValueStr, newValueStr))
                    {
                        shouldRecord = true;
                    }
                }
                else if ("DELETE".equals(changeType) && StringUtils.isNotEmpty(oldValueStr))
                {
                    shouldRecord = true;
                }

                if (shouldRecord)
                {
                    SysDataChangeLog changeLog = new SysDataChangeLog();
                    changeLog.setEntityName(entityName);
                    changeLog.setEntityId(entityId);
                    changeLog.setFieldName(fieldName);
                    changeLog.setOldValue(truncateValue(oldValueStr));
                    changeLog.setNewValue(truncateValue(newValueStr));
                    changeLog.setChangeType(changeType);
                    changeLog.setOperateBy(operateBy);
                    changeLog.setOperateTime(new Date());
                    logList.add(changeLog);
                }
            }
            catch (Exception e)
            {
                log.debug("构建变更日志-字段处理异常: {}", e.getMessage());
            }
        }
        return logList;
    }

    /**
     * 从方法参数中找到实体参数
     */
    private Object findEntityParam(Object[] args, String keyField)
    {
        if (args == null || args.length == 0)
        {
            return null;
        }
        // 通过反射查找包含主键getter方法的参数
        String getterName = "get" + Character.toUpperCase(keyField.charAt(0)) + keyField.substring(1);
        for (Object arg : args)
        {
            if (arg == null)
            {
                continue;
            }
            // 跳过框架类型参数
            Class<?> clazz = arg.getClass();
            if (clazz.getName().startsWith("jakarta.servlet") || clazz.getName().startsWith("javax.servlet")
                    || clazz.getName().startsWith("org.springframework"))
            {
                continue;
            }
            try
            {
                clazz.getMethod(getterName);
                return arg;
            }
            catch (NoSuchMethodException e)
            {
                // 不是实体参数，继续查找
            }
        }
        return null;
    }

    /**
     * 通过反射获取字段值
     */
    private Object getFieldValue(Object entity, String fieldName)
    {
        try
        {
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method getter = entity.getClass().getMethod(getterName);
            return getter.invoke(entity);
        }
        catch (Exception e)
        {
            return null;
        }
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

    /**
     * 截断过长的值
     */
    private String truncateValue(String value)
    {
        if (value == null)
        {
            return null;
        }
        return value.length() > 2000 ? value.substring(0, 2000) : value;
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
}
