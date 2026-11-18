package com.ruoyi.flowable.listener;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.config.FlowReturnConstants;
import com.ruoyi.flowable.service.FlowParticipantResolver;
import com.ruoyi.flowable.service.FlowRuntimeModelService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 流程待办消息监听器（流程待审批 → 系统消息中心）
 *
 * 与消息中心（sys_message）的衔接口径：
 * <ul>
 * <li>任务到达（TASK_CREATED）：向办理人 / 候选办理人各推送一条「审批消息」，点击直达任务办理页；</li>
 * <li>任务办结（TASK_COMPLETED / 任务删除）：把该任务的消息标记为已处理，未读列表不再出现；</li>
 * <li>流程办结或终止（PROCESS_COMPLETED / PROCESS_CANCELLED）：把该流程实例的全部待办消息标记为已处理。</li>
 * </ul>
 *
 * 消息通过 redirect_url 携带 taskId / instanceId，用于办结后按链接关键字定位并关闭消息，
 * 无需改动 sys_message 表结构。发起人节点（退回后重新提交）不推送，避免给自己发待办。
 *
 * @author ruoyi
 */
public class FlowTaskMessageListener implements FlowableEventListener
{
    private static final Logger log = LoggerFactory.getLogger(FlowTaskMessageListener.class);

    /** 消息业务来源（字典 sys_message_biz_source） */
    private static final String BIZ_SOURCE = "flow";

    @Override
    public void onEvent(FlowableEvent event)
    {
        try
        {
            if (!(event instanceof FlowableEngineEntityEvent))
            {
                return;
            }
            Object entity = ((FlowableEngineEntityEvent) event).getEntity();
            if (FlowableEngineEventType.TASK_CREATED.equals(event.getType()))
            {
                if (entity instanceof TaskEntity)
                {
                    onTaskCreated((TaskEntity) entity);
                }
                return;
            }
            if (FlowableEngineEventType.TASK_COMPLETED.equals(event.getType())
                    || FlowableEngineEventType.ENTITY_DELETED.equals(event.getType()))
            {
                if (entity instanceof TaskEntity)
                {
                    closeTaskMessages(((TaskEntity) entity).getId());
                }
                return;
            }
            if (FlowableEngineEventType.PROCESS_COMPLETED.equals(event.getType())
                    || FlowableEngineEventType.PROCESS_CANCELLED.equals(event.getType()))
            {
                String instanceId = processInstanceId(entity);
                if (instanceId != null)
                {
                    closeInstanceMessages(instanceId);
                }
            }
        }
        catch (Exception e)
        {
            // 消息推送失败不影响流程流转
            log.error("流程待办消息处理失败：" + e.getMessage(), e);
        }
    }

    /** 任务到达：向办理人推送审批消息 */
    private void onTaskCreated(TaskEntity task)
    {
        if (task == null || StringUtils.isEmpty(task.getId()) || StringUtils.isEmpty(task.getProcessDefinitionId()))
        {
            return;
        }
        // 发起人节点（退回后重新提交）不推送，避免给自己发待办
        if (FlowReturnConstants.INITIATOR_NODE_ID.equals(task.getTaskDefinitionKey()))
        {
            return;
        }
        // 连续节点办理人相同时会自动跳过，此时任务已办结，不再推送待办消息
        if (isFinished(task.getId()))
        {
            return;
        }
        Set<Long> recipients = resolveRecipients(task);
        if (recipients.isEmpty())
        {
            log.debug("流程任务 {}（{}）未解析到办理人，跳过消息推送", task.getId(), task.getName());
            return;
        }
        String processInstanceId = task.getProcessInstanceId();
        String processName = processName(task.getProcessDefinitionId());
        String title = instanceTitle(processInstanceId);
        if (StringUtils.isEmpty(title))
        {
            title = processName;
        }
        String initiatorName = initiatorName(processInstanceId);
        String taskName = task.getName() == null ? processName : task.getName();
        String msgTitle = "流程待审批：" + (StringUtils.isEmpty(title) ? taskName : title);
        StringBuilder content = new StringBuilder();
        if (StringUtils.isNotEmpty(processName))
        {
            content.append("流程「").append(processName).append("」");
        }
        if (StringUtils.isNotEmpty(initiatorName))
        {
            content.append("由 ").append(initiatorName).append(" 发起");
        }
        if (content.length() > 0)
        {
            content.append("，");
        }
        content.append("当前节点：").append(taskName).append("，请及时办理。");
        String redirectUrl = buildRedirectUrl(task, title, processName, initiatorName);
        MessageHelper messageHelper = SpringUtils.getBean(MessageHelper.class);
        for (Long recipientId : recipients)
        {
            messageHelper.sendMessageToUser(msgTitle, content.toString(), "3", "2",
                    BIZ_SOURCE, null, redirectUrl, recipientId, "流程办理");
        }
    }

    /** 任务是否已办结（任务创建监听器内可能已被「同人自动跳过」直接办理） */
    private boolean isFinished(String taskId)
    {
        if (StringUtils.isEmpty(taskId))
        {
            return false;
        }
        try
        {
            return SpringUtils.getBean(HistoryService.class).createHistoricTaskInstanceQuery()
                    .taskId(taskId).finished().count() > 0;
        }
        catch (Exception e)
        {
            log.warn("查询流程任务办结状态失败：taskId={}, {}", taskId, e.getMessage());
            return false;
        }
    }

    /** 办理人解析：已指定办理人优先，其次取候选用户 */
    private Set<Long> resolveRecipients(TaskEntity task)
    {
        Set<Long> recipients = new LinkedHashSet<>();
        addUser(recipients, task.getAssignee());
        if (recipients.isEmpty())
        {
            try
            {
                List<IdentityLink> links = SpringUtils.getBean(TaskService.class).getIdentityLinksForTask(task.getId());
                if (links != null)
                {
                    for (IdentityLink link : links)
                    {
                        addUser(recipients, link.getUserId());
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("查询流程任务候选办理人失败：taskId={}, {}", task.getId(), e.getMessage());
            }
        }
        return recipients;
    }

    private void addUser(Set<Long> recipients, String userId)
    {
        if (StringUtils.isNotEmpty(userId) && userId.matches("\\d+"))
        {
            recipients.add(Long.valueOf(userId));
        }
    }

    /** 关闭单个任务对应的待办消息 */
    private void closeTaskMessages(String taskId)
    {
        if (StringUtils.isEmpty(taskId))
        {
            return;
        }
        SpringUtils.getBean(com.ruoyi.system.service.ISysMessageService.class)
                .markHandledByUrl(BIZ_SOURCE, "taskId=" + taskId);
    }

    /** 关闭整个流程实例对应的待办消息（办结 / 终止 / 撤销） */
    private void closeInstanceMessages(String instanceId)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            return;
        }
        SpringUtils.getBean(com.ruoyi.system.service.ISysMessageService.class)
                .markHandledByUrl(BIZ_SOURCE, "instanceId=" + instanceId);
    }

    private String processInstanceId(Object entity)
    {
        if (entity instanceof ExecutionEntity)
        {
            ExecutionEntity execution = (ExecutionEntity) entity;
            return execution.getProcessInstanceId() != null ? execution.getProcessInstanceId() : execution.getId();
        }
        if (entity instanceof ProcessInstance)
        {
            return ((ProcessInstance) entity).getId();
        }
        return null;
    }

    /** 流程名称（设计器最新模型） */
    private String processName(String processDefinitionId)
    {
        try
        {
            BpmnModel model = SpringUtils.getBean(FlowRuntimeModelService.class).getByDefinitionId(processDefinitionId);
            if (model != null && model.getMainProcess() != null)
            {
                return model.getMainProcess().getName();
            }
        }
        catch (Exception e)
        {
            log.warn("读取流程名称失败：{}", e.getMessage());
        }
        return null;
    }

    /** 流程实例标题（发起时填写的标题，取不到时退回流程名称） */
    private String instanceTitle(String processInstanceId)
    {
        if (StringUtils.isEmpty(processInstanceId))
        {
            return null;
        }
        try
        {
            ProcessInstance runtime = SpringUtils.getBean(RuntimeService.class)
                    .createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (runtime != null)
            {
                return runtime.getName();
            }
            HistoricProcessInstance history = SpringUtils.getBean(HistoryService.class)
                    .createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            return history == null ? null : history.getName();
        }
        catch (Exception e)
        {
            log.warn("读取流程实例标题失败：{}", e.getMessage());
            return null;
        }
    }

    /** 流程发起人姓名 */
    private String initiatorName(String processInstanceId)
    {
        if (StringUtils.isEmpty(processInstanceId))
        {
            return null;
        }
        try
        {
            String startUserId = null;
            ProcessInstance runtime = SpringUtils.getBean(RuntimeService.class)
                    .createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (runtime != null)
            {
                startUserId = runtime.getStartUserId();
            }
            if (StringUtils.isEmpty(startUserId))
            {
                HistoricProcessInstance history = SpringUtils.getBean(HistoryService.class)
                        .createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
                startUserId = history == null ? null : history.getStartUserId();
            }
            if (StringUtils.isEmpty(startUserId))
            {
                return null;
            }
            List<String> ids = new ArrayList<>();
            ids.add(startUserId);
            Map<String, String> names = SpringUtils.getBean(FlowParticipantResolver.class).userNames(ids);
            return names.get(startUserId);
        }
        catch (Exception e)
        {
            log.warn("读取流程发起人失败：{}", e.getMessage());
            return null;
        }
    }

    /** 任务办理页链接（redirect_url），参数与待办列表打开办理页保持一致 */
    private String buildRedirectUrl(TaskEntity task, String title, String processName, String initiatorName)
    {
        StringBuilder url = new StringBuilder("/flow/task/handle");
        url.append("?taskId=").append(enc(task.getId()));
        url.append("&taskName=").append(enc(task.getName()));
        url.append("&title=").append(enc(title));
        url.append("&instanceId=").append(enc(task.getProcessInstanceId()));
        url.append("&defName=").append(enc(processName));
        if (StringUtils.isNotEmpty(initiatorName))
        {
            url.append("&startUserName=").append(enc(initiatorName));
        }
        return url.toString();
    }

    private String enc(String value)
    {
        if (value == null)
        {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public boolean isFailOnException()
    {
        // 消息异常不阻塞流程
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
