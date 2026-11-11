package com.ruoyi.flowable.listener;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.config.FlowReturnConstants;
import com.ruoyi.flowable.service.FlowParticipantResolver;

/**
 * 节点参与者派发监听器：任务创建时解析 flowable:participantConfig，
 * 把「固定对象 / 组织关系 / 活动关系 / 表单字段」配置的参与人转为真实办理人。
 *
 * 派发规则（handleMode 取人方式 × performMode 办理方式）：
 * <ul>
 * <li>抢占（claim）+ 指定（assign）：解析出 1 人 → setAssignee；解析出多人 → 进候选池（认领即办）</li>
 * <li>抢占 + 候选（candidate）：解析出的人进候选池，谁认领谁办</li>
 * <li>抢占 + 提交时指定（runtime）：优先用上一环节提交时指定的人（变量 nextAssignee_节点ID / nextAssignee），
 * 指定的人必须落在系统解析出的候选范围内；上一环节没指定则退回候选池</li>
 * <li>会签（and）/ 串签（seq）：多实例任务，不在本监听器处理 —— 集合由表达式
 * {@code ${participantCollection.collectionFor('节点ID', execution)}} 在引擎求值时现算，
 * 用户任务上的 flowable:assignee="${participantAssignee}" 再按实例逐个落人</li>
 * </ul>
 *
 * 兜底：解析不到人时按「节点配置的兜底办理人 → 流程管理员」转交，绝不留下无人可办的任务。
 *
 * 与 OperatorGroupTaskListener 的分工：
 * - 本监听器负责 participantConfig（设计器「节点操作人员」面板）；
 * - OperatorGroupTaskListener 负责历史遗留的 operatorGroups（老版「操作组」面板）；
 * - BPMN 内置 assignee / candidateUsers / candidateGroups 由引擎自行生效，两者都不重复处理。
 *
 * @author ruoyi
 */
public class ParticipantResolveListener implements FlowableEventListener
{
    private static final Logger log = LoggerFactory.getLogger(ParticipantResolveListener.class);

    @Override
    public void onEvent(FlowableEvent event)
    {
        if (!(event instanceof FlowableEngineEntityEvent) || !FlowableEngineEventType.TASK_CREATED.equals(event.getType()))
        {
            return;
        }
        try
        {
            TaskEntity task = (TaskEntity) ((FlowableEngineEntityEvent) event).getEntity();
            if (task == null || task.getProcessDefinitionId() == null || task.getTaskDefinitionKey() == null)
            {
                return;
            }
            // 已指定办理人的节点不处理（多实例实例任务由 ${participantAssignee} 赋值，也走这里跳过）
            if (task.getAssignee() != null && !task.getAssignee().isEmpty())
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
            // 发起人（申请）节点：只作为「退回发起人」的落点，办理人固定为流程发起人
            if (isInitiatorNode(userTask))
            {
                String initiator = startUserId(task.getProcessInstanceId());
                if (initiator != null && !initiator.isEmpty())
                {
                    SpringUtils.getBean(TaskService.class).setAssignee(task.getId(), initiator);
                    log.info("发起人节点派发：任务 {}（{}）办理人 {}", task.getId(), task.getName(), initiator);
                }
                else
                {
                    log.warn("发起人节点 {}（{}）取不到流程发起人，任务将无人可办", task.getId(), task.getName());
                }
                return;
            }
            // 会签/串签：集合由 collectionFor 表达式现算，正常情况每个实例创建时就已带 assignee
            if (userTask.getLoopCharacteristics() != null)
            {
                log.warn("任务 {}（{}）是多实例任务但未取到办理人，请检查集合表达式是否指向 participantCollection.collectionFor",
                        task.getId(), task.getName());
                return;
            }
            // 内置候选/候选组已生效的节点不重复处理
            if ((userTask.getCandidateUsers() != null && !userTask.getCandidateUsers().isEmpty())
                    || (userTask.getCandidateGroups() != null && !userTask.getCandidateGroups().isEmpty()))
            {
                return;
            }
            FlowParticipantResolver resolver = SpringUtils.getBean(FlowParticipantResolver.class);
            JSONObject cfg = resolver.readConfig(userTask);
            if (cfg == null)
            {
                return;
            }
            FlowParticipantResolver.Ctx ctx = buildCtx(task.getProcessInstanceId());
            FlowParticipantResolver.Result result = resolver.resolveWithFallback(userTask, ctx);
            if (result.isEmpty())
            {
                log.error("节点 {}（{}）解析不到办理人且无兜底人，任务将无人可办：{}",
                        task.getId(), task.getName(), result.getUnresolved());
                return;
            }
            String handleMode = cfg.getString("handleMode");
            List<String> userIds = new ArrayList<>(result.getUserIds());
            // 提交时指定：上一环节选了人且落在候选范围内，就用他的人
            if ("runtime".equals(handleMode))
            {
                List<String> designated = resolver.readDesignated(userTask.getId(), ctx, result.getUserIds());
                if (!designated.isEmpty())
                {
                    userIds = designated;
                }
            }
            TaskService taskService = SpringUtils.getBean(TaskService.class);
            boolean single = userIds.size() == 1 && !"candidate".equals(handleMode);
            if (single)
            {
                taskService.setAssignee(task.getId(), userIds.get(0));
                log.info("参与者派发（指定）：任务 {}（{}）办理人 {}{}", task.getId(), task.getName(), userIds.get(0),
                        result.isFailover() ? "（兜底）" : "");
            }
            else
            {
                for (String userId : userIds)
                {
                    taskService.addCandidateUser(task.getId(), userId);
                }
                log.info("参与者派发（候选）：任务 {}（{}）候选用户 {}{}", task.getId(), task.getName(), userIds,
                        result.isFailover() ? "（兜底）" : "");
            }
        }
        catch (Exception e)
        {
            // 派发失败不阻塞流程流转
            log.error("节点参与者派发失败：" + e.getMessage(), e);
        }
    }

    /** 节点是否为发起人（申请）节点（部署时注入，带 flowable:initiatorNode="true"） */
    private boolean isInitiatorNode(UserTask task)
    {
        Map<String, List<org.flowable.bpmn.model.ExtensionAttribute>> attrs = task.getAttributes();
        if (attrs == null)
        {
            return false;
        }
        List<org.flowable.bpmn.model.ExtensionAttribute> list = attrs.get(FlowReturnConstants.ATTR_INITIATOR_NODE);
        if (list == null || list.isEmpty())
        {
            return false;
        }
        String value = list.get(0).getValue();
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /** 流程发起人ID（发起人节点的办理人） */
    private String startUserId(String processInstanceId)
    {
        try
        {
            ProcessInstance instance = SpringUtils.getBean(RuntimeService.class)
                    .createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (instance != null && instance.getStartUserId() != null)
            {
                return instance.getStartUserId();
            }
            org.flowable.engine.history.HistoricProcessInstance hpi = SpringUtils.getBean(HistoryService.class)
                    .createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            return hpi == null ? null : hpi.getStartUserId();
        }
        catch (Exception e)
        {
            log.warn("查询流程发起人失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 构造解析上下文：发起人 + 上一活动办理人 + 流程变量
     *
     * 供 {@link FlowParticipantResolver#collectionFor} 复用（会签/串签求集合时需要同一套上下文）。
     *
     * @param processInstanceId 流程实例ID
     */
    public static FlowParticipantResolver.Ctx buildCtx(String processInstanceId)
    {
        FlowParticipantResolver.Ctx ctx = new FlowParticipantResolver.Ctx();
        ctx.setProcessInstanceId(processInstanceId);
        if (processInstanceId == null)
        {
            return ctx;
        }
        RuntimeService runtimeService = SpringUtils.getBean(RuntimeService.class);
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (instance != null && instance.getStartUserId() != null && !instance.getStartUserId().isEmpty())
        {
            ctx.setInitiator(instance.getStartUserId());
        }
        if (instance != null && instance.getProcessDefinitionId() != null)
        {
            // 「流程节点操作人」引用申请节点时需要它来判断节点类型
            ctx.setProcessDefinitionId(instance.getProcessDefinitionId());
        }
        try
        {
            Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
            ctx.setVariables(variables == null ? new HashMap<>() : variables);
        }
        catch (Exception e)
        {
            ctx.setVariables(new HashMap<>());
        }
        ctx.setPrevAssignee(lastAssignee(processInstanceId));
        return ctx;
    }

    /** 上一活动（最近一个已完成任务）的办理人 */
    private static String lastAssignee(String processInstanceId)
    {
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
        // 派发异常不阻塞流程
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
