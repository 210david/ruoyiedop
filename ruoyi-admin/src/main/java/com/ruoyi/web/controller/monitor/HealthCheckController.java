package com.ruoyi.web.controller.monitor;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 健康检查接口
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/health")
public class HealthCheckController
{
    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 系统健康状态检查
     * 
     * @return 健康检查结果
     */
    @GetMapping()
    public AjaxResult getHealth()
    {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> components = new HashMap<>();

        // 检查数据库连接
        components.put("db", checkDatabase());

        // 检查Redis连接
        components.put("redis", checkRedis());

        // 检查磁盘使用率
        components.put("disk", checkDisk());

        // 检查内存使用率
        components.put("memory", checkMemory());

        // 判断整体状态
        boolean allUp = components.values().stream()
                .allMatch(comp -> "UP".equals(((Map<String, Object>) comp).get("status")));
        result.put("status", allUp ? "UP" : "DOWN");
        result.put("components", components);

        return AjaxResult.success(result);
    }

    /**
     * 检查数据库连接
     */
    private Map<String, Object> checkDatabase()
    {
        Map<String, Object> dbStatus = new HashMap<>();
        try (Connection conn = dataSource.getConnection())
        {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbVersion = metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion();
            // 执行 SELECT 1 验证连接有效性
            conn.prepareStatement("SELECT 1").execute();
            dbStatus.put("status", "UP");
            dbStatus.put("detail", dbVersion);
        }
        catch (Exception e)
        {
            dbStatus.put("status", "DOWN");
            dbStatus.put("detail", "数据库连接失败: " + e.getMessage());
        }
        return dbStatus;
    }

    /**
     * 检查Redis连接
     */
    private Map<String, Object> checkRedis()
    {
        Map<String, Object> redisStatus = new HashMap<>();
        try
        {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            redisStatus.put("status", "UP");
            redisStatus.put("detail", "Redis " + pong);
        }
        catch (Exception e)
        {
            redisStatus.put("status", "DOWN");
            redisStatus.put("detail", "Redis连接失败: " + e.getMessage());
        }
        return redisStatus;
    }

    /**
     * 检查磁盘使用率
     */
    private Map<String, Object> checkDisk()
    {
        Map<String, Object> diskStatus = new HashMap<>();
        try
        {
            File[] roots = File.listRoots();
            StringBuilder detail = new StringBuilder();
            long totalFree = 0;
            long totalSpace = 0;
            for (File root : roots)
            {
                totalFree += root.getFreeSpace();
                totalSpace += root.getTotalSpace();
            }
            if (totalSpace > 0)
            {
                long freeGB = totalFree / (1024 * 1024 * 1024);
                diskStatus.put("status", "UP");
                diskStatus.put("detail", "可用空间 " + freeGB + "GB");
            }
            else
            {
                diskStatus.put("status", "UP");
                diskStatus.put("detail", "可用空间 未知");
            }
        }
        catch (Exception e)
        {
            diskStatus.put("status", "DOWN");
            diskStatus.put("detail", "磁盘检查失败: " + e.getMessage());
        }
        return diskStatus;
    }

    /**
     * 检查内存使用率
     */
    private Map<String, Object> checkMemory()
    {
        Map<String, Object> memStatus = new HashMap<>();
        try
        {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double usageRate = (double) usedMemory / totalMemory * 100;

            memStatus.put("status", "UP");
            memStatus.put("detail", "使用率 " + String.format("%.0f", usageRate) + "%");
        }
        catch (Exception e)
        {
            memStatus.put("status", "DOWN");
            memStatus.put("detail", "内存检查失败: " + e.getMessage());
        }
        return memStatus;
    }
}
