package com.ruoyi.flowable.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.flowable.bpmn.model.Activity;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExclusiveGateway;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.InclusiveGateway;
import org.flowable.bpmn.model.ParallelGateway;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.flowable.service.FlowDefinitionResolver;
import com.ruoyi.flowable.service.FlowParticipantResolver;

/**
 * 流程参与者 Controller
 *
 * 发起页提交前的「下一步审批人」预览：按流程定义走到第一个（批）用户任务节点，
 * 用 FlowParticipantResolver 把节点上配置的参与人解析为具体人员，供前端确认后发送。
 *
 * 注：该接口面向普通发起人，不加 @PreAuthorize（与流程图预览 /flowable/definition/diagram 一致）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/participant")
public class FlowParticipantController extends BaseController
{
    /** 顺序流解析的最大深度（防止环路导致栈溢出） */
    private static final int MAX_DEPTH = 8;

    @Autowired
    private RepositoryService repositoryService;

    /** 版本解析：预览必须与实际发起用同一个版本，否则会预览错人 */
    @Autowired
    private FlowDefinitionResolver flowDefinitionResolver;

    @Autowired
    private FlowParticipantResolver flowParticipantResolver;

    /** 节点配置取设计器最新保存的模型，操作者调整保存即生效 */
    @Autowired
    private com.ruoyi.flowable.service.FlowRuntimeModelService flowRuntimeModelService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    /** 条件表达式求值（与引擎一致：按出口条件决定实际会走的节点） */
    @Autowired
    private ProcessEngine processEngine;

    /**
     * 预览发起后第一环节的审批节点与办理人
     *
     * @param processKey 流程定义 Key（模型 Key）
     * @param body 可选，{ variables: { 表单字段: 值 } }，用于解析「表单字段」类型的参与人
     */
    @PostMapping("/next/{processKey}")
    public AjaxResult next(@PathVariable("processKey") String processKey,
            @RequestBody(required = false) Map<String, Object> body)
    {
        ProcessDefinition definition;
        try
        {
            definition = flowDefinitionResolver.requireLaunchable(processKey);
        }
        catch (ServiceException e)
        {
            return AjaxResult.error(e.getMessage());
        }
        if (definition == null)
        {
            return AjaxResult.error("流程尚未部署：「" + processKey + "」");
        }
        BpmnModel bpmnModel = flowRuntimeModelService.getByProcessKey(definition.getKey());
        if (bpmnModel == null || bpmnModel.getMainProcess() == null)
        {
            bpmnModel = repositoryService.getBpmnModel(definition.getId());
        }
        if (bpmnModel == null || bpmnModel.getMainProcess() == null)
        {
            return AjaxResult.error("流程模型解析失败：「" + processKey + "」");
        }
        FlowParticipantResolver.Ctx ctx = new FlowParticipantResolver.Ctx();
        String initiator = currentUserId();
        ctx.setInitiator(initiator);
        ctx.setPrevAssignee(initiator);
        // 「流程节点操作人」引用申请节点时，需要定义才能判断是否为开始节点
        ctx.setProcessDefinitionId(definition.getId());
        if (body != null && body.get("variables") instanceof Map)
        {
            ctx.setVariables(castMap(body.get("variables")));
        }
        List<Map<String, Object>> nodes = new ArrayList<>();
        // 条件网关按发起表单变量求值，只保留实际会走的出口（如请假天数 1 天只走「3天以内」）
        Map<String, Object> variables = ctx.getVariables() == null ? new HashMap<>() : ctx.getVariables();
        for (UserTask task : nextUserTasks(bpmnModel, variables))
        {
            nodes.add(describeNode(task, ctx));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("processKey", processKey);
        data.put("processName", definition.getName());
        data.put("version", definition.getVersion());
        data.put("nodes", nodes);
        return AjaxResult.success(data);
    }

    /** 节点描述：名称 + 处理方式 + 解析后的具体人员（+ 未解析的配置项） */
    private Map<String, Object> describeNode(UserTask task, FlowParticipantResolver.Ctx ctx)
    {
        FlowParticipantResolver.Result result = flowParticipantResolver.describe(task, ctx);
        JSONObject cfg = flowParticipantResolver.readConfig(task);
        String handleMode = cfg == null || cfg.getString("handleMode") == null ? "assign" : cfg.getString("handleMode");
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", task.getId());
        node.put("name", task.getName() == null ? task.getId() : task.getName());
        node.put("handleMode", handleMode);
        node.put("performMode", cfg == null || cfg.getString("performMode") == null ? "claim" : cfg.getString("performMode"));
        node.put("participantMode", cfg == null || cfg.getString("participantMode") == null ? "single" : cfg.getString("participantMode"));
        // 取人方式=提交时指定：需要上一环节在办理时选人
        node.put("needPick", "runtime".equals(handleMode));
        node.put("people", flowParticipantResolver.usersInfo(result.getUserIds()));
        node.put("unresolved", result.getUnresolved());
        return node;
    }

    /**
     * 办理时预览下一个（批）审批节点与候选范围
     *
     * 供办理页「提交时指定下一环节办理人」使用：取人方式=提交时指定的节点会在前端弹出选人框，
     * 选定的人写回变量 nextAssignee_节点ID，由 ParticipantResolveListener 在任务创建时采纳。
     *
     * @param taskId 当前待办任务ID
     */
    @PostMapping("/nextByTask/{taskId}")
    public AjaxResult nextByTask(@PathVariable("taskId") String taskId)
    {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            return AjaxResult.error("任务不存在或已办理");
        }
        BpmnModel bpmnModel = flowRuntimeModelService.getByDefinitionId(task.getProcessDefinitionId());
        if (bpmnModel == null || bpmnModel.getMainProcess() == null)
        {
            bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        }
        if (bpmnModel == null || bpmnModel.getMainProcess() == null)
        {
            return AjaxResult.error("流程模型解析失败");
        }
        FlowElement current = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        // 发起人（申请）节点只在部署版本的 BPMN 里，设计器模型取不到时回退部署模型
        if (!(current instanceof FlowNode))
        {
            BpmnModel deployed = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (deployed != null && deployed.getMainProcess() != null)
            {
                FlowElement deployedNode = deployed.getFlowElement(task.getTaskDefinitionKey());
                if (deployedNode instanceof FlowNode)
                {
                    bpmnModel = deployed;
                    current = deployedNode;
                }
            }
        }
        if (!(current instanceof FlowNode))
        {
            return AjaxResult.error("当前节点不是流程节点");
        }
        FlowParticipantResolver.Ctx ctx = new FlowParticipantResolver.Ctx();
        String processInstanceId = task.getProcessInstanceId();
        ctx.setProcessInstanceId(processInstanceId);
        ctx.setProcessDefinitionId(task.getProcessDefinitionId());
        Map<String, Object> variables = new HashMap<>();
        try
        {
            ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).singleResult();
            if (instance != null && instance.getStartUserId() != null)
            {
                ctx.setInitiator(instance.getStartUserId());
            }
            Map<String, Object> runtimeVariables = runtimeService.getVariables(processInstanceId);
            if (runtimeVariables != null)
            {
                variables = runtimeVariables;
            }
            ctx.setVariables(variables);
        }
        catch (Exception e)
        {
            ctx.setVariables(variables);
        }
        // 活动类关系的锚点：本环节办理人
        ctx.setPrevAssignee(task.getAssignee() != null ? task.getAssignee() : ctx.getInitiator());
        List<UserTask> nextTasks = new ArrayList<>();
        // 按出口条件求值，只收集实际会走的节点（避免分支网关把两条分支都列出来）
        collect(current, nextTasks, new HashSet<>(), 0, variables);
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (UserTask next : nextTasks)
        {
            nodes.add(describeNode(next, ctx));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("taskId", taskId);
        data.put("nodes", nodes);
        return AjaxResult.success(data);
    }

    /** 从开始事件沿顺序流找到第一层的用户任务节点（遇到网关继续展开，遇到结束事件跳过） */
    private List<UserTask> nextUserTasks(BpmnModel bpmnModel, Map<String, Object> variables)
    {
        List<UserTask> tasks = new ArrayList<>();
        StartEvent start = null;
        for (FlowElement element : bpmnModel.getMainProcess().getFlowElements())
        {
            if (element instanceof StartEvent)
            {
                start = (StartEvent) element;
                break;
            }
        }
        if (start == null)
        {
            return tasks;
        }
        collect(start, tasks, new HashSet<>(), 0, variables);
        return tasks;
    }

    private void collect(FlowElement node, List<UserTask> tasks, Set<String> visited, int depth,
            Map<String, Object> variables)
    {
        if (!(node instanceof FlowNode) || depth > MAX_DEPTH || !visited.add(node.getId()))
        {
            return;
        }
        for (SequenceFlow flow : selectOutgoingFlows((FlowNode) node, variables))
        {
            FlowElement target = flow.getTargetFlowElement();
            if (target == null)
            {
                continue;
            }
            if (target instanceof UserTask)
            {
                boolean exists = false;
                for (UserTask t : tasks)
                {
                    if (t.getId().equals(target.getId()))
                    {
                        exists = true;
                        break;
                    }
                }
                if (!exists)
                {
                    tasks.add((UserTask) target);
                }
            }
            else if (target instanceof EndEvent)
            {
                // 直接结束的分支不产生审批节点
                continue;
            }
            else
            {
                collect(target, tasks, visited, depth + 1, variables);
            }
        }
    }

    /**
     * 选出当前节点实际会走的出口，规则与 Flowable 引擎一致：
     * <ul>
     * <li>排他网关：按顺序取第一条条件为真的出口（无条件视为真），都不满足时走默认出口</li>
     * <li>包容网关：所有条件为真的出口，都不满足时走默认出口</li>
     * <li>并行网关：全部出口</li>
     * <li>普通节点：只走条件为真的出口，都不满足时走默认出口</li>
     * </ul>
     * 这样「下一环节」预览不会把多个分支的节点同时列出来。
     */
    private List<SequenceFlow> selectOutgoingFlows(FlowNode node, Map<String, Object> variables)
    {
        List<SequenceFlow> outgoing = node.getOutgoingFlows();
        if (outgoing == null || outgoing.isEmpty())
        {
            return Collections.emptyList();
        }
        if (node instanceof ParallelGateway)
        {
            return outgoing;
        }
        String defaultFlowId = null;
        if (node instanceof ExclusiveGateway)
        {
            defaultFlowId = ((ExclusiveGateway) node).getDefaultFlow();
        }
        else if (node instanceof InclusiveGateway)
        {
            defaultFlowId = ((InclusiveGateway) node).getDefaultFlow();
        }
        else if (node instanceof Activity)
        {
            defaultFlowId = ((Activity) node).getDefaultFlow();
        }
        List<SequenceFlow> picked = new ArrayList<>();
        SequenceFlow defaultFlow = null;
        for (SequenceFlow flow : outgoing)
        {
            if (defaultFlowId != null && defaultFlowId.equals(flow.getId()))
            {
                defaultFlow = flow;
                continue;
            }
            if (conditionTrue(flow, variables))
            {
                picked.add(flow);
                // 排他网关只会走第一条满足条件的出口
                if (node instanceof ExclusiveGateway)
                {
                    return picked;
                }
            }
        }
        if (picked.isEmpty() && defaultFlow != null)
        {
            picked.add(defaultFlow);
        }
        return picked;
    }

    /** 顺序流条件求值：无条件视为通过；表达式异常按不通过处理，避免预览把错误分支也列出来 */
    private boolean conditionTrue(SequenceFlow flow, Map<String, Object> variables)
    {
        String expressionText = flow.getConditionExpression();
        if (expressionText == null || expressionText.trim().isEmpty())
        {
            return true;
        }
        try
        {
            Expression expression = ((org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl) processEngine
                    .getProcessEngineConfiguration()).getExpressionManager().createExpression(expressionText);
            Object value = expression.getValue(new MapVariableContainer(variables));
            return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /** 基于流程变量 Map 的表达式变量容器（供条件表达式读取 ${字段}） */
    private static final class MapVariableContainer implements VariableContainer
    {
        private final Map<String, Object> variables;

        private MapVariableContainer(Map<String, Object> variables)
        {
            this.variables = variables == null ? new HashMap<>() : variables;
        }

        @Override
        public boolean hasVariable(String variableName)
        {
            return variables.containsKey(variableName);
        }

        @Override
        public Object getVariable(String variableName)
        {
            return variables.get(variableName);
        }

        @Override
        public void setVariable(String variableName, Object value)
        {
            variables.put(variableName, value);
        }

        @Override
        public void setTransientVariable(String variableName, Object value)
        {
            // 预览只读，无需处理临时变量
        }

        @Override
        public String getTenantId()
        {
            return null;
        }

        @Override
        public Set<String> getVariableNames()
        {
            return variables.keySet();
        }
    }

    private String currentUserId()
    {
        try
        {
            Long userId = SecurityUtils.getUserId();
            return userId == null ? null : String.valueOf(userId);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value)
    {
        return (Map<String, Object>) value;
    }
}
