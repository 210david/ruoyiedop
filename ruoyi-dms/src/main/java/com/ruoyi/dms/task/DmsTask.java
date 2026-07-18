package com.ruoyi.dms.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.dms.service.IDmsInspectionTaskService;
import com.ruoyi.dms.service.IDmsPmPlanService;
import com.ruoyi.dms.service.IDmsWorkOrderService;

/**
 * DMS定时任务
 * 通过Quartz调度，invokeTarget: dmsTask.xxx
 */
@Component("dmsTask")
public class DmsTask
{
    private static final Logger log = LoggerFactory.getLogger(DmsTask.class);

    @Autowired
    private IDmsPmPlanService dmsPmPlanService;

    @Autowired
    private IDmsWorkOrderService dmsWorkOrderService;

    @Autowired
    private IDmsInspectionTaskService dmsInspectionTaskService;

    /**
     * PM计划自动生成工单
     * invokeTarget: dmsTask.pmAutoGenerate
     */
    public void pmAutoGenerate()
    {
        log.info("===== 开始执行PM计划自动生成工单定时任务 =====");
        try
        {
            int count = dmsPmPlanService.autoGeneratePmWorkOrders();
            log.info("===== PM计划自动生成工单完成，共生成 {} 个工单 =====", count);
        }
        catch (Exception e)
        {
            log.error("PM计划自动生成工单任务执行失败", e);
        }
    }

    /**
     * SLA超时检查
     * invokeTarget: dmsTask.slaTimeoutCheck
     */
    public void slaTimeoutCheck()
    {
        log.info("===== 开始执行SLA超时检查定时任务 =====");
        try
        {
            int count = dmsWorkOrderService.checkSlaTimeout();
            log.info("===== SLA超时检查完成，共发现 {} 个超时工单 =====", count);
        }
        catch (Exception e)
        {
            log.error("SLA超时检查任务执行失败", e);
        }
    }

    /**
     * 点检任务自动生成
     * 每天根据巡检路线周期自动生成点检任务
     * invokeTarget: dmsTask.inspectionAutoGenerate
     */
    public void inspectionAutoGenerate()
    {
        log.info("===== 开始执行点检任务自动生成定时任务 =====");
        try
        {
            int count = dmsInspectionTaskService.autoGenerateTasks();
            log.info("===== 点检任务自动生成完成，共生成 {} 个任务 =====", count);
        }
        catch (Exception e)
        {
            log.error("点检任务自动生成任务执行失败", e);
        }
    }

    /**
     * 点检任务逾期标记
     * 将过期未完成的点检任务标记为逾期
     * invokeTarget: dmsTask.inspectionOverdueCheck
     */
    public void inspectionOverdueCheck()
    {
        log.info("===== 开始执行点检任务逾期标记定时任务 =====");
        try
        {
            int count = dmsInspectionTaskService.markOverdueTasks();
            log.info("===== 点检任务逾期标记完成，共标记 {} 个任务为逾期 =====", count);
        }
        catch (Exception e)
        {
            log.error("点检任务逾期标记任务执行失败", e);
        }
    }
}
