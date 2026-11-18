package com.ruoyi.flowable.listener;

import java.util.Map;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.service.FlowArchiveHandler;

/**
 * 流程归档分发监听器
 *
 * 流程实例办结（PROCESS_COMPLETED）后，按流程定义 Key 分发给容器中所有
 * FlowArchiveHandler 实现（如 HR 考勤回写：出差单/外出单/补卡/请假单）。
 *
 * 单个处理器失败只记日志，不影响其他处理器与引擎主流程。
 *
 * @author ruoyi
 */
public class FlowArchiveDispatchListener implements FlowableEventListener
{
    private static final Logger log = LoggerFactory.getLogger(FlowArchiveDispatchListener.class);

    @Override
    public void onEvent(FlowableEvent event)
    {
        if (!(event instanceof FlowableEngineEntityEvent))
        {
            return;
        }
        boolean completed = FlowableEngineEventType.PROCESS_COMPLETED.equals(event.getType());
        boolean cancelled = FlowableEngineEventType.PROCESS_CANCELLED.equals(event.getType());
        if (!completed && !cancelled)
        {
            return;
        }
        String instanceId = ((FlowableEngineEntityEvent) event).getProcessInstanceId();
        log.info("归档分发：流程{} instanceId={}", completed ? "办结" : "终止", instanceId);
        try
        {
            HistoryService historyService = SpringUtils.getBean(HistoryService.class);
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            if (hpi == null)
            {
                log.warn("归档分发：未查到历史实例 instanceId={}", instanceId);
                return;
            }
            String processKey = hpi.getProcessDefinitionKey();
            String businessKey = hpi.getBusinessKey();
            Map<String, FlowArchiveHandler> handlers = SpringUtils.getBeansOfType(FlowArchiveHandler.class);
            log.info("归档分发：processKey={}, businessKey={}, 处理器数量={}", processKey, businessKey,
                    handlers == null ? 0 : handlers.size());
            if (handlers == null || handlers.isEmpty())
            {
                return;
            }
            for (FlowArchiveHandler handler : handlers.values())
            {
                if (handler.processKeys() == null || !handler.processKeys().contains(processKey))
                {
                    continue;
                }
                try
                {
                    if (completed)
                    {
                        handler.archive(processKey, instanceId, businessKey);
                    }
                    else
                    {
                        handler.cancelled(processKey, instanceId, businessKey);
                    }
                }
                catch (Exception e)
                {
                    // 单个业务归档失败不影响其他归档与流程本身
                    log.error("流程归档失败：processKey={}, instanceId={}, handler={}, error={}",
                            processKey, instanceId, handler.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }
        catch (Exception e)
        {
            log.error("流程归档分发异常：instanceId={}, error={}", instanceId, e.getMessage(), e);
        }
    }

    @Override
    public boolean isFailOnException()
    {
        // 归档失败不影响引擎主流程
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent()
    {
        return false;
    }

    @Override
    public String getOnTransaction()
    {
        return null;
    }
}
