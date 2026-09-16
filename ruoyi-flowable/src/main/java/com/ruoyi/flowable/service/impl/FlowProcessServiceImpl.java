package com.ruoyi.flowable.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.engine.ProcessEngine;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.api.TaskQuery;
import org.flowable.engine.task.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowInstance;
import com.ruoyi.flowable.domain.FlowTask;
import com.ruoyi.flowable.domain.dto.FlowCompleteDTO;
import com.ruoyi.flowable.domain.dto.FlowStartDTO;
import com.ruoyi.flowable.service.IFlowProcessService;

/**
 * 流程实例/任务 Service 实现
 *
 * 身份体系：办理人直接使用 RuoYi sys_user.user_id（字符串形式存入 assignee），
 * 不使用 Flowable IDM 模块。
 *
 * @author ruoyi
 */
@Service
public class FlowProcessServiceImpl implements IFlowProcessService
{
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private com.ruoyi.flowable.mapper.FlowLeaveMapper flowLeaveMapper;

    /** 审批意见类型标识 */

    @Override
    @Transactional
    public String startProcess(FlowStartDTO dto)
    {
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(dto.getProcessDefinitionKey())
                .latestVersion().singleResult();
        if (definition == null)
        {
            throw new ServiceException("流程定义不存在或未部署：" + dto.getProcessDefinitionKey());
        }
        if (definition.isSuspended())
        {
            throw new ServiceException("流程定义已挂起，无法发起");
        }
        Long userId = SecurityUtils.getUserId();
        // 记录发起人身份
        org.flowable.common.engine.impl.identity.Authentication
                .setAuthenticatedUserId(String.valueOf(userId));
        try
        {
            Map<String, Object> variables = dto.getVariables();
            ProcessInstance instance = runtimeService.startProcessInstanceById(
                    definition.getId(), dto.getBusinessKey(), variables);
            return instance.getId();
        }
        finally
        {
            org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(null);
        }
    }

    @Override
    public List<FlowInstance> selectMyProcessList(FlowInstance query)
    {
        Long userId = SecurityUtils.getUserId();
        HistoricProcessInstanceQuery hisQuery = historyService.createHistoricProcessInstanceQuery()
                .startedBy(String.valueOf(userId))
                .orderByProcessInstanceStartTime().desc();
        if (StringUtils.isNotEmpty(query.getProcessDefinitionName()))
        {
            hisQuery.processDefinitionNameLike("%" + query.getProcessDefinitionName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionKey()))
        {
            hisQuery.processDefinitionKey(query.getProcessDefinitionKey());
        }
        if ("running".equals(query.getStatus()))
        {
            hisQuery.unfinished();
        }
        else if ("completed".equals(query.getStatus()))
        {
            hisQuery.finished();
        }
        List<HistoricProcessInstance> instances = hisQuery.list();
        List<FlowInstance> list = new ArrayList<>();
        for (HistoricProcessInstance hpi : instances)
        {
            list.add(convertInstance(hpi));
        }
        return list;
    }

    @Override
    public FlowInstance selectInstanceDetail(String instanceId)
    {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (hpi == null)
        {
            throw new ServiceException("流程实例不存在");
        }
        FlowInstance fi = convertInstance(hpi);
        fi.setVariables(hpi.getProcessVariables());
        return fi;
    }

    @Override
    @Transactional
    public int cancelProcess(String instanceId, String reason)
    {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (instance == null)
        {
            throw new ServiceException("流程实例不存在或已结束");
        }
        Long userId = SecurityUtils.getUserId();
        if (!String.valueOf(userId).equals(instance.getStartUserId()))
        {
            throw new ServiceException("只有发起人才能撤销该流程");
        }
        runtimeService.deleteProcessInstance(instanceId,
                StringUtils.isNotEmpty(reason) ? reason : "发起人撤销");
        // 同步回写示例业务表状态为已撤销
        try
        {
            com.ruoyi.flowable.domain.FlowLeave query = new com.ruoyi.flowable.domain.FlowLeave();
            query.setInstanceId(instanceId);
            java.util.List<com.ruoyi.flowable.domain.FlowLeave> leaves =
                    flowLeaveMapper.selectFlowLeaveList(query);
            for (com.ruoyi.flowable.domain.FlowLeave leave : leaves)
            {
                if ("1".equals(leave.getStatus()))
                {
                    com.ruoyi.flowable.domain.FlowLeave update = new com.ruoyi.flowable.domain.FlowLeave();
                    update.setLeaveId(leave.getLeaveId());
                    update.setStatus("4");
                    update.setAuditOpinion(StringUtils.isNotEmpty(reason) ? reason : "发起人撤销");
                    update.setUpdateBy(SecurityUtils.getUsername());
                    flowLeaveMapper.updateFlowLeave(update);
                }
            }
        }
        catch (Exception ignored)
        {
        }
        return 1;
    }

    @Override
    public List<FlowTask> selectTodoList(FlowTask query)
    {
        Long userId = SecurityUtils.getUserId();
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskAssignee(String.valueOf(userId))
                .orderByTaskCreateTime().desc();
        if (StringUtils.isNotEmpty(query.getTaskName()))
        {
            taskQuery.taskNameLike("%" + query.getTaskName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionName()))
        {
            taskQuery.processDefinitionNameLike("%" + query.getProcessDefinitionName() + "%");
        }
        List<Task> tasks = taskQuery.list();
        List<FlowTask> list = new ArrayList<>();
        for (Task task : tasks)
        {
            list.add(convertTask(task));
        }
        return list;
    }

    @Override
    public long countTodo()
    {
        Long userId = SecurityUtils.getUserId();
        return taskService.createTaskQuery().taskAssignee(String.valueOf(userId)).count();
    }

    @Override
    public List<FlowTask> selectDoneList(FlowTask query)
    {
        Long userId = SecurityUtils.getUserId();
        HistoricTaskInstanceQuery hisQuery = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(String.valueOf(userId))
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc();
        if (StringUtils.isNotEmpty(query.getTaskName()))
        {
            hisQuery.taskNameLike("%" + query.getTaskName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionName()))
        {
            hisQuery.processDefinitionNameLike("%" + query.getProcessDefinitionName() + "%");
        }
        List<HistoricTaskInstance> tasks = hisQuery.list();
        List<FlowTask> list = new ArrayList<>();
        for (HistoricTaskInstance task : tasks)
        {
            FlowTask ft = convertHistoricTask(task);
            // 取该任务的审批意见（type=comment），解析【outcome】前缀
            List<Comment> comments = taskService.getTaskComments(task.getId());
            if (!comments.isEmpty())
            {
                Comment comment = comments.get(0);
                applyComment(ft, comment);
            }
            list.add(ft);
        }
        return list;
    }

    @Override
    @Transactional
    public int completeTask(FlowCompleteDTO dto)
    {
        Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已办理");
        }
        Long userId = SecurityUtils.getUserId();
        if (!String.valueOf(userId).equals(task.getAssignee()))
        {
            throw new ServiceException("您不是该任务的办理人");
        }
        String outcome = StringUtils.isNotEmpty(dto.getOutcome()) ? dto.getOutcome() : "approved";
        // 记录审批意见：type 必须为 "comment"（Flowable 默认查询仅返回该类型），
        // outcome 编码进 FULL_MSG_ 前缀【outcome:xxx】便于读取
        taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(),
                "comment", "【" + outcome + "】" + (dto.getComment() == null ? "" : dto.getComment()));
        if (dto.getVariables() != null && !dto.getVariables().isEmpty())
        {
            taskService.complete(dto.getTaskId(), dto.getVariables());
        }
        else
        {
            taskService.complete(dto.getTaskId());
        }
        // 流程已结束则直接回写示例业务表（确定性回调，监听器作兜底）
        if (runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).count() == 0)
        {
            try
            {
                com.ruoyi.flowable.domain.FlowLeave q = new com.ruoyi.flowable.domain.FlowLeave();
                q.setInstanceId(task.getProcessInstanceId());
                java.util.List<com.ruoyi.flowable.domain.FlowLeave> leaves =
                        flowLeaveMapper.selectFlowLeaveList(q);
                for (com.ruoyi.flowable.domain.FlowLeave lv : leaves)
                {
                    if ("1".equals(lv.getStatus()))
                    {
                        com.ruoyi.flowable.domain.FlowLeave update =
                                new com.ruoyi.flowable.domain.FlowLeave();
                        update.setLeaveId(lv.getLeaveId());
                        update.setStatus("rejected".equals(outcome) ? "3" : "2");
                        update.setAuditOpinion(dto.getComment());
                        update.setUpdateBy("flowable");
                        flowLeaveMapper.updateFlowLeave(update);
                    }
                }
            }
            catch (Exception ignored)
            {
            }
        }
        return 1;
    }

    @Override
    @Transactional
    public int delegateTask(String taskId, Long targetUserId, String comment)
    {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在");
        }
        if (targetUserId == null)
        {
            throw new ServiceException("转办人不能为空");
        }
        taskService.setAssignee(taskId, String.valueOf(targetUserId));
        // type 固定为 "comment"，转办标识编码进消息前缀
        taskService.addComment(taskId, task.getProcessInstanceId(), "comment",
                "【delegate】转办：" + (comment == null ? "" : comment));
        return 1;
    }

    @Override
    public List<FlowTask> selectHistoryTasks(String instanceId)
    {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();
        List<FlowTask> list = new ArrayList<>();
        for (HistoricTaskInstance task : tasks)
        {
            FlowTask ft = convertHistoricTask(task);
            List<Comment> comments = taskService.getTaskComments(task.getId());
            for (Comment comment : comments)
            {
                applyComment(ft, comment);
            }
            list.add(ft);
        }
        return list;
    }

    @Override
    public byte[] generateProcessDiagram(String instanceId)
    {
        try
        {
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            if (hpi == null)
            {
                throw new ServiceException("流程实例不存在");
            }
            BpmnModel bpmnModel = repositoryService.getBpmnModel(hpi.getProcessDefinitionId());
            // 高亮已流转节点
            List<HistoricActivityInstance> activityInstances = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instanceId).list();
            List<String> highLightedActivities = new ArrayList<>();
            List<String> highLightedFlows = new ArrayList<>();
            for (HistoricActivityInstance hai : activityInstances)
            {
                highLightedActivities.add(hai.getActivityId());
                if ("sequenceFlow".equals(hai.getActivityType()))
                {
                    highLightedFlows.add(hai.getActivityId());
                }
            }
            ProcessDiagramGenerator diagramGenerator = processEngine.getProcessEngineConfiguration()
                    .getProcessDiagramGenerator();
            try (InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png",
                    highLightedActivities, highLightedFlows,
                    "宋体", "宋体", "宋体", null, 1.0, false))
            {
                return in.readAllBytes();
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException("生成流程图失败：" + e.getMessage());
        }
    }

    /** 解析审批意见：FULL_MSG_ 格式为【outcome】正文 */
    private void applyComment(FlowTask ft, Comment comment)
    {
        String msg = comment.getFullMessage();
        if (StringUtils.isEmpty(msg))
        {
            return;
        }
        if (msg.startsWith("【") && msg.contains("】"))
        {
            int end = msg.indexOf("】");
            ft.setOutcome(msg.substring(1, end));
            ft.setComment(msg.substring(end + 1));
        }
        else
        {
            ft.setComment(msg);
        }
    }

    /** 历史流程实例转视图对象 */
    private FlowInstance convertInstance(HistoricProcessInstance hpi)
    {
        FlowInstance fi = new FlowInstance();
        fi.setInstanceId(hpi.getId());
        fi.setProcessDefinitionId(hpi.getProcessDefinitionId());
        fi.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
        fi.setProcessDefinitionName(hpi.getProcessDefinitionName());
        fi.setProcessDefinitionVersion(hpi.getProcessDefinitionVersion());
        fi.setBusinessKey(hpi.getBusinessKey());
        fi.setStartTime(hpi.getStartTime());
        fi.setEndTime(hpi.getEndTime());
        if (StringUtils.isNotEmpty(hpi.getStartUserId()))
        {
            try
            {
                fi.setStartUserId(Long.valueOf(hpi.getStartUserId()));
            }
            catch (NumberFormatException ignored) { }
        }
        if (hpi.getEndTime() == null)
        {
            fi.setStatus("running");
            // 当前活动节点
            List<HistoricActivityInstance> active = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(hpi.getId()).unfinished().list();
            if (!active.isEmpty())
            {
                fi.setCurrentActivityName(active.get(0).getActivityName());
            }
        }
        else if (StringUtils.isNotEmpty(hpi.getDeleteReason()))
        {
            fi.setStatus("canceled");
        }
        else
        {
            fi.setStatus("completed");
        }
        return fi;
    }

    /** 运行中任务转视图对象 */
    private FlowTask convertTask(Task task)
    {
        FlowTask ft = new FlowTask();
        ft.setTaskId(task.getId());
        ft.setTaskName(task.getName());
        ft.setTaskDefinitionKey(task.getTaskDefinitionKey());
        ft.setInstanceId(task.getProcessInstanceId());
        ft.setProcessDefinitionId(task.getProcessDefinitionId());
        ft.setCreateTime(task.getCreateTime());
        if (StringUtils.isNotEmpty(task.getAssignee()))
        {
            try
            {
                ft.setAssigneeId(Long.valueOf(task.getAssignee()));
            }
            catch (NumberFormatException ignored) { }
        }
        fillProcessInfo(ft, task.getProcessInstanceId());
        return ft;
    }

    /** 历史任务转视图对象 */
    private FlowTask convertHistoricTask(HistoricTaskInstance task)
    {
        FlowTask ft = new FlowTask();
        ft.setTaskId(task.getId());
        ft.setTaskName(task.getName());
        ft.setTaskDefinitionKey(task.getTaskDefinitionKey());
        ft.setInstanceId(task.getProcessInstanceId());
        ft.setProcessDefinitionId(task.getProcessDefinitionId());
        ft.setCreateTime(task.getCreateTime());
        ft.setEndTime(task.getEndTime());
        if (StringUtils.isNotEmpty(task.getAssignee()))
        {
            try
            {
                ft.setAssigneeId(Long.valueOf(task.getAssignee()));
            }
            catch (NumberFormatException ignored) { }
        }
        fillProcessInfo(ft, task.getProcessInstanceId());
        return ft;
    }

    /** 补充流程名称、业务主键、发起人 */
    private void fillProcessInfo(FlowTask ft, String instanceId)
    {
        try
        {
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            if (hpi != null)
            {
                ft.setProcessDefinitionName(hpi.getProcessDefinitionName());
                ft.setBusinessKey(hpi.getBusinessKey());
            }
        }
        catch (Exception ignored) { }
    }
}
