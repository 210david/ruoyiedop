package com.ruoyi.qms.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.qms.service.IQmsInspTaskService;

/**
 * 质量管理定时任务
 *
 * 调用方式：在Quartz定时任务中配置
 * - Bean名称: qmsTask
 * - 方法1: autoGenerateIqcTasks（IQC自动生成）
 * - 方法2: checkOverdueTasks（逾期检查）
 *
 * @author ruoyi
 */
@Component("qmsTask")
public class QmsTask
{
    private static final Logger log = LoggerFactory.getLogger(QmsTask.class);

    @Autowired
    private IQmsInspTaskService qmsInspTaskService;

    /**
     * IQC检验任务自动生成
     * 每天凌晨1点扫描需IQC检验的物料属性，自动生成检验任务
     * invokeTarget: qmsTask.autoGenerateIqcTasks
     */
    public void autoGenerateIqcTasks()
    {
        log.info("===== 开始执行QMS IQC任务自动生成定时任务 =====");
        try
        {
            int count = qmsInspTaskService.autoGenerateIqcTasks();
            log.info("===== QMS IQC任务自动生成完成，共生成 {} 个任务 =====", count);
        }
        catch (Exception e)
        {
            log.error("QMS IQC任务自动生成执行失败", e);
        }
    }

    /**
     * 检验任务逾期标记
     * 每天凌晨2点检查待检状态超过3天的检验任务并标记逾期
     * invokeTarget: qmsTask.checkOverdueTasks
     */
    public void checkOverdueTasks()
    {
        log.info("===== 开始执行QMS检验任务逾期检查定时任务 =====");
        try
        {
            int count = qmsInspTaskService.checkOverdueTasks();
            log.info("===== QMS检验任务逾期检查完成，共标记 {} 个逾期任务 =====", count);
        }
        catch (Exception e)
        {
            log.error("QMS检验任务逾期检查执行失败", e);
        }
    }
}
