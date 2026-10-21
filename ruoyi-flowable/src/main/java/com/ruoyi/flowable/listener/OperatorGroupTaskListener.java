package com.ruoyi.flowable.listener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.spring.SpringUtils;

/**
 * 操作组任务监听器：任务创建时解析节点扩展属性 flowable:operatorGroups，
 * 将「指定部门/指定分部/指定岗位/流程创建人」解析为候选用户（运行态真实分配）。
 *
 * 说明：
 * - 指定人（user）/指定角色（role）已由 BPMN 内置属性 flowable:assignee/candidateGroups 生效，此处不重复分配；
 * - 所有人（all）/节点操作者（nodeOp）一期跳过（避免全公司加待办 / 依赖前一节点操作者语义），后续单独接入；
 * - 组内条件表达式（condition）与多批次（batch>0 串行）一期跳过，全部候选一次分配；
 * - 监听器在引擎初始化阶段注册，引擎服务统一通过 SpringUtils 惰性获取（避免循环依赖）。
 *
 * @author ruoyi
 */
public class OperatorGroupTaskListener implements FlowableEventListener
{
    private static final Logger log = LoggerFactory.getLogger(OperatorGroupTaskListener.class);

    @Override
    public void onEvent(FlowableEvent event)
    {
        if (!(event instanceof FlowableEngineEntityEvent)
                || !FlowableEngineEventType.TASK_CREATED.equals(event.getType()))
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
            // 已设置办理人的节点不处理（BPMN 内置 assignee 已生效）
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
            // 已配置候选组的节点（内置 candidateGroups）不重复处理
            if (userTask.getCandidateGroups() != null && !userTask.getCandidateGroups().isEmpty())
            {
                return;
            }
            String raw = readOperatorGroups(userTask);
            if (raw == null || raw.isEmpty())
            {
                return;
            }
            Set<String> users = resolveUsers(raw, task);
            if (users.isEmpty())
            {
                return;
            }
            TaskService taskService = SpringUtils.getBean(TaskService.class);
            for (String userId : users)
            {
                taskService.addCandidateUser(task.getId(), userId);
            }
            log.info("操作组候选分配：任务 {}（{}）分配候选用户 {}", task.getId(), task.getName(), users);
        }
        catch (Exception e)
        {
            // 候选分配失败不阻塞流程流转
            log.error("操作组候选分配失败：" + e.getMessage(), e);
        }
    }

    /** 读取节点扩展属性 flowable:operatorGroups（JSON 数组字符串） */
    private String readOperatorGroups(UserTask userTask)
    {
        Map<String, List<ExtensionAttribute>> attrs = userTask.getAttributes();
        if (attrs == null)
        {
            return null;
        }
        List<ExtensionAttribute> list = attrs.get("operatorGroups");
        if (list == null || list.isEmpty())
        {
            return null;
        }
        return list.get(0).getValue();
    }

    /** 解析操作组 JSON，按类型映射为候选用户集合（批次升序合并、去重） */
    private Set<String> resolveUsers(String raw, TaskEntity task)
    {
        Set<String> users = new LinkedHashSet<>();
        JSONArray groups;
        try
        {
            groups = JSON.parseArray(raw);
        }
        catch (Exception e)
        {
            return users;
        }
        if (groups == null || groups.isEmpty())
        {
            return users;
        }
        List<JSONObject> sorted = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++)
        {
            sorted.add(groups.getJSONObject(i));
        }
        // 按批次号升序（一期全部一次分配，仅排序保证稳定性）
        sorted.sort((a, b) -> Integer.compare(a.getIntValue("batch"), b.getIntValue("batch")));
        for (JSONObject g : sorted)
        {
            String type = g.getString("type");
            if (type == null)
            {
                continue;
            }
            switch (type)
            {
                case "user":
                {
                    String userId = g.getString("userId");
                    if (userId != null && !userId.isEmpty())
                    {
                        users.add(userId);
                    }
                    break;
                }
                case "dept":
                {
                    users.addAll(usersByDept(g.getString("refId")));
                    break;
                }
                case "subDept":
                {
                    users.addAll(usersBySubDept(g.getString("refId")));
                    break;
                }
                case "role":
                {
                    // 内置 candidateGroups 已生效，兜底再按角色查一次（重复添加由 Set 去重）
                    users.addAll(usersByRole(g.getString("roleId")));
                    break;
                }
                case "post":
                {
                    users.addAll(usersByPost(g.getString("refId")));
                    break;
                }
                case "creator":
                {
                    RuntimeService runtimeService = SpringUtils.getBean(RuntimeService.class);
                    org.flowable.engine.runtime.ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                            .processInstanceId(task.getProcessInstanceId()).singleResult();
                    if (pi != null && pi.getStartUserId() != null && !pi.getStartUserId().isEmpty())
                    {
                        users.add(pi.getStartUserId());
                    }
                    break;
                }
                default:
                    // all / nodeOp 一期跳过
                    break;
            }
        }
        return users;
    }

    /** 指定部门 → 部门下所有有效用户 */
    private List<String> usersByDept(String deptId)
    {
        List<String> result = new ArrayList<>();
        if (deptId == null || deptId.isEmpty())
        {
            return result;
        }
        try
        {
            org.springframework.jdbc.core.JdbcTemplate jdbc = SpringUtils.getBean(org.springframework.jdbc.core.JdbcTemplate.class);
            jdbc.query("select user_id from sys_user where dept_id = ? and del_flag = '0' and status = '0'",
                    rs -> { result.add(rs.getString(1)); }, Long.parseLong(deptId));
        }
        catch (Exception e)
        {
            log.warn("按部门查询候选用户失败：{}", e.getMessage());
        }
        return result;
    }

    /** 指定分部 → 分部（含子孙部门）下所有有效用户（ancestors 递归，RuoYi 惯例 find_in_set） */
    private List<String> usersBySubDept(String deptId)
    {
        List<String> result = new ArrayList<>();
        if (deptId == null || deptId.isEmpty())
        {
            return result;
        }
        try
        {
            org.springframework.jdbc.core.JdbcTemplate jdbc = SpringUtils.getBean(org.springframework.jdbc.core.JdbcTemplate.class);
            jdbc.query("select user_id from sys_user where del_flag = '0' and status = '0' and dept_id in "
                    + "(select dept_id from sys_dept where dept_id = ? or find_in_set(?, ancestors))",
                    rs -> { result.add(rs.getString(1)); }, Long.parseLong(deptId), deptId);
        }
        catch (Exception e)
        {
            log.warn("按分部查询候选用户失败：{}", e.getMessage());
        }
        return result;
    }

    /** 指定岗位 → 岗位关联的所有有效用户（sys_user_post） */
    private List<String> usersByPost(String postId)
    {
        List<String> result = new ArrayList<>();
        if (postId == null || postId.isEmpty())
        {
            return result;
        }
        try
        {
            org.springframework.jdbc.core.JdbcTemplate jdbc = SpringUtils.getBean(org.springframework.jdbc.core.JdbcTemplate.class);
            jdbc.query("select up.user_id from sys_user_post up join sys_user u on u.user_id = up.user_id "
                    + "where up.post_id = ? and u.del_flag = '0' and u.status = '0'",
                    rs -> { result.add(rs.getString(1)); }, Long.parseLong(postId));
        }
        catch (Exception e)
        {
            log.warn("按岗位查询候选用户失败：{}", e.getMessage());
        }
        return result;
    }

    /** 指定角色 → 角色下用户（兜底解析，内置 candidateGroups 为主链路） */
    private List<String> usersByRole(String roleId)
    {
        List<String> result = new ArrayList<>();
        if (roleId == null || roleId.isEmpty())
        {
            return result;
        }
        try
        {
            org.springframework.jdbc.core.JdbcTemplate jdbc = SpringUtils.getBean(org.springframework.jdbc.core.JdbcTemplate.class);
            jdbc.query("select ur.user_id from sys_user_role ur join sys_user u on u.user_id = ur.user_id "
                    + "where ur.role_id = ? and u.del_flag = '0' and u.status = '0'",
                    rs -> { result.add(rs.getString(1)); }, Long.parseLong(roleId));
        }
        catch (Exception e)
        {
            log.warn("按角色查询候选用户失败：{}", e.getMessage());
        }
        return result;
    }

    @Override
    public boolean isFailOnException()
    {
        // 候选分配异常不阻塞流程
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
