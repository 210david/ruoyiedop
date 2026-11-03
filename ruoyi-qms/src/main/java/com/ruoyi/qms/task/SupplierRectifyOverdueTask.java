package com.ruoyi.qms.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.service.IQmsSupplierRectifyService;

/**
 * 客诉供应商整改单逾期升级定时任务（KR-03）
 *
 * 业务规则：供应商整改单逾期未回复，消息中心自动升级提醒采购负责人
 * 定时任务配置（sys_job）：invoke_target = supplierRectifyOverdueTask.handle()，建议每日 8 点执行
 */
@Component("supplierRectifyOverdueTask")
public class SupplierRectifyOverdueTask {
    @Autowired
    private IQmsSupplierRectifyService rectifyService;

    public void handle() {
        int rows = rectifyService.handleOverdue();
        if (rows > 0) {
            // 升级数量仅记录日志，提醒推送由消息中心补充
            System.out.println("[SupplierRectifyOverdueTask] 供应商整改单逾期升级 " + rows + " 条" + (StringUtils.isNotEmpty("")));
        }
    }
}
