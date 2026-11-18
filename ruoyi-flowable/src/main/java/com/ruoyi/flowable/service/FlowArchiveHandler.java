package com.ruoyi.flowable.service;

import java.util.Set;

/**
 * 流程归档处理器 SPI
 *
 * 流程实例办结（PROCESS_COMPLETED）后，按 processKey 分发给已注册的处理器，
 * 由业务模块把流程表单数据回写到自己的业务表，实现「审批流 → 业务/考勤数据」联动。
 *
 * 使用方式：业务模块（如 ruoyi-hr）实现本接口并注册为 Spring Bean，
 * 归档分发监听器 FlowArchiveDispatchListener 会自动发现并按流程 Key 分发。
 *
 * @author ruoyi
 */
public interface FlowArchiveHandler
{
    /**
     * 本处理器支持的流程定义 Key 集合（如 travelApply / outingApply / makeupApply / leave_process）
     */
    Set<String> processKeys();

    /**
     * 流程办结归档回调
     *
     * @param processKey  流程定义 Key
     * @param instanceId  流程实例 ID（可用于查询流程变量/表单数据）
     * @param businessKey 业务主键（发起时写入，通常为业务表行 ID）
     */
    void archive(String processKey, String instanceId, String businessKey);

    /**
     * 流程终止/撤销回调（可选）
     *
     * @param processKey  流程定义 Key
     * @param instanceId  流程实例 ID
     * @param businessKey 业务主键
     */
    default void cancelled(String processKey, String instanceId, String businessKey)
    {
        // 默认不处理
    }

    /**
     * 流程复活/回退回调（可选）
     *
     * 已办结流程被「复活」重新进入审批时，业务台账需要从「已通过」回退到「审批中」，
     * 并回滚办结时产生的副作用（考勤打卡回写、假期余额扣减等）。
     *
     * @param processKey  流程定义 Key
     * @param instanceId  原流程实例 ID（复活前的实例）
     * @param businessKey 业务主键
     */
    default void rollback(String processKey, String instanceId, String businessKey)
    {
        // 默认不处理
    }

    /**
     * 流程复活后实例 ID 变更（可选）
     *
     * 复活会新建一个运行实例，业务台账里记录的 flow_instance_id 需要从旧实例改为新实例，
     * 否则后续办结/终止时按实例 ID 找不到台账。
     *
     * @param processKey      流程定义 Key
     * @param oldInstanceId   复活前的实例 ID
     * @param newInstanceId   复活后的新实例 ID
     */
    default void rebind(String processKey, String oldInstanceId, String newInstanceId)
    {
        // 默认不处理
    }
}
