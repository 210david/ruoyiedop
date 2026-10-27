package com.ruoyi.dms.task;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.dms.domain.DmsDataRecord;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.mapper.DmsDataRecordMapper;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
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

    @Autowired
    private DmsEquipmentMapper dmsEquipmentMapper;

    @Autowired
    private DmsDataRecordMapper dmsDataRecordMapper;

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
     * 设备数据采集模拟
     * 定时为所有运行中的设备生成采集数据记录，包含温度、转速、功率等参数
     * invokeTarget: dmsTask.collectSimData
     */
    public void collectSimData()
    {
        log.info("===== 开始执行设备数据采集模拟任务 =====");
        try
        {
            // 查询所有正常状态的设备
            DmsEquipment queryParam = new DmsEquipment();
            queryParam.setStatus("0");
            List<DmsEquipment> equipmentList = dmsEquipmentMapper.selectEquipmentList(queryParam);
            int count = 0;
            for (DmsEquipment eq : equipmentList)
            {
                // 只为运行中或闲置的设备生成采集数据
                String status = eq.getEquipmentStatus();
                if ("2".equals(status) || "4".equals(status)) { continue; }

                DmsDataRecord record = new DmsDataRecord();
                record.setEquipmentId(eq.getEquipmentId());
                record.setEquipmentCode(eq.getEquipmentCode());
                record.setEquipmentName(eq.getEquipmentName());
                record.setCollectMode("1"); // 自动采集
                record.setCollectTime(new Date());

                // 运行状态映射：设备状态0=运行, 1=闲置
                record.setRunStatus("0".equals(status) ? "0" : "1");

                // 运行小时数（基于购置日期累计）
                if (eq.getPurchaseDate() != null)
                {
                    long hours = (System.currentTimeMillis() - eq.getPurchaseDate().getTime()) / 3600000;
                    record.setRunHours(new BigDecimal(hours % 10000));
                }
                else
                {
                    record.setRunHours(new BigDecimal(ThreadLocalRandom.current().nextInt(100, 8000)));
                }

                // 加工件数（随机）
                record.setProductCount(new BigDecimal(ThreadLocalRandom.current().nextInt(500, 50000)));

                // param1 = 温度 (℃)，运行设备35-75，闲置25-35
                if ("0".equals(status))
                {
                    record.setParam1Value(new BigDecimal(35 + ThreadLocalRandom.current().nextDouble(0, 40))
                        .setScale(1, BigDecimal.ROUND_HALF_UP));
                }
                else
                {
                    record.setParam1Value(new BigDecimal(25 + ThreadLocalRandom.current().nextDouble(0, 10))
                        .setScale(1, BigDecimal.ROUND_HALF_UP));
                }

                // param2 = 转速 (RPM)，运行设备1200-2000，闲置为0
                if ("0".equals(status))
                {
                    record.setParam2Value(new BigDecimal(1200 + ThreadLocalRandom.current().nextInt(0, 800)));
                }
                else
                {
                    record.setParam2Value(BigDecimal.ZERO);
                }

                // param3 = 功率 (kW)，运行设备45-85，闲置5-15
                if ("0".equals(status))
                {
                    record.setParam3Value(new BigDecimal(45 + ThreadLocalRandom.current().nextDouble(0, 40))
                        .setScale(1, BigDecimal.ROUND_HALF_UP));
                }
                else
                {
                    record.setParam3Value(new BigDecimal(5 + ThreadLocalRandom.current().nextDouble(0, 10))
                        .setScale(1, BigDecimal.ROUND_HALF_UP));
                }

                dmsDataRecordMapper.insertRecord(record);
                count++;
            }
            log.info("===== 设备数据采集模拟完成，共生成 {} 条采集记录 =====", count);
        }
        catch (Exception e)
        {
            log.error("设备数据采集模拟任务执行失败", e);
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
