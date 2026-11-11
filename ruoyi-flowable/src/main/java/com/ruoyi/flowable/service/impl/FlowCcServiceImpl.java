package com.ruoyi.flowable.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowCc;
import com.ruoyi.flowable.mapper.FlowCcMapper;
import com.ruoyi.flowable.service.FlowParticipantResolver;
import com.ruoyi.flowable.service.IFlowCcService;

/**
 * 流程抄送（待阅）Service 实现
 *
 * 数据边界一律取当前登录人，前端传的 receiverId 不被信任。
 *
 * @author ruoyi
 */
@Service
public class FlowCcServiceImpl implements IFlowCcService
{
    private static final Logger log = LoggerFactory.getLogger(FlowCcServiceImpl.class);

    @Autowired
    private FlowCcMapper flowCcMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FlowParticipantResolver flowParticipantResolver;

    @Override
    public List<FlowCc> selectMyCcList(FlowCc query)
    {
        if (query == null)
        {
            query = new FlowCc();
        }
        query.setReceiverId(currentUserId());
        return flowCcMapper.selectMyCcList(query);
    }

    @Override
    public int countUnread()
    {
        return flowCcMapper.countUnread(currentUserId());
    }

    @Override
    public int markRead(Long ccId)
    {
        if (ccId == null)
        {
            throw new ServiceException("抄送ID不能为空");
        }
        return flowCcMapper.markRead(ccId, currentUserId());
    }

    @Override
    public int markAllRead()
    {
        return flowCcMapper.markAllRead(currentUserId());
    }

    @Override
    public int sendCc(String taskId, List<Long> userIds, String comment)
    {
        if (StringUtils.isEmpty(taskId))
        {
            throw new ServiceException("任务标识不能为空");
        }
        if (userIds == null || userIds.isEmpty())
        {
            throw new ServiceException("请选择抄送人员");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已办理");
        }
        Long userId = currentUserId();
        if (!String.valueOf(userId).equals(task.getAssignee()))
        {
            throw new ServiceException("您不是该任务的办理人");
        }
        // 抄送人本人不抄送给自己
        LinkedHashSet<Long> targets = new LinkedHashSet<>();
        for (Long id : userIds)
        {
            if (id != null && !id.equals(userId))
            {
                targets.add(id);
            }
        }
        if (targets.isEmpty())
        {
            throw new ServiceException("不能抄送给自己，请重新选择接收人");
        }
        String instanceId = task.getProcessInstanceId();
        HistoricProcessInstance instance = historicInstance(instanceId);
        String processName = null;
        String processKey = null;
        try
        {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
            if (bpmnModel != null && bpmnModel.getMainProcess() != null)
            {
                processName = bpmnModel.getMainProcess().getName();
                processKey = bpmnModel.getMainProcess().getId();
            }
        }
        catch (Exception e)
        {
            log.warn("手动抄送：读取流程定义失败 {}", e.getMessage());
        }
        String title = instance == null ? null : instance.getName();
        if (StringUtils.isEmpty(title))
        {
            title = processName;
        }
        String senderName = currentUserName();
        List<String> idTexts = new ArrayList<>();
        for (Long id : targets)
        {
            idTexts.add(String.valueOf(id));
        }
        Map<String, String> names = flowParticipantResolver.userNames(idTexts);
        int inserted = 0;
        for (Long receiverId : targets)
        {
            // 同一流程同一节点同一接收人只保留一条（与节点级自动抄送去重）
            if (flowCcMapper.countByInstanceTaskReceiver(instanceId, taskId, receiverId) > 0)
            {
                continue;
            }
            FlowCc cc = new FlowCc();
            cc.setInstanceId(instanceId);
            cc.setProcessKey(processKey);
            cc.setProcessName(processName);
            cc.setBusinessKey(instance == null ? null : instance.getBusinessKey());
            cc.setTitle(title);
            cc.setTaskId(taskId);
            cc.setTaskName(task.getName());
            cc.setSenderId(userId);
            cc.setSenderName(senderName);
            cc.setReceiverId(receiverId);
            cc.setReceiverName(names.get(String.valueOf(receiverId)));
            cc.setReadFlag("0");
            flowCcMapper.insertCc(cc);
            inserted++;
        }
        log.info("手动抄送：任务 {}（{}）由 {} 抄送给 {} 人{}", taskId, task.getName(), senderName, inserted,
                StringUtils.isEmpty(comment) ? "" : "，说明：" + comment);
        return inserted;
    }

    @Override
    public List<FlowCc> selectByInstance(String instanceId)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            return new ArrayList<>();
        }
        return flowCcMapper.selectByInstance(instanceId);
    }

    private HistoricProcessInstance historicInstance(String instanceId)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            return null;
        }
        try
        {
            return historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
        }
        catch (Exception e)
        {
            log.warn("手动抄送：读取历史实例失败 {}", e.getMessage());
            return null;
        }
    }

    private String currentUserName()
    {
        try
        {
            SysUser user = SecurityUtils.getLoginUser().getUser();
            if (user != null)
            {
                return StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName();
            }
        }
        catch (Exception ignored)
        {
        }
        return SecurityUtils.getUsername();
    }

    private Long currentUserId()
    {
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
        {
            throw new ServiceException("未获取到当前登录用户");
        }
        return userId;
    }
}
