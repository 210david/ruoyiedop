package com.ruoyi.flowable.listener;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.service.IFlowLeaveService;

/**
 * 流程结束监听器：流程实例结束时回调示例业务表回写状态
 *
 * 注意：监听器在引擎初始化阶段注册，因此不能在字段上注入引擎服务
 * （会产生循环依赖），统一在事件触发时通过 SpringUtils 惰性获取。
 *
 * @author ruoyi
 */
public class FlowProcessEndListener implements FlowableEventListener
{
    @Override
    public void onEvent(FlowableEvent event)
    {
        if (!(event instanceof FlowableEngineEntityEvent)
                || !FlowableEngineEventType.PROCESS_COMPLETED.equals(event.getType()))
        {
            return;
        }
        try
        {
            String instanceId = ((FlowableEngineEntityEvent) event).getProcessInstanceId();
            HistoryService historyService = SpringUtils.getBean(HistoryService.class);
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            if (hpi == null)
            {
                return;
            }
            SpringUtils.getBean(IFlowLeaveService.class).onProcessFinished(instanceId, hpi.getBusinessKey());
        }
        catch (Exception ignored)
        {
            // 示例业务回调异常不阻断引擎
        }
    }

    @Override
    public boolean isFailOnException()
    {
        // 回调失败不影响引擎主流程
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
        // 非事务生命周期监听
        return null;
    }
}
