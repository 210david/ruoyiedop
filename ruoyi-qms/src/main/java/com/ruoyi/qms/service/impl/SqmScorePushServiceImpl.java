package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.qms.domain.QmsSqmPushLog;
import com.ruoyi.qms.domain.QmsSupplierEval;
import com.ruoyi.qms.mapper.QmsSqmPushLogMapper;
import com.ruoyi.qms.service.ISqmScorePushService;

/**
 * SQM→PMS 质量评分推送服务实现
 *
 * 实现策略：
 * 1. 创建推送日志记录（qms_sqm_push_log）
 * 2. 通过 Spring ApplicationEvent 发布质量评分事件
 *    — PMS 模块可通过 @EventListener 订阅此事件，将质量分写入其综合评估的"质量维度"
 *    — 本期 QMS 侧完成事件发布，PMS 侧监听器由 PMS 团队按需添加
 * 3. 推送失败时记录失败原因，支持定时任务重试（最多3次）
 *
 * 注：此实现不依赖 PMS 模块代码，符合"只完善QMS"的约束。
 *     PMS 侧只需添加一个 @EventListener 即可接收数据，无需改动已有功能。
 */
@Service
public class SqmScorePushServiceImpl implements ISqmScorePushService {

    private static final Logger log = LoggerFactory.getLogger(SqmScorePushServiceImpl.class);
    private static final int MAX_RETRY = 3;

    @Autowired
    private QmsSqmPushLogMapper pushLogMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pushScoreToPms(QmsSupplierEval eval) {
        if (eval == null || eval.getSupplierId() == null) {
            log.warn("[SQM-Push] 评价对象或供应商ID为空，跳过推送");
            return;
        }

        // 1. 创建推送日志
        QmsSqmPushLog pushLog = new QmsSqmPushLog();
        pushLog.setSupplierId(eval.getSupplierId());
        pushLog.setSupplierName(eval.getSupplierName());
        pushLog.setEvalId(eval.getEvalId());
        pushLog.setEvalPeriod(eval.getEvalPeriod());
        pushLog.setQualityScore(eval.getTotalScore());
        pushLog.setGrade(eval.getGrade());
        pushLog.setPassRate(eval.getPassRate());
        pushLog.setPpm(eval.getPpm());
        pushLog.setPushStatus("0");
        pushLog.setRetryCount(0);
        pushLog.setCreateBy(SecurityUtils.getUsername());
        pushLogMapper.insertPushLog(pushLog);

        // 2. 执行推送（发布事件）
        doPush(pushLog);
    }

    @Override
    public void retryFailedPush() {
        List<QmsSqmPushLog> pendingList = pushLogMapper.selectPendingPushList();
        if (pendingList.isEmpty()) {
            return;
        }
        log.info("[SQM-Push] 重试推送，待处理 {} 条", pendingList.size());
        for (QmsSqmPushLog pushLog : pendingList) {
            doPush(pushLog);
        }
    }

    /**
     * 实际推送逻辑：发布 Spring ApplicationEvent
     * PMS 侧可通过 @EventListener 监听 SqmScorePushEvent 接收数据
     */
    private void doPush(QmsSqmPushLog pushLog) {
        try {
            // 发布质量评分推送事件
            SqmScorePushEvent event = new SqmScorePushEvent(this, pushLog);
            eventPublisher.publishEvent(event);

            // 标记推送成功
            pushLog.setPushStatus("1");
            pushLog.setPushTime(new Date());
            pushLog.setUpdateBy(SecurityUtils.getUsername());
            pushLogMapper.updatePushLog(pushLog);

            log.info("[SQM-Push] 推送成功 supplier={}, period={}, score={}, grade={}",
                    pushLog.getSupplierName(), pushLog.getEvalPeriod(),
                    pushLog.getQualityScore(), pushLog.getGrade());

        } catch (Exception e) {
            // 记录失败
            int retryCount = (pushLog.getRetryCount() == null ? 0 : pushLog.getRetryCount()) + 1;
            pushLog.setRetryCount(retryCount);
            pushLog.setErrorMsg(e.getMessage() != null ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500)) : "未知错误");

            if (retryCount >= MAX_RETRY) {
                pushLog.setPushStatus("2"); // 标记为失败
                log.error("[SQM-Push] 推送失败(已达最大重试) supplier={}, error={}",
                        pushLog.getSupplierName(), e.getMessage());
            } else {
                log.warn("[SQM-Push] 推送失败(将重试) supplier={}, retry={}/{}, error={}",
                        pushLog.getSupplierName(), retryCount, MAX_RETRY, e.getMessage());
            }

            pushLog.setUpdateBy(SecurityUtils.getUsername());
            pushLogMapper.updatePushLog(pushLog);
        }
    }

    @Override
    public List<QmsSqmPushLog> selectPushLogList(QmsSqmPushLog log) {
        return pushLogMapper.selectPushLogList(log);
    }

    @Override
    public QmsSqmPushLog selectPushLogById(Long logId) {
        return pushLogMapper.selectPushLogById(logId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePushLogByIds(Long[] logIds) {
        return pushLogMapper.deletePushLogByIds(logIds);
    }
}
