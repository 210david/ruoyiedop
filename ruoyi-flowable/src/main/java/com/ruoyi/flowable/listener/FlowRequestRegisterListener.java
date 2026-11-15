package com.ruoyi.flowable.listener;

import java.util.Arrays;
import java.util.Collection;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.service.FlowRequestService;

/**
 * 流程请求登记监听器：流程实例创建时为实例分配短数字「流程编号」
 *
 * Flowable 的实例ID是 UUID，业务展示与检索都不方便；这里在实例创建事件里登记到
 * flow_request 表，由自增主键 request_id 作为对外流程编号（对标泛微 requestid）。
 * 登记失败不影响流程发起，列表展示时回退为实例ID。
 *
 * @author ruoyi
 */
public class FlowRequestRegisterListener implements FlowableEventListener
{
    private static final Logger log = LoggerFactory.getLogger(FlowRequestRegisterListener.class);

    @Override
    public void onEvent(FlowableEvent event)
    {
        if (!FlowableEngineEventType.PROCESS_CREATED.equals(event.getType()))
        {
            return;
        }
        String processInstanceId = null;
        String processDefinitionKey = null;
        String businessKey = null;
        if (event instanceof FlowableEngineEntityEvent)
        {
            Object entity = ((FlowableEngineEntityEvent) event).getEntity();
            if (entity instanceof ExecutionEntity)
            {
                ExecutionEntity execution = (ExecutionEntity) entity;
                processInstanceId = execution.getProcessInstanceId();
                if (StringUtils.isEmpty(processInstanceId))
                {
                    processInstanceId = execution.getId();
                }
                processDefinitionKey = execution.getProcessDefinitionKey();
                businessKey = execution.getBusinessKey();
            }
        }
        if (StringUtils.isEmpty(processInstanceId) && event instanceof FlowableEngineEvent)
        {
            processInstanceId = ((FlowableEngineEvent) event).getProcessInstanceId();
        }
        if (StringUtils.isEmpty(processInstanceId))
        {
            return;
        }
        try
        {
            SpringUtils.getBean(FlowRequestService.class)
                    .register(processInstanceId, processDefinitionKey, businessKey);
        }
        catch (Exception e)
        {
            log.error("流程请求登记失败（实例 {}）：{}", processInstanceId, e.getMessage(), e);
        }
    }

    @Override
    public boolean isFailOnException()
    {
        // 登记失败不能阻断流程发起
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

    @Override
    public Collection<? extends FlowableEventType> getTypes()
    {
        return Arrays.asList(FlowableEngineEventType.PROCESS_CREATED);
    }
}
