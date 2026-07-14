package com.ruoyi.framework.aspectj;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.common.alert.AlarmInfo;
import com.ruoyi.framework.alert.AlertManager;

/**
 * 定时告警检查切面
 * 
 * @author ruoyi
 */
@Component
public class AlertAspect
{
    private static final Logger log = LoggerFactory.getLogger(AlertAspect.class);

    /** 内存使用率告警阈值 */
    private static final double MEMORY_USAGE_THRESHOLD = 0.85;

    /** 磁盘使用率告警阈值 */
    private static final double DISK_USAGE_THRESHOLD = 0.90;

    @Autowired
    private AlertManager alertManager;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 每30秒检查一次告警条件
     */
    @Scheduled(fixedRate = 30000)
    public void checkAlarm()
    {
        // 数据库连接检查
        checkDatabaseConnection();

        // Redis连接检查
        checkRedisConnection();

        // 内存使用率检查
        checkMemoryUsage();

        // 磁盘使用率检查
        checkDiskUsage();
    }

    /**
     * 检查数据库连接
     */
    private void checkDatabaseConnection()
    {
        try (Connection conn = dataSource.getConnection())
        {
            DatabaseMetaData metaData = conn.getMetaData();
            // 执行 SELECT 1 验证连接有效性
            conn.prepareStatement("SELECT 1").execute();
            log.debug("数据库连接检查通过: {}", metaData.getDatabaseProductVersion());
        }
        catch (Exception e)
        {
            log.error("数据库连接检查失败", e);
            AlarmInfo alarm = new AlarmInfo();
            alarm.setType("DATABASE");
            alarm.setLevel("ERROR");
            alarm.setTitle("数据库连接异常");
            alarm.setContent("数据库连接检测失败: " + e.getMessage());
            alertManager.sendAlert(alarm);
        }
    }

    /**
     * 检查Redis连接
     */
    private void checkRedisConnection()
    {
        try
        {
            String result = redisTemplate.getConnectionFactory().getConnection().ping();
            if (!"PONG".equalsIgnoreCase(result))
            {
                throw new RuntimeException("Redis PING返回异常: " + result);
            }
            log.debug("Redis连接检查通过");
        }
        catch (Exception e)
        {
            log.error("Redis连接检查失败", e);
            AlarmInfo alarm = new AlarmInfo();
            alarm.setType("REDIS");
            alarm.setLevel("ERROR");
            alarm.setTitle("Redis连接异常");
            alarm.setContent("Redis连接检测失败: " + e.getMessage());
            alertManager.sendAlert(alarm);
        }
    }

    /**
     * 检查内存使用率
     */
    private void checkMemoryUsage()
    {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double usageRate = (double) usedMemory / totalMemory;

        if (usageRate > MEMORY_USAGE_THRESHOLD)
        {
            log.warn("内存使用率超过阈值: {}%", String.format("%.2f", usageRate * 100));
            AlarmInfo alarm = new AlarmInfo();
            alarm.setType("MEMORY");
            alarm.setLevel("WARN");
            alarm.setTitle("内存使用率过高");
            alarm.setContent(String.format("内存使用率 %.2f%%，超过阈值 85%%，总内存: %d MB, 已用: %d MB",
                    usageRate * 100,
                    totalMemory / (1024 * 1024),
                    usedMemory / (1024 * 1024)));
            alertManager.sendAlert(alarm);
        }
    }

    /**
     * 检查磁盘使用率
     */
    private void checkDiskUsage()
    {
        try
        {
            File[] roots = File.listRoots();
            for (File root : roots)
            {
                long totalSpace = root.getTotalSpace();
                long freeSpace = root.getFreeSpace();
                if (totalSpace <= 0)
                {
                    continue;
                }
                long usedSpace = totalSpace - freeSpace;
                double usageRate = (double) usedSpace / totalSpace;

                if (usageRate > DISK_USAGE_THRESHOLD)
                {
                    log.warn("磁盘使用率超过阈值: {} -> {}%", root.getPath(), String.format("%.2f", usageRate * 100));
                    AlarmInfo alarm = new AlarmInfo();
                    alarm.setType("DISK");
                    alarm.setLevel("WARN");
                    alarm.setTitle("磁盘使用率过高");
                    alarm.setContent(String.format("磁盘[%s]使用率 %.2f%%，超过阈值 90%%，总空间: %d GB, 可用: %d GB",
                            root.getPath(),
                            usageRate * 100,
                            totalSpace / (1024 * 1024 * 1024),
                            freeSpace / (1024 * 1024 * 1024)));
                    alertManager.sendAlert(alarm);
                }
            }
        }
        catch (Exception e)
        {
            log.error("磁盘使用率检查失败", e);
        }
    }
}
