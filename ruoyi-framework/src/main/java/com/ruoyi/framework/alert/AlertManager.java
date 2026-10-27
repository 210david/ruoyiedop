package com.ruoyi.framework.alert;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.alert.AlarmInfo;
import com.ruoyi.common.alert.AlertChannel;

/**
 * 告警管理器：管理多个告警通道，支持后续扩展邮件、钉钉、企业微信通道
 * 
 * @author ruoyi
 */
@Component
public class AlertManager
{
    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);

    /** 告警通道列表 */
    private final List<AlertChannel> channels = new ArrayList<>();

    /** 默认日志告警通道 */
    @Autowired
    private LogAlertChannel logAlertChannel;

    /**
     * 注册告警通道
     * 
     * @param channel 告警通道
     */
    public void registerChannel(AlertChannel channel)
    {
        channels.add(channel);
        log.info("注册告警通道: {}", channel.getClass().getSimpleName());
    }

    /**
     * 发送告警到所有已注册的通道
     * 
     * @param alarm 告警信息
     */
    public void sendAlert(AlarmInfo alarm)
    {
        // 始终通过默认日志通道发送
        try
        {
            logAlertChannel.sendAlert(alarm);
        }
        catch (Exception e)
        {
            log.error("默认告警通道发送失败: {}", e.getMessage());
        }

        // 通过所有已注册的扩展通道发送
        for (AlertChannel channel : channels)
        {
            try
            {
                channel.sendAlert(alarm);
            }
            catch (Exception e)
            {
                log.error("告警通道[{}]发送失败: {}", channel.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
