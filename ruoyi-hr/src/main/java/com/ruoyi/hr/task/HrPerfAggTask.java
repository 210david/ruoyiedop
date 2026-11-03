package com.ruoyi.hr.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.hr.service.IHrSalesPerfService;

/**
 * 营销业绩月度归集定时任务（需求三）
 *
 * sys_job 配置：invoke_target = hrPerfAggTask.handle()，每月 1 日凌晨 2 点归集上月业绩
 * 归集口径：成交（订单创建时间归属周期）/ 回款（记录创建时间归属周期）/ 新客（建档且完成首笔成交）/ 审批通过的补录
 */
@Component("hrPerfAggTask")
public class HrPerfAggTask {
    @Autowired
    private IHrSalesPerfService perfService;

    public void handle() {
        // 默认归集上一个自然月
        String period = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        perfService.aggregate(period);
    }
}
