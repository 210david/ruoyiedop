package com.ruoyi.food.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodBatchStatusLog;
import com.ruoyi.food.domain.FoodExpiryPolicy;
import com.ruoyi.food.mapper.FoodBatchMapper;
import com.ruoyi.food.service.IFoodExpiryPolicyService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 食品批次效期预警Service（定时任务专用）
 *
 * 临期预警闭环：扫描临期批次推送消息中心（按批次按天去重）→ 处置走批次操作留痕；
 * 过期自动冻结：按效期策略 freezeDays（0=过期即冻结）自动冻结过期批次
 *
 * @author ruoyi
 */
@Service
public class FoodBatchAlertService
{
    private static final Logger log = LoggerFactory.getLogger(FoodBatchAlertService.class);

    /** 消息推送去重标记（写入批次状态日志） */
    public static final String OP_ALERT_SENT = "ALERT_SENT";

    @Autowired
    private FoodBatchMapper batchMapper;

    @Autowired
    private IFoodExpiryPolicyService policyService;

    @Autowired
    private MessageHelper messageHelper;

    /**
     * 临期预警推送 + 过期自动冻结（quartz 入口）
     *
     * @return 处理批次数量
     */
    public int pushAlerts()
    {
        int affected = 0;
        // 先刷新全部批次效期状态
        policyService.refreshExpireStatus();
        affected += autoFreezeExpired();
        affected += pushNearExpiryAlerts();
        log.info("[批次效期预警] 处理完成，共处理 {} 个批次", affected);
        return affected;
    }

    /**
     * 过期自动冻结：效期策略 freezeDays（0=过期即冻结，N=过期N天后冻结）
     */
    private int autoFreezeExpired()
    {
        List<FoodBatch> expiredList = batchMapper.selectExpiredNotFrozenBatches();
        int count = 0;
        for (FoodBatch batch : expiredList)
        {
            try
            {
                if (batch.getExpireDate() == null)
                {
                    continue;
                }
                long daysOverdue = daysBetween(batch.getExpireDate(), LocalDate.now());
                java.util.Map<String, Object> materialInfo = batchMapper.selectMaterialInfo(batch.getMaterialId());
                FoodExpiryPolicy policy = policyService.matchPolicy(batch.getMaterialId(),
                        materialInfo == null || materialInfo.get("material_type") == null ? null
                                : String.valueOf(materialInfo.get("material_type")));
                int freezeDays = policy != null && policy.getFreezeDays() != null ? policy.getFreezeDays() : 0;
                if (daysOverdue < freezeDays)
                {
                    continue;
                }
                String reason = "过期自动冻结（效期策略：过期" + freezeDays + "天后冻结，已过期" + daysOverdue + "天）";
                batchMapper.updateBatchStatus(batch.getBatchId(), batch.getBatchStatus(), "2", reason);
                insertLog(batch, batch.getBatchStatus(), "2", "AUTO_FREEZE", reason);
                // 同步推送冻结通知
                messageHelper.sendMessage(
                    "批次已过期自动冻结：" + batch.getBatchCode(),
                    "物料：" + batch.getMaterialName() + "，到期日期：" + DateUtils.parseDateToStr("yyyy-MM-dd", batch.getExpireDate())
                            + "，批次数量：" + batch.getQty() + "，原因：" + reason,
                    "2", "3", "food", batch.getBatchId(),
                    "/wms/batch/ledger?batchCode=" + batch.getBatchCode(),
                    "food:batch:list", "2", "批次台账");
                count++;
            }
            catch (Exception e)
            {
                log.error("批次【{}】过期自动冻结失败", batch.getBatchCode(), e);
            }
        }
        if (count > 0)
        {
            log.info("[批次效期预警] 过期自动冻结 {} 个批次", count);
        }
        return count;
    }

    /**
     * 临期预警推送：按批次按天去重（同一批次同一天只推一次），处置闭环走批次操作留痕
     */
    private int pushNearExpiryAlerts()
    {
        List<FoodBatch> nearList = batchMapper.selectNearExpiryBatches();
        String today = DateUtils.getDate();
        int count = 0;
        for (FoodBatch batch : nearList)
        {
            try
            {
                if (hasAlertToday(batch.getBatchId(), today))
                {
                    continue;
                }
                Long daysRemain = batch.getExpireDate() == null ? null
                        : daysBetween(LocalDate.now(), toLocalDate(batch.getExpireDate()));
                messageHelper.sendMessage(
                    "批次临期预警：" + batch.getBatchCode(),
                    "物料：" + batch.getMaterialName() + "，到期日期：" + DateUtils.parseDateToStr("yyyy-MM-dd", batch.getExpireDate())
                            + "，剩余 " + daysRemain + " 天，批次数量：" + batch.getQty()
                            + "，请及时安排处置（出库消化/冻结/报废）",
                    "2", "2", "food", batch.getBatchId(),
                    "/wms/batch/ledger?batchCode=" + batch.getBatchCode(),
                    "food:batch:list", "1", "批次台账");
                insertLog(batch, null, null, OP_ALERT_SENT, "临期预警推送-" + today);
                count++;
            }
            catch (Exception e)
            {
                log.error("批次【{}】临期预警推送失败", batch.getBatchCode(), e);
            }
        }
        if (count > 0)
        {
            log.info("[批次效期预警] 推送临期预警 {} 个批次", count);
        }
        return count;
    }

    private boolean hasAlertToday(Long batchId, String today)
    {
        for (FoodBatchStatusLog logItem : batchMapper.selectStatusLogList(batchId))
        {
            if (OP_ALERT_SENT.equals(logItem.getOperateType())
                    && logItem.getReason() != null && logItem.getReason().endsWith(today))
            {
                return true;
            }
        }
        return false;
    }

    private void insertLog(FoodBatch batch, String fromStatus, String toStatus, String operateType, String reason)
    {
        FoodBatchStatusLog logItem = new FoodBatchStatusLog();
        logItem.setBatchId(batch.getBatchId());
        logItem.setBatchCode(batch.getBatchCode());
        logItem.setFromStatus(fromStatus);
        logItem.setToStatus(toStatus);
        logItem.setOperateType(operateType);
        logItem.setReason(reason);
        logItem.setOperateBy("system");
        batchMapper.insertStatusLog(logItem);
    }

    private long daysBetween(LocalDate start, LocalDate end)
    {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }

    private long daysBetween(Date date, LocalDate end)
    {
        return java.time.temporal.ChronoUnit.DAYS.between(toLocalDate(date), end);
    }

    private LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
