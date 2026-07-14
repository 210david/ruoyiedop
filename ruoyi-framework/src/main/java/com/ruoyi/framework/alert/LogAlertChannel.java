package com.ruoyi.framework.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.alert.AlarmInfo;
import com.ruoyi.common.alert.AlertChannel;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;

/**
 * 默认告警通道实现：记录到日志和数据库
 * 
 * @author ruoyi
 */
@Component
public class LogAlertChannel implements AlertChannel
{
    private static final Logger log = LoggerFactory.getLogger(LogAlertChannel.class);

    @Override
    public void sendAlert(AlarmInfo alarm)
    {
        // 记录到日志文件
        log.warn("系统告警 - 类型: {}, 级别: {}, 标题: {}, 内容: {}", 
                alarm.getType(), alarm.getLevel(), alarm.getTitle(), alarm.getContent());

        // 异步写入数据库告警日志表
        try
        {
            AsyncManager.me().execute(AsyncFactory.recordAlarm(alarm));
        }
        catch (Exception e)
        {
            log.error("告警信息写入数据库失败: {}", e.getMessage());
        }
    }
}
