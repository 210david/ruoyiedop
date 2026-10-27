package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsSqmPushLog;
import com.ruoyi.qms.domain.QmsSupplierEval;

/**
 * SQM→PMS 质量评分推送服务接口
 *
 * 对应 PRD FR-SQM-06：QMS 质量评分完成后推送至 PMS 供综合评估引用
 */
public interface ISqmScorePushService {

    /**
     * 推送供应商质量评分至 PMS
     *
     * @param eval QMS 供应商质量评价（已完成评分计算）
     */
    public void pushScoreToPms(QmsSupplierEval eval);

    /**
     * 重试失败的推送（由定时任务调用）
     */
    public void retryFailedPush();

    /** 查询推送日志列表 */
    public List<QmsSqmPushLog> selectPushLogList(QmsSqmPushLog log);

    /** 查询推送日志详情 */
    public QmsSqmPushLog selectPushLogById(Long logId);

    /** 删除推送日志 */
    public int deletePushLogByIds(Long[] logIds);
}
