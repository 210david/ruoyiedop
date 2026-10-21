package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowLeave;
import com.ruoyi.flowable.mapper.FlowLeaveMapper;
import com.ruoyi.flowable.service.IFlowLeaveService;
import com.ruoyi.system.service.ISysUserService;

/**
 * 请假单 Service 实现
 *
 * 状态流转：0草稿 →(submit)→ 1审批中 →(流程结束)→ 2已通过/3已驳回；发起人撤销 → 4已撤销
 *
 * @author ruoyi
 */
@Service
public class FlowLeaveServiceImpl implements IFlowLeaveService
{
    @Autowired
    private FlowLeaveMapper flowLeaveMapper;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private org.flowable.engine.RuntimeService runtimeService;

    @Autowired
    private org.flowable.engine.RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Override
    public List<FlowLeave> selectFlowLeaveList(FlowLeave leave)
    {
        // 数据权限：仅本人可见
        leave.setStartUserId(SecurityUtils.getUserId());
        return flowLeaveMapper.selectFlowLeaveList(leave);
    }

    @Override
    public FlowLeave selectFlowLeaveByLeaveId(Long leaveId)
    {
        FlowLeave leave = flowLeaveMapper.selectFlowLeaveByLeaveId(leaveId);
        if (leave == null)
        {
            throw new ServiceException("请假单不存在");
        }
        if (!SecurityUtils.getUserId().equals(leave.getStartUserId()))
        {
            throw new ServiceException("无权查看他人的请假单");
        }
        return leave;
    }

    @Override
    @Transactional
    public int insertFlowLeave(FlowLeave leave)
    {
        SysUser user = userService.selectUserById(SecurityUtils.getUserId());
        leave.setStatus("0");
        leave.setStartUserId(user.getUserId());
        leave.setStartUserName(user.getNickName());
        leave.setCreateBy(user.getUserName());
        return flowLeaveMapper.insertFlowLeave(leave);
    }

    @Override
    @Transactional
    public int updateFlowLeave(FlowLeave leave)
    {
        FlowLeave db = checkEditable(leave.getLeaveId());
        leave.setStatus(db.getStatus());
        leave.setStartUserId(null);
        leave.setInstanceId(null);
        leave.setUpdateBy(SecurityUtils.getUsername());
        return flowLeaveMapper.updateFlowLeave(leave);
    }

    @Override
    @Transactional
    public int deleteFlowLeaveByLeaveIds(Long[] leaveIds)
    {
        for (Long leaveId : leaveIds)
        {
            checkEditable(leaveId);
        }
        return flowLeaveMapper.deleteFlowLeaveByLeaveIds(leaveIds);
    }

    @Override
    @Transactional
    public String submitFlowLeave(Long leaveId, Long approverUserId)
    {
        FlowLeave leave = checkEditable(leaveId);
        if (approverUserId == null)
        {
            throw new ServiceException("请选择审批人");
        }
        // 发起流程：businessKey = leaveId，变量 approver 供 UserTask 表达式 ${approver} 使用
        org.flowable.engine.repository.ProcessDefinition definition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey("leave_process")
                .latestVersion().singleResult();
        if (definition == null)
        {
            throw new ServiceException("流程[leave_process]未部署，请先在流程模型中部署");
        }
        if (definition.isSuspended())
        {
            throw new ServiceException("流程[leave_process]已挂起，无法发起");
        }
        java.util.Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("approver", String.valueOf(approverUserId));
        variables.put("leaveId", leaveId);
        variables.put("days", leave.getDays());
        String username = SecurityUtils.getUsername();
        org.flowable.common.engine.impl.identity.Authentication
                .setAuthenticatedUserId(String.valueOf(SecurityUtils.getUserId()));
        try
        {
            org.flowable.engine.runtime.ProcessInstance instance = runtimeService
                    .startProcessInstanceById(definition.getId(), String.valueOf(leaveId), variables);
            leave.setStatus("1");
            leave.setInstanceId(instance.getId());
            leave.setUpdateBy(username);
            flowLeaveMapper.updateFlowLeave(leave);
            return instance.getId();
        }
        finally
        {
            org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(null);
        }
    }

    @Override
    @Transactional
    public void onProcessFinished(String instanceId, String businessKey)
    {
        if (StringUtils.isEmpty(businessKey))
        {
            return;
        }
        FlowLeave leave = flowLeaveMapper.selectFlowLeaveByLeaveId(Long.valueOf(businessKey));
        if (leave == null || !"1".equals(leave.getStatus()))
        {
            return;
        }
        // 取最后一个已办任务的审批结果作为最终结论
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId).finished()
                .orderByHistoricTaskInstanceEndTime().desc().list();
        String outcome = "approved";
        String opinion = null;
        for (HistoricTaskInstance task : tasks)
        {
            List<Comment> comments = taskService.getTaskComments(task.getId());
            if (!comments.isEmpty())
            {
                String msg = comments.get(0).getFullMessage();
                if (StringUtils.isNotEmpty(msg) && msg.startsWith("【") && msg.contains("】"))
                {
                    outcome = msg.substring(1, msg.indexOf("】"));
                    opinion = msg.substring(msg.indexOf("】") + 1);
                }
                break;
            }
        }
        FlowLeave update = new FlowLeave();
        update.setLeaveId(leave.getLeaveId());
        update.setStatus("rejected".equals(outcome) ? "3" : "2");
        update.setAuditOpinion(opinion);
        update.setUpdateBy("flowable");
        flowLeaveMapper.updateFlowLeave(update);
    }

    /** 校验单据可编辑（草稿/驳回，且属于当前用户） */
    private FlowLeave checkEditable(Long leaveId)
    {
        FlowLeave db = flowLeaveMapper.selectFlowLeaveByLeaveId(leaveId);
        if (db == null)
        {
            throw new ServiceException("请假单不存在");
        }
        if (!SecurityUtils.getUserId().equals(db.getStartUserId()))
        {
            throw new ServiceException("无权操作他人的请假单");
        }
        if (!"0".equals(db.getStatus()) && !"3".equals(db.getStatus()))
        {
            throw new ServiceException("仅草稿或已驳回状态可操作");
        }
        return db;
    }
}
