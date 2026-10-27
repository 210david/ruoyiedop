package com.ruoyi.common.alert;

/**
 * 告警通道接口
 * 
 * @author ruoyi
 */
public interface AlertChannel
{
    /**
     * 发送告警
     * 
     * @param alarm 告警信息
     */
    void sendAlert(AlarmInfo alarm);
}
