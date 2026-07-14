package com.ruoyi.web.controller.monitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.druid.pool.DruidDataSource;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.framework.datasource.DynamicDataSource;

/**
 * 应用监控指标API
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/app")
public class AppMonitorController
{
    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取应用运行指标
     * 
     * @return 应用监控指标
     */
    @GetMapping("/metrics")
    public AjaxResult getMetrics()
    {
        Map<String, Object> metrics = new HashMap<>();

        // 请求统计（TODO: 后续可通过拦截器统计）
        metrics.put("totalRequests", 0);
        metrics.put("avgResponseTime", 0);
        metrics.put("errorRate", "0%");

        // 在线用户数 - 通过Redis中的登录token数量统计
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        metrics.put("onlineUsers", keys != null ? keys.size() : 0);

        // 数据库连接池状态
        metrics.put("dbPool", getDatabasePoolStatus());

        // JVM内存信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvmInfo = new HashMap<>();
        jvmInfo.put("total", runtime.totalMemory() / (1024 * 1024));
        jvmInfo.put("free", runtime.freeMemory() / (1024 * 1024));
        jvmInfo.put("max", runtime.maxMemory() / (1024 * 1024));
        jvmInfo.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        metrics.put("jvm", jvmInfo);

        return AjaxResult.success(metrics);
    }

    /**
     * 获取数据库连接池状态
     */
    private Map<String, Object> getDatabasePoolStatus()
    {
        Map<String, Object> poolStatus = new HashMap<>();
        try
        {
            // 获取Druid数据源状态
            DruidDataSource druidDS = getDruidDataSource();
            if (druidDS != null)
            {
                poolStatus.put("activeCount", druidDS.getActiveCount());
                poolStatus.put("maxActive", druidDS.getMaxActive());
                poolStatus.put("poolingCount", druidDS.getPoolingCount());
                poolStatus.put("connectCount", druidDS.getConnectCount());
                poolStatus.put("closeCount", druidDS.getCloseCount());
                poolStatus.put("waitThreadCount", druidDS.getWaitThreadCount());
                poolStatus.put("notEmptyWaitCount", druidDS.getNotEmptyWaitCount());
                poolStatus.put("status", "UP");
            }
            else
            {
                poolStatus.put("status", "UNKNOWN");
                poolStatus.put("detail", "非Druid数据源");
            }
        }
        catch (Exception e)
        {
            poolStatus.put("status", "ERROR");
            poolStatus.put("detail", e.getMessage());
        }
        return poolStatus;
    }

    /**
     * 获取Druid数据源实例
     */
    private DruidDataSource getDruidDataSource()
    {
        try
        {
            if (dataSource instanceof DynamicDataSource)
            {
                DynamicDataSource dynamicDS = (DynamicDataSource) dataSource;
                DataSource resolvedDS = null;
                try
                {
                    java.lang.reflect.Method m = dynamicDS.getClass().getMethod("determineTargetDataSource");
                    m.setAccessible(true);
                    resolvedDS = (DataSource) m.invoke(dynamicDS);
                }
                catch (Exception e)
                {
                    // ignore
                }
                if (resolvedDS instanceof DruidDataSource)
                {
                    return (DruidDataSource) resolvedDS;
                }
            }
            if (dataSource instanceof DruidDataSource)
            {
                return (DruidDataSource) dataSource;
            }
        }
        catch (Exception e)
        {
            // ignore
        }
        return null;
    }
}
