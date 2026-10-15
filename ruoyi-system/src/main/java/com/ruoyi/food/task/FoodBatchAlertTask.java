package com.ruoyi.food.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.food.service.impl.FoodBatchAlertService;

/**
 * 食品批次效期预警定时任务
 *
 * sys_job 调用目标：foodBatchAlertTask.pushAlerts()
 * 内容：临期批次推送消息中心（按天去重）+ 过期批次按效期策略自动冻结
 *
 * @author ruoyi
 */
@Component("foodBatchAlertTask")
public class FoodBatchAlertTask
{
    @Autowired
    private FoodBatchAlertService alertService;

    public void pushAlerts()
    {
        alertService.pushAlerts();
    }
}
