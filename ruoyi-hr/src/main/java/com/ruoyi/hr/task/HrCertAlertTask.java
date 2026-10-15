package com.ruoyi.hr.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.hr.service.impl.HrCertAlertService;

/**
 * 健康证到期提醒定时任务
 *
 * sys_job 调用目标：hrCertAlertTask.pushAlerts()
 * 内容：刷新健康证预警状态 → 临期每日提醒/过期提醒一次（按证书+状态去重，噪音受控）
 *
 * @author ruoyi
 */
@Component("hrCertAlertTask")
public class HrCertAlertTask
{
    @Autowired
    private HrCertAlertService alertService;

    public void pushAlerts()
    {
        alertService.pushExpiringAlerts();
    }
}
