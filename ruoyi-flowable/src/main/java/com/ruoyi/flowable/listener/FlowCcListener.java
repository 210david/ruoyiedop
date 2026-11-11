package com.ruoyi.flowable.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.domain.FlowCc;
import com.ruoyi.flowable.mapper.FlowCcMapper;
import com.ruoyi.flowable.service.FlowParticipantResolver;

/**
 * 流程抄送监听器（对标泛微「抄送 / 待阅」）
 *
 * 节点上配置 flowable:ccConfig 后，在指定时机自动把流程抄送给配置的人员：
 * <pre>
 * { "timing": "enter|complete|both", "participants": [ ... 同 participantConfig.participants ... ] }
 * </pre>
 * - timing=enter：任务创建（节点到达）时抄送；
 * - timing=complete：任务办理完成时抄送；
 * - timing=both：两者都抄送。
 *
 * 抄送记录写入 flow_cc，接收人在「待办流程-待阅」页签查看，可打开流程详情并标记已读。
 * 同一实例同一节点同一接收人只产生一条记录（幂等），抄送人本人不抄送给自己。
 *
 * @author ruoyi
 */
public class FlowCcListener implements FlowableEventListener
{
    private static final Logger log = LoggerFactory.getLogger(FlowCcListener.class);

    @Override
    public void onEvent(FlowableEvent event)
    {
        if (!(event instanceof FlowableEngineEntityEvent))
        {
            return;
        }
        boolean created = FlowableEngineEventType.TASK_CREATED.equals(event.getType());
        boolean completed = FlowableEngineEventType.TASK_COMPLETED.equals(event.getType());
        if (!created && !completed)
        {
            return;
        }
        try
        {
            TaskEntity task = (TaskEntity) ((FlowableEngineEntityEvent) event).getEntity();
            if (task == null || StringUtils.isEmpty(task.getProcessDefinitionId())
                    || StringUtils.isEmpty(task.getTaskDefinitionKey()))
            {
                return;
            }
            RepositoryService repositoryService = SpringUtils.getBean(RepositoryService.class);
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (bpmnModel == null)
            {
                return;
            }
            FlowElement element = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
            if (!(element instanceof UserTask))
            {
                return;
            }
            UserTask userTask = (UserTask) element;
            FlowParticipantResolver resolver = SpringUtils.getBean(FlowParticipantResolver.class);
            JSONObject cfg = resolver.readJson(userTask, "ccConfig");
            if (cfg == null)
            {
                return;
            }
            if (!matchTiming(cfg.getString("timing"), created))
            {
                return;
            }
            JSONArray participants = cfg.getJSONArray("participants");
            if (participants == null || participants.isEmpty())
            {
                return;
            }
            String senderId = completed ? task.getAssignee() : previousAssignee(task.getProcessInstanceId());
            FlowParticipantResolver.Ctx ctx = buildCtx(task, senderId);
            // 首个节点到达时尚无上一办理人，触发人回退为流程发起人
            senderId = ctx.getPrevAssignee();
            FlowParticipantResolver.Result result = resolver.resolveTargets(participants, ctx, task.getId());
            if (result.isEmpty())
            {
                if (!result.getUnresolved().isEmpty())
                {
                    log.warn("节点 {}（{}）抄送未能解析出人员：{}", task.getId(), task.getName(), result.getUnresolved());
                }
                return;
            }
            saveCc(task, bpmnModel, resolver, result.getUserIds(), senderId);
        }
        catch (Exception e)
        {
            // 抄送失败不影响流程流转
            log.error("流程抄送失败：" + e.getMessage(), e);
        }
    }

    /** 抄送时机匹配 */
    private boolean matchTiming(String timing, boolean created)
    {
        String t = StringUtils.isEmpty(timing) ? "enter" : timing.trim();
        if ("both".equals(t) || "all".equals(t))
        {
            return true;
        }
        return created ? "enter".equals(t) : "complete".equals(t);
    }

    /** 落库：同一实例同一节点同一接收人只保留一条 */
    private void saveCc(TaskEntity task, BpmnModel bpmnModel, FlowParticipantResolver resolver,
                        LinkedHashSet<String> userIds, String senderId)
    {
        FlowCcMapper mapper = SpringUtils.getBean(FlowCcMapper.class);
        HistoricProcessInstance instance = historicInstance(task.getProcessInstanceId());
        String processName = bpmnModel.getMainProcess() == null ? null : bpmnModel.getMainProcess().getName();
        String processKey = bpmnModel.getMainProcess() == null ? null : bpmnModel.getMainProcess().getId();
        String title = instance == null ? null : instance.getName();
        if (StringUtils.isEmpty(title) && instance != null)
        {
            title = processName;
        }
        Map<String, String> names = resolver.userNames(userIds);
        String senderName = null;
        if (!StringUtils.isEmpty(senderId))
        {
            Map<String, String> senderNames = resolver.userNames(new ArrayList<>(List.of(senderId)));
            senderName = senderNames.get(senderId);
        }
        int inserted = 0;
        for (String userId : userIds)
        {
            if (StringUtils.isEmpty(userId) || userId.equals(senderId) || !userId.matches("\\d+"))
            {
                continue;
            }
            Long receiverId = Long.valueOf(userId);
            if (mapper.countByInstanceTaskReceiver(task.getProcessInstanceId(), task.getId(), receiverId) > 0)
            {
                continue;
            }
            FlowCc cc = new FlowCc();
            cc.setInstanceId(task.getProcessInstanceId());
            cc.setProcessKey(processKey);
            cc.setProcessName(processName);
            cc.setBusinessKey(instance == null ? null : instance.getBusinessKey());
            cc.setTitle(title);
            cc.setTaskId(task.getId());
            cc.setTaskName(task.getName());
            cc.setSenderId(StringUtils.isEmpty(senderId) || !senderId.matches("\\d+") ? null : Long.valueOf(senderId));
            cc.setSenderName(senderName);
            cc.setReceiverId(receiverId);
            cc.setReceiverName(names.get(userId));
            cc.setReadFlag("0");
            mapper.insertCc(cc);
            inserted++;
        }
        if (inserted > 0)
        {
            log.info("流程抄送：任务 {}（{}）抄送给 {} 人", task.getId(), task.getName(), inserted);
        }
    }

    /** 组织解析上下文：发起人 + 抄送触发人 + 流程变量 */
    private FlowParticipantResolver.Ctx buildCtx(TaskEntity task, String senderId)
    {
        FlowParticipantResolver.Ctx ctx = new FlowParticipantResolver.Ctx();
        String processInstanceId = task.getProcessInstanceId();
        HistoricProcessInstance instance = historicInstance(processInstanceId);
        String initiator = task.getProcessInstanceId() == null ? null : startUserId(processInstanceId, instance);
        ctx.setInitiator(initiator);
        ctx.setPrevAssignee(StringUtils.isEmpty(senderId) ? initiator : senderId);
        // 「流程节点操作人」类抄送对象需要实例与定义才能反查
        ctx.setProcessInstanceId(processInstanceId);
        if (instance != null)
        {
            ctx.setProcessDefinitionId(instance.getProcessDefinitionId());
        }
        try
        {
            RuntimeService runtimeService = SpringUtils.getBean(RuntimeService.class);
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            ctx.setVariables(variables == null ? new HashMap<>() : variables);
        }
        catch (Exception e)
        {
            ctx.setVariables(new HashMap<>());
        }
        return ctx;
    }

    private String startUserId(String processInstanceId, HistoricProcessInstance instance)
    {
        if (instance != null && !StringUtils.isEmpty(instance.getStartUserId()))
        {
            return instance.getStartUserId();
        }
        try
        {
            RuntimeService runtimeService = SpringUtils.getBean(RuntimeService.class);
            ProcessInstance runtime = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if (runtime != null && !StringUtils.isEmpty(runtime.getStartUserId()))
            {
                return runtime.getStartUserId();
            }
        }
        catch (Exception e)
        {
            log.warn("查询流程发起人失败：{}", e.getMessage());
        }
        return null;
    }

    private HistoricProcessInstance historicInstance(String processInstanceId)
    {
        if (StringUtils.isEmpty(processInstanceId))
        {
            return null;
        }
        try
        {
            HistoryService historyService = SpringUtils.getBean(HistoryService.class);
            return historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
        }
        catch (Exception e)
        {
            log.warn("查询历史流程实例失败：{}", e.getMessage());
            return null;
        }
    }

    /** 上一活动（最近一个已完成任务）的办理人，作为「谁抄送给你」 */
    private String previousAssignee(String processInstanceId)
    {
        if (StringUtils.isEmpty(processInstanceId))
        {
            return null;
        }
        try
        {
            HistoryService historyService = SpringUtils.getBean(HistoryService.class);
            List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .finished()
                    .orderByHistoricTaskInstanceEndTime().desc()
                    .listPage(0, 1);
            if (!list.isEmpty())
            {
                return list.get(0).getAssignee();
            }
        }
        catch (Exception e)
        {
            log.warn("查询上一活动办理人失败：{}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean isFailOnException()
    {
        // 抄送异常不阻塞流程
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
