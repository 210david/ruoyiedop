package com.ruoyi.flowable.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.listener.ParticipantResolveListener;

/**
 * 流程节点参与者解析器
 *
 * 把表单设计器落到 BPMN 扩展属性 flowable:participantConfig 上的「参与者」
 * （活动关系 / 组织关系 / 表单字段）在运行期解析为具体的候选用户ID，供两处复用：
 *
 * 1. {@link com.ruoyi.flowable.listener.ParticipantResolveListener}：任务创建事件里真正派发（指定 / 候选）；
 * 2. {@link com.ruoyi.flowable.controller.FlowParticipantController}：发起前预览下一步审批人。
 *
 * 参与者数据结构（与 views/flowable/model/index.vue 的「设置操作人员」一致）：
 * - 通用：{type:'user|dept|deptLeader|deptChargeLeader|role|post|nodeUser|nodeUserLeader', ids:[...], nodeId, includeChild, name}
 * - 活动：{type:'initiator|activityUser'}（发起人 / 上一节点办理人，带 mode:'activity'）
 * - 表单字段：{type:'fieldUser|fieldUserLeader|fieldDeptLeader|fieldDeptChargeLeader', field, fieldType, name}
 * - 节点级：handleMode=assign（指定）|candidate（候选）|runtime（提交时指定），
 *   performMode=claim（抢占）|and（会签）|seq（串签），fallbackUserIds 为无人可办时的兜底人
 * - 旧结构继续兼容：{mode:'activity', type:'initiator|activityUser|...'} / {mode:'org', type, kind, id} / {mode:'formField', type:'field', field}
 *
 * 数据说明：
 * - RuoYi 的 sys_dept.leader 存的是「负责人姓名」，需按 nick_name / user_name 反查用户；
 * - 分管领导取 sys_dept.charge_leader_id（部门编辑页可维护），缺失时向上回溯父级部门；
 * - 组织关系取不到负责人时向上回溯父级组织（最多 MAX_DEPT_CLIMB 层），避免流程停在无人可办的节点；
 * - 「流程节点操作人」按 act_hi_taskinst 中该节点最近一次已完成任务的办理人解析（需流程实例ID，发起前预览不可解析）；
 *   引用的是「申请（开始）节点」时按流程发起人解析（开始节点没有任务记录）。
 *
 * @author ruoyi
 */
@Component("participantCollection")
public class FlowParticipantResolver
{
    private static final Logger log = LoggerFactory.getLogger(FlowParticipantResolver.class);

    /** 组织负责人向上回溯的最大层数（防止脏数据造成死循环） */
    private static final int MAX_DEPT_CLIMB = 6;

    /**
     * 会签/串签多实例集合变量名前缀：participantList_节点ID
     *
     * 必须与前端 views/flowable/model/index.vue 写入 BPMN 的 flowable:collection 完全一致。
     */
    public static final String COLLECTION_PREFIX = "participantList_";

    /**
     * 多实例元素变量名：participantAssignee
     *
     * BPMN 里用户任务的办理人表达式固定为 ${participantAssignee}，与 flowable:elementVariable 一致。
     */
    public static final String ELEMENT_VARIABLE = "participantAssignee";

    /**
     * 会签/串签多实例集合的取值入口（BPMN 里写作表达式调用）
     *
     * 为什么用表达式而不是「进入节点前预置变量」：多实例的集合是在节点开始执行的同一刻求值的，
     * SEQUENCEFLOW_TAKEN 事件里 execution 的当前元素已切换为目标节点，靠事件预置存在时序风险
     * （并行网关下会漏写，导致引擎直接抛 Variable 'xxx' was not found）。
     * 改成 flowable:collection="${participantCollection.collectionFor('节点ID', execution)}"，
     * 由引擎在真正需要集合时回调，天然没有时序问题。
     *
     * @param nodeId    用户任务节点ID
     * @param execution 当前执行实例（引擎表达式自动注入）
     * @return 办理人集合，保证非空（解析不到人时返回兜底人 / 流程管理员）
     */
    public List<String> collectionFor(String nodeId, DelegateExecution execution)
    {
        List<String> userIds = new ArrayList<>();
        try
        {
            if (execution == null || blank(nodeId))
            {
                return adminUserIds();
            }
            BpmnModel bpmnModel = SpringUtils.getBean(RepositoryService.class)
                    .getBpmnModel(execution.getProcessDefinitionId());
            FlowElement element = bpmnModel == null ? null : bpmnModel.getFlowElement(nodeId);
            if (!(element instanceof UserTask))
            {
                log.error("会签/串签节点 {} 不存在或不是用户任务，无法解析办理人", nodeId);
                return adminUserIds();
            }
            UserTask userTask = (UserTask) element;
            Ctx ctx = ParticipantResolveListener.buildCtx(execution.getProcessInstanceId());
            Result resolved = resolveWithFallback(userTask, ctx);
            userIds.addAll(resolved.getUserIds());
            // 取人方式=提交时指定：上一环节选定的人优先（仍限制在候选范围内）
            JSONObject cfg = readConfig(userTask);
            if (cfg != null && "runtime".equals(cfg.getString("handleMode")))
            {
                List<String> designated = readDesignated(nodeId, ctx, resolved.getUserIds());
                if (!designated.isEmpty())
                {
                    userIds = designated;
                }
            }
            log.info("会签/串签参与者解析：节点 {}（{}）→ {}{}", nodeId, userTask.getName(), userIds,
                    resolved.isFailover() ? "（兜底）" : "");
        }
        catch (Exception e)
        {
            log.error("会签/串签参与者解析失败，节点 {}：{}", nodeId, e.getMessage(), e);
        }
        if (userIds.isEmpty())
        {
            // 空集合会让引擎直接跳过整个审批节点，宁可转管理员也不能漏审
            log.error("会签/串签节点 {} 解析不到办理人，已转流程管理员", nodeId);
            userIds.addAll(adminUserIds());
        }
        return userIds;
    }

    /** 解析上下文 */
    public static class Ctx
    {
        /** 发起人用户ID */
        private String initiator;

        /** 上一活动（已完成任务）的办理人ID，首个审批节点回退为发起人 */
        private String prevAssignee;

        /** 表单变量（用于 formField 类型的参与人） */
        private Map<String, Object> variables = new HashMap<>();

        /** 流程实例ID（用于「流程节点操作人」类型，按历史任务反查该节点办理人） */
        private String processInstanceId;

        /** 流程定义ID（用于判断引用的节点是否为「申请（开始）节点」） */
        private String processDefinitionId;

        public String getInitiator()
        {
            return initiator;
        }

        public void setInitiator(String initiator)
        {
            this.initiator = initiator;
        }

        public String getPrevAssignee()
        {
            return prevAssignee;
        }

        public void setPrevAssignee(String prevAssignee)
        {
            this.prevAssignee = prevAssignee;
        }

        public Map<String, Object> getVariables()
        {
            return variables;
        }

        public void setVariables(Map<String, Object> variables)
        {
            this.variables = variables == null ? new HashMap<>() : variables;
        }

        public String getProcessInstanceId()
        {
            return processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId)
        {
            this.processInstanceId = processInstanceId;
        }

        public String getProcessDefinitionId()
        {
            return processDefinitionId;
        }

        public void setProcessDefinitionId(String processDefinitionId)
        {
            this.processDefinitionId = processDefinitionId;
        }

        /** 活动类关系的作用人：优先上一办理人，其次发起人 */
        public String activityBase()
        {
            return !blank(prevAssignee) ? prevAssignee : initiator;
        }
    }

    /** 解析结果 */
    public static class Result
    {
        /** 解析出的用户ID（保持配置顺序并去重） */
        private final LinkedHashSet<String> userIds = new LinkedHashSet<>();

        /** 未能解析的配置项名称（分管领导、表达式等），用于发起前提示 */
        private final List<String> unresolved = new ArrayList<>();

        /** 是否走了「无人可办」兜底（兜底办理人 / 流程管理员） */
        private boolean failover = false;

        public LinkedHashSet<String> getUserIds()
        {
            return userIds;
        }

        public List<String> getUnresolved()
        {
            return unresolved;
        }

        public boolean isFailover()
        {
            return failover;
        }

        public void setFailover(boolean failover)
        {
            this.failover = failover;
        }

        public boolean isEmpty()
        {
            return userIds.isEmpty();
        }
    }

    /**
     * 读取节点参与者配置（flowable:participantConfig）
     *
     * @return 未配置时返回 null
     */
    public JSONObject readConfig(UserTask task)
    {
        return readJson(task, "participantConfig");
    }

    /**
     * 读取节点上的 JSON 扩展属性（如 participantConfig / ccConfig）
     *
     * @return 未配置或解析失败时返回 null
     */
    public JSONObject readJson(UserTask task, String attrName)
    {
        String raw = readAttr(task, attrName);
        if (blank(raw))
        {
            return null;
        }
        try
        {
            return JSON.parseObject(raw);
        }
        catch (Exception e)
        {
            log.warn("节点 {} 的 {} 解析失败：{}", task.getId(), attrName, e.getMessage());
            return null;
        }
    }

    /** 读取节点扩展属性值 */
    public String readAttr(UserTask task, String name)
    {
        Map<String, List<ExtensionAttribute>> attrs = task.getAttributes();
        if (attrs == null)
        {
            return null;
        }
        List<ExtensionAttribute> list = attrs.get(name);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        return list.get(0).getValue();
    }

    /**
     * 按 participantConfig 解析节点参与者为具体用户ID
     *
     * 说明：BPMN 内置属性（assignee / candidateUsers / candidateGroups）由引擎自行生效，此处不重复处理。
     */
    public Result resolve(UserTask task, Ctx ctx)
    {
        if (task == null)
        {
            return new Result();
        }
        JSONObject cfg = readConfig(task);
        JSONArray participants = cfg == null ? null : cfg.getJSONArray("participants");
        return resolveTargets(participants, ctx, task.getId());
    }

    /**
     * 解析参与者，解析不到人时按「兜底办理人 → 流程管理员」回退
     *
     * 会签/串签的多实例节点如果拿到空集合会被引擎直接跳过（等于漏审），
     * 因此兜底不是可选优化，而是必须的保险：宁可交给管理员，也不能让节点消失。
     *
     * @param task 用户任务
     * @param ctx  解析上下文
     * @return 解析结果，兜底时 {@link Result#isFailover()} 为 true
     */
    public Result resolveWithFallback(UserTask task, Ctx ctx)
    {
        Result result = resolve(task, ctx);
        if (!result.isEmpty())
        {
            return result;
        }
        JSONObject cfg = task == null ? null : readConfig(task);
        Result fallback = new Result();
        JSONArray fallbackIds = cfg == null ? null : cfg.getJSONArray("fallbackUserIds");
        if (fallbackIds != null)
        {
            for (int i = 0; i < fallbackIds.size(); i++)
            {
                addUser(fallbackIds.getString(i), fallback, "兜底办理人");
            }
        }
        if (fallback.isEmpty())
        {
            // 未配置兜底人：转流程管理员（RuoYi 内置 admin 角色 role_id=1）
            for (String admin : adminUserIds())
            {
                fallback.getUserIds().add(admin);
            }
        }
        if (!fallback.isEmpty())
        {
            result.getUserIds().addAll(fallback.getUserIds());
            result.setFailover(true);
            log.warn("节点 {}（{}）参与者解析为空，已按兜底转交 {}：{}",
                    task == null ? "-" : task.getId(), task == null ? "-" : task.getName(),
                    fallback.getUserIds(), result.getUnresolved());
        }
        else
        {
            log.error("节点 {}（{}）参与者解析为空且无可用兜底人（无兜底配置、无管理员账号）：{}",
                    task == null ? "-" : task.getId(), task == null ? "-" : task.getName(), result.getUnresolved());
        }
        return result;
    }

    /**
     * 流程管理员（RuoYi 内置 admin 角色 role_id=1 的用户）
     *
     * 兜底链的最后一级：既没配兜底人、又解析不出办理人时转给他们，
     * 否则会签节点的空集合会让引擎直接跳过整个审批环节。
     */
    public List<String> adminUserIds()
    {
        return many("select u.user_id from sys_user u "
                + "join sys_user_role ur on ur.user_id = u.user_id "
                + "where ur.role_id = 1 and u.del_flag = '0' and u.status = '0'");
    }

    /**
     * 读取上一个环节提交时指定的办理人（取人方式=提交时指定）
     *
     * 取变量优先级：nextAssignee_节点ID → nextAssignee；值支持数组与逗号分隔字符串。
     * 只在候选范围内才采纳，避免上一环节把人随便指到范围外。
     *
     * @param nodeId  目标节点ID
     * @param ctx     解析上下文
     * @param allowed 系统解析出的候选范围
     */
    public List<String> readDesignated(String nodeId, Ctx ctx, Collection<String> allowed)
    {
        List<String> result = new ArrayList<>();
        if (ctx == null || ctx.getVariables() == null)
        {
            return result;
        }
        Object value = null;
        if (!blank(nodeId))
        {
            value = ctx.getVariables().get("nextAssignee_" + nodeId);
        }
        if (value == null)
        {
            value = ctx.getVariables().get("nextAssignee");
        }
        if (value == null)
        {
            return result;
        }
        List<String> picked = new ArrayList<>();
        if (value instanceof Collection)
        {
            for (Object o : (Collection<?>) value)
            {
                if (o != null)
                {
                    picked.add(String.valueOf(o).trim());
                }
            }
        }
        else
        {
            for (String t : String.valueOf(value).split("[,，、;；]"))
            {
                picked.add(t.trim());
            }
        }
        for (String id : picked)
        {
            if (blank(id))
            {
                continue;
            }
            if (allowed == null || allowed.isEmpty() || allowed.contains(id))
            {
                result.add(id);
            }
            else
            {
                log.warn("提交时指定的办理人 {} 不在节点 {} 的候选范围内，已忽略", id, nodeId);
            }
        }
        return result;
    }

    /**
     * 解析一组参与者配置为具体用户ID（供抄送等非办理人场景复用）
     *
     * @param participants 参与者配置数组（结构同 participantConfig.participants）
     * @param ctx          解析上下文
     * @param owner        归属标识（节点/任务ID，仅用于日志）
     */
    public Result resolveTargets(JSONArray participants, Ctx ctx, String owner)
    {
        Result result = new Result();
        if (participants == null || participants.isEmpty() || ctx == null)
        {
            return result;
        }
        for (int i = 0; i < participants.size(); i++)
        {
            JSONObject p = participants.getJSONObject(i);
            if (p == null)
            {
                continue;
            }
            try
            {
                resolveOne(p, ctx, result);
            }
            catch (Exception e)
            {
                log.warn("{} 参与者 {} 解析异常：{}", owner, p.getString("name"), e.getMessage());
                result.getUnresolved().add(String.valueOf(p.getString("name")));
            }
        }
        return result;
    }

    /**
     * 预览用：解析参与者，并兼容仅写了 BPMN 内置属性的旧模型
     */
    public Result describe(UserTask task, Ctx ctx)
    {
        Result result = resolve(task, ctx);
        JSONObject cfg = readConfig(task);
        JSONArray participants = cfg == null ? null : cfg.getJSONArray("participants");
        boolean hasConfig = participants != null && !participants.isEmpty();
        if (hasConfig)
        {
            return result;
        }
        // 旧模型：flowable:assignee / candidateUsers / candidateGroups
        String assignee = task.getAssignee();
        if (!blank(assignee))
        {
            if (assignee.contains("${"))
            {
                if (assignee.contains("initiator"))
                {
                    addUser(ctx.getInitiator(), result, "流程发起人");
                }
                else
                {
                    result.getUnresolved().add("表达式 " + assignee);
                }
            }
            else
            {
                addUser(assignee, result, "指定办理人");
            }
        }
        if (result.isEmpty() && task.getCandidateUsers() != null)
        {
            for (String u : task.getCandidateUsers())
            {
                addUser(u, result, "候选用户");
            }
        }
        if (result.isEmpty() && task.getCandidateGroups() != null)
        {
            for (String g : task.getCandidateGroups())
            {
                addAll(usersByRole(g), result, "候选角色 " + g);
            }
        }
        // 操作组（flowable:operatorGroups）由 OperatorGroupTaskListener 在任务创建时按组织关系指派
        if (result.isEmpty() && result.getUnresolved().isEmpty() && !blank(readAttr(task, "operatorGroups")))
        {
            result.getUnresolved().add("操作组（提交后按组织关系自动指派）");
        }
        return result;
    }

    /** 用户信息（用于发起前预览展示） */
    public List<Map<String, Object>> usersInfo(Collection<String> userIds)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        if (userIds == null || userIds.isEmpty())
        {
            return list;
        }
        List<Long> ids = new ArrayList<>();
        for (String id : userIds)
        {
            if (isNumeric(id))
            {
                ids.add(Long.parseLong(id));
            }
        }
        if (ids.isEmpty())
        {
            return list;
        }
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < ids.size(); i++)
        {
            in.append(i == 0 ? "?" : ",?");
        }
        try
        {
            list = jdbc().query("select u.user_id, u.user_name, u.nick_name, ifnull(d.dept_name, '') dept_name "
                    + "from sys_user u left join sys_dept d on d.dept_id = u.dept_id "
                    + "where u.user_id in (" + in + ")", (rs, i) ->
                    {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("userId", rs.getString("user_id"));
                        m.put("userName", rs.getString("nick_name"));
                        m.put("loginName", rs.getString("user_name"));
                        m.put("deptName", rs.getString("dept_name"));
                        return m;
                    }, ids.toArray());
            // 按传入顺序排序，保证前端展示稳定
            List<Map<String, Object>> ordered = new ArrayList<>();
            for (String id : userIds)
            {
                for (Map<String, Object> m : list)
                {
                    if (id.equals(String.valueOf(m.get("userId"))))
                    {
                        ordered.add(m);
                    }
                }
            }
            return ordered;
        }
        catch (Exception e)
        {
            log.warn("查询用户信息失败：{}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 用户ID → 姓名（忽略取不到的用户，保持入参顺序）
     */
    public Map<String, String> userNames(Collection<String> userIds)
    {
        Map<String, String> map = new LinkedHashMap<>();
        for (Map<String, Object> m : usersInfo(userIds))
        {
            Object id = m.get("userId");
            Object name = m.get("userName");
            if (id != null)
            {
                map.put(String.valueOf(id), name == null ? "" : String.valueOf(name));
            }
        }
        return map;
    }

    // ==================== 参与者单项解析 ====================

    /**
     * 参与者单项解析
     *
     * 配置结构（前端「设置操作人员」弹窗产出）：
     * {
     *   type: 'user|dept|deptLeader|deptChargeLeader|role|post|nodeUser|nodeUserLeader
     *         |initiator|activityUser
     *         |fieldUser|fieldUserLeader|fieldDeptLeader|fieldDeptChargeLeader',
     *   ids: ['1','2'],        // 固定对象（人员/部门/角色/岗位）
     *   id: '1',               // 兼容旧配置的单值写法
     *   nodeId: 'sid-xxx',     // nodeUser / nodeUserLeader 引用的节点
     *   field: 'applyUser',    // field* 类型的表单字段
     *   fieldType: 'user|dept',// 字段值类型（人员字段 / 部门字段），缺省时自动判定
     *   includeChild: false,   // 指定部门默认只取本部门，显式 true 才含下级
     *   mode/type 旧结构（activity / org / formField）继续兼容
     * }
     */
    private void resolveOne(JSONObject p, Ctx ctx, Result result)
    {
        String type = p.getString("type");
        String label = p.getString("name");
        if (blank(type))
        {
            result.getUnresolved().add("未设置关系");
            return;
        }
        // 旧结构 type=field 的表单字段
        if ("field".equals(type) || "formField".equals(p.getString("mode")))
        {
            resolveField(p, ctx, label, "user", result);
            return;
        }
        // 旧结构：活动关系（initiator / activityUser / prevAssignee* / currentUserLeader）
        if (isActivityType(type))
        {
            resolveActivity(type, ctx, label, result);
            return;
        }
        List<String> ids = readIds(p);
        // 指定部门 = 该部门本身的人员；只有显式配置 includeChild=true 才含下级部门
        boolean includeChild = Boolean.TRUE.equals(p.getBoolean("includeChild"));
        if (ids.isEmpty() && !type.startsWith("field") && !type.startsWith("node"))
        {
            result.getUnresolved().add(blank(label) ? type : label);
            return;
        }
        switch (type)
        {
            // ===== 通用·固定对象 =====
            case "user":
                for (String id : ids)
                {
                    addUser(id, result, label);
                }
                break;
            case "dept":
                for (String id : ids)
                {
                    addAll(usersByDept(id, includeChild), result, label);
                }
                break;
            case "role":
                for (String id : ids)
                {
                    addAll(usersByRole(id), result, label);
                }
                break;
            case "post":
                for (String id : ids)
                {
                    addAll(usersByPost(id), result, label);
                }
                break;
            case "deptLeader":
            case "deptOrgLeader":
            case "deptParentLeader":
                // 指定部门的领导 = 该部门 sys_dept.leader（取不到向上回溯）；
                // deptParentLeader / deptOrgLeader 为历史命名，语义统一为「部门领导」
                for (String id : ids)
                {
                    addAll(leadersClimb(id), result, label);
                }
                break;
            case "deptChargeLeader":
                // 指定部门的分管领导 = sys_dept.charge_leader_id，缺失时向上回溯
                for (String id : ids)
                {
                    addAll(chargeLeadersClimb(id), result, label);
                }
                break;
            case "userLeader":
                for (String id : ids)
                {
                    addAll(leadersClimb(parentDept(userDept(id))), result, label);
                }
                break;
            case "userOrgLeader":
                for (String id : ids)
                {
                    addAll(leadersClimb(userDept(id)), result, label);
                }
                break;
            case "userChargeLeader":
                for (String id : ids)
                {
                    addAll(chargeLeadersClimb(userDept(id)), result, label);
                }
                break;
            // ===== 通用·流程节点操作人 =====
            case "nodeUser":
                addAll(nodeAssignees(p.getString("nodeId"), ctx), result, label);
                break;
            case "nodeUserLeader":
                for (String assignee : nodeAssignees(p.getString("nodeId"), ctx))
                {
                    addAll(leadersClimb(parentDept(userDept(assignee))), result, label);
                }
                break;
            // ===== 表单字段中的组织或用户 =====
            case "fieldUser":
                resolveField(p, ctx, label, "user", result);
                break;
            case "fieldUserLeader":
                resolveFieldLeader(p, ctx, label, false, result);
                break;
            case "fieldDeptLeader":
                resolveFieldLeader(p, ctx, label, false, result);
                break;
            case "fieldDeptParentLeader":
                resolveFieldLeader(p, ctx, label, false, result);
                break;
            case "fieldDeptChargeLeader":
                resolveFieldLeader(p, ctx, label, true, result);
                break;
            default:
                result.getUnresolved().add(blank(label) ? type : label);
                break;
        }
    }

    /** 读取配置中的对象ID列表：优先 ids 数组，兼容旧的单值 id */
    private List<String> readIds(JSONObject p)
    {
        List<String> list = new ArrayList<>();
        JSONArray ids = p.getJSONArray("ids");
        if (ids != null)
        {
            for (int i = 0; i < ids.size(); i++)
            {
                String v = ids.getString(i);
                if (!blank(v))
                {
                    list.add(v.trim());
                }
            }
        }
        if (list.isEmpty())
        {
            String id = p.getString("id");
            if (!blank(id))
            {
                for (String t : id.split("[,，]"))
                {
                    if (!blank(t))
                    {
                        list.add(t.trim());
                    }
                }
            }
        }
        return list;
    }

    /** 是否旧版「活动关系」类型 */
    private boolean isActivityType(String type)
    {
        switch (type)
        {
            case "initiator":
            case "initiatorDeptAndBelow":
            case "activityUser":
            case "activityUserLeader":
            case "activityUserOrgLeader":
            case "activityUserChargeLeader":
            case "prevAssigneeDeptAndBelow":
            case "prevAssigneeLeader":
            case "currentUserLeader":
                return true;
            default:
                return false;
        }
    }

    /** 表单字段 → 人员：按字段值类型解析（人员字段取值 / 部门字段取人） */
    private void resolveField(JSONObject p, Ctx ctx, String label, String fallbackType, Result result)
    {
        String field = p.getString("field");
        Object value = field == null ? null : ctx.getVariables().get(field);
        String fieldType = p.getString("fieldType");
        if (blank(fieldType))
        {
            fieldType = fallbackType;
        }
        List<String> userIds;
        if ("dept".equals(fieldType))
        {
            // 部门字段：取所选部门下的人员（本部门，不含下级）
            userIds = new ArrayList<>();
            for (String deptId : resolveDeptTokens(value))
            {
                userIds.addAll(usersByDept(deptId, false));
            }
        }
        else
        {
            userIds = resolveUserTokens(value);
        }
        if (userIds.isEmpty())
        {
            result.getUnresolved().add(blank(label) ? String.valueOf(field) : label);
        }
        else
        {
            addAll(userIds, result, null);
        }
    }

    /** 表单字段 → 领导：人员字段取其所在部门的领导，部门字段取该部门的领导 / 分管领导 */
    private void resolveFieldLeader(JSONObject p, Ctx ctx, String label, boolean charge, Result result)
    {
        String field = p.getString("field");
        Object value = field == null ? null : ctx.getVariables().get(field);
        String fieldType = p.getString("fieldType");
        List<String> leaders = new ArrayList<>();
        // 部门字段优先按部门解析，人员字段按人员→所属部门解析
        List<String> deptIds = resolveDeptTokens(value);
        boolean isDeptValue = "dept".equals(fieldType) || (!"user".equals(fieldType) && !deptIds.isEmpty());
        if (isDeptValue)
        {
            for (String deptId : deptIds)
            {
                leaders.addAll(charge ? chargeLeadersClimb(deptId) : leadersClimb(deptId));
            }
        }
        else
        {
            for (String userId : resolveUserTokens(value))
            {
                leaders.addAll(charge ? chargeLeadersClimb(userDept(userId))
                        : leadersClimb(parentDept(userDept(userId))));
            }
        }
        if (leaders.isEmpty())
        {
            result.getUnresolved().add(blank(label) ? String.valueOf(field) : label);
        }
        else
        {
            addAll(leaders, result, null);
        }
    }

    /** 活动关系：以发起人 / 上一活动办理人 / 当前办理人为锚点 */
    private void resolveActivity(String type, Ctx ctx, String label, Result result)
    {
        String base = ctx.activityBase();
        if (blank(type))
        {
            result.getUnresolved().add(blank(label) ? "未设置关系" : label);
            return;
        }
        switch (type)
        {
            case "initiator":
                addUser(ctx.getInitiator(), result, label);
                break;
            case "initiatorDeptAndBelow":
                addAll(usersByDeptTree(userDept(ctx.getInitiator())), result, label);
                break;
            case "activityUser":
                addUser(base, result, label);
                break;
            case "activityUserOrgLeader":
                addAll(leadersClimb(userDept(base)), result, label);
                break;
            case "activityUserLeader":
            case "currentUserLeader":
                addAll(leadersClimb(parentDept(userDept(base))), result, label);
                break;
            case "prevAssigneeDeptAndBelow":
                addAll(usersByDeptTree(userDept(base)), result, label);
                break;
            case "prevAssigneeLeader":
                addAll(leadersClimb(parentDept(userDept(base))), result, label);
                break;
            case "activityUserChargeLeader":
                addAll(chargeLeadersClimb(userDept(base)), result, label);
                break;
            default:
                result.getUnresolved().add(blank(label) ? type : label);
                break;
        }
    }

    // ==================== 解析工具 ====================

    private void addUser(String userId, Result result, String label)
    {
        if (blank(userId))
        {
            result.getUnresolved().add(blank(label) ? "空用户" : label);
            return;
        }
        String v = userId.trim();
        if (isNumeric(v) && !usersBy("user_id", v).isEmpty())
        {
            result.getUserIds().add(v);
            return;
        }
        // 兼容配置里写的是登录名 / 姓名
        List<String> byName = many("select user_id from sys_user where del_flag = '0' and status = '0' "
                + "and (user_name = ? or nick_name = ?)", v, v);
        if (byName.isEmpty())
        {
            result.getUnresolved().add(blank(label) ? v : label);
        }
        else
        {
            result.getUserIds().addAll(byName);
        }
    }

    private void addAll(Collection<String> userIds, Result result, String label)
    {
        if (userIds == null || userIds.isEmpty())
        {
            result.getUnresolved().add(blank(label) ? "未匹配到人员" : label);
            return;
        }
        result.getUserIds().addAll(userIds);
    }

    /** 部门（含子孙部门）下的有效用户 */
    private List<String> usersByDeptTree(String deptId)
    {
        return usersByDept(deptId, true);
    }

    /**
     * 部门下的有效用户
     *
     * @param deptId       部门ID
     * @param includeChild 是否包含下级部门（false 时只取本部门直属人员）
     */
    private List<String> usersByDept(String deptId, boolean includeChild)
    {
        if (blank(deptId) || !isNumeric(deptId))
        {
            return new ArrayList<>();
        }
        if (!includeChild)
        {
            return many("select user_id from sys_user where del_flag = '0' and status = '0' and dept_id = ?", deptId);
        }
        return many("select user_id from sys_user where del_flag = '0' and status = '0' and dept_id in "
                + "(select dept_id from sys_dept where dept_id = ? or find_in_set(?, ancestors))", deptId, deptId);
    }

    /** 角色下的有效用户 */
    private List<String> usersByRole(String roleId)
    {
        if (blank(roleId) || !isNumeric(roleId))
        {
            return new ArrayList<>();
        }
        return many("select ur.user_id from sys_user_role ur join sys_user u on u.user_id = ur.user_id "
                + "where ur.role_id = ? and u.del_flag = '0' and u.status = '0'", roleId);
    }

    /** 岗位下的有效用户（sys_post + sys_user_post） */
    private List<String> usersByPost(String postId)
    {
        if (blank(postId))
        {
            return new ArrayList<>();
        }
        if (isNumeric(postId))
        {
            return many("select up.user_id from sys_user_post up join sys_user u on u.user_id = up.user_id "
                    + "where up.post_id = ? and u.del_flag = '0' and u.status = '0'", postId);
        }
        // 兼容配置里写的是岗位编码
        return many("select up.user_id from sys_user_post up join sys_user u on u.user_id = up.user_id "
                + "join sys_post p on p.post_id = up.post_id "
                + "where p.post_code = ? and u.del_flag = '0' and u.status = '0'", postId);
    }

    /**
     * 部门分管领导（sys_dept.charge_leader_id，逐级向上回溯到有分管领导的部门）
     */
    private List<String> chargeLeadersClimb(String deptId)
    {
        String cur = deptId;
        for (int i = 0; i < MAX_DEPT_CLIMB && !blank(cur); i++)
        {
            List<String> leaders = new ArrayList<>();
            String chargeLeaderId = one("select charge_leader_id from sys_dept where dept_id = ? and del_flag = '0'", cur);
            if (!blank(chargeLeaderId) && !"0".equals(chargeLeaderId))
            {
                for (String id : usersBy("user_id", chargeLeaderId))
                {
                    leaders.add(id);
                }
            }
            if (!leaders.isEmpty())
            {
                return leaders;
            }
            cur = parentDept(cur);
        }
        return new ArrayList<>();
    }

    /**
     * 「流程节点操作人」：取指定节点最近一次已完成任务的办理人
     *
     * 说明：
     * - 引用「申请（开始）节点」时，它没有任务记录，按流程发起人解析（等价于「发起人」关系，
     *   但可在同一处配成「申请节点的上级领导」等衍生关系）；
     * - 预览（流程尚未发起）时无实例，非申请节点返回空由调用方标记为未解析。
     */
    private List<String> nodeAssignees(String nodeId, Ctx ctx)
    {
        List<String> result = new ArrayList<>();
        if (blank(nodeId) || ctx == null)
        {
            return result;
        }
        if (!blank(ctx.getProcessInstanceId()))
        {
            try
            {
                List<String> list = many("select assignee_ from act_hi_taskinst where proc_inst_id_ = ? and task_def_key_ = ? "
                        + "and assignee_ is not null and assignee_ <> '' order by end_time_ desc limit 1",
                        ctx.getProcessInstanceId(), nodeId);
                for (String assignee : list)
                {
                    // 会签节点历史里可能是逗号分隔的多人
                    for (String t : String.valueOf(assignee).split("[,，]"))
                    {
                        if (!blank(t))
                        {
                            result.add(t.trim());
                        }
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("查询节点 {} 操作人失败：{}", nodeId, e.getMessage());
            }
        }
        if (result.isEmpty() && isStartNode(nodeId, ctx))
        {
            // 申请（开始）节点没有任务记录，其「操作人」就是流程发起人
            String initiator = !blank(ctx.getInitiator()) ? ctx.getInitiator() : ctx.getPrevAssignee();
            if (!blank(initiator))
            {
                result.add(initiator);
            }
        }
        return result;
    }

    /** 节点是否为当前流程的「申请（开始）节点」 */
    private boolean isStartNode(String nodeId, Ctx ctx)
    {
        try
        {
            String defId = ctx.getProcessDefinitionId();
            if (blank(defId) && !blank(ctx.getProcessInstanceId()))
            {
                defId = one("select proc_def_id_ from act_hi_procinst where proc_inst_id_ = ?", ctx.getProcessInstanceId());
                if (blank(defId))
                {
                    defId = one("select proc_def_id_ from act_ru_execution where proc_inst_id_ = ? and parent_id_ is null limit 1",
                            ctx.getProcessInstanceId());
                }
                ctx.setProcessDefinitionId(defId);
            }
            if (blank(defId))
            {
                return false;
            }
            BpmnModel model = SpringUtils.getBean(RepositoryService.class).getBpmnModel(defId);
            if (model == null || model.getMainProcess() == null)
            {
                return false;
            }
            FlowElement element = model.getMainProcess().getFlowElement(nodeId, true);
            return element instanceof StartEvent;
        }
        catch (Exception e)
        {
            log.warn("判断节点 {} 是否为申请节点失败：{}", nodeId, e.getMessage());
            return false;
        }
    }

    private List<String> usersBy(String column, String value)
    {
        return many("select user_id from sys_user where del_flag = '0' and status = '0' and " + column + " = ?", value);
    }

    /** 用户所属部门ID */
    private String userDept(String userId)
    {
        if (blank(userId) || !isNumeric(userId))
        {
            return null;
        }
        return one("select dept_id from sys_user where user_id = ?", userId);
    }

    /** 父部门ID（无上级时返回 null） */
    private String parentDept(String deptId)
    {
        if (blank(deptId) || !isNumeric(deptId))
        {
            return null;
        }
        String parentId = one("select parent_id from sys_dept where dept_id = ?", deptId);
        if (blank(parentId) || "0".equals(parentId) || parentId.equals(deptId))
        {
            return null;
        }
        return parentId;
    }

    /**
     * 组织负责人（逐级向上回溯，找到即返回）
     * sys_dept.leader 存姓名，按 nick_name / user_name 反查用户
     */
    private List<String> leadersClimb(String deptId)
    {
        String cur = deptId;
        for (int i = 0; i < MAX_DEPT_CLIMB && !blank(cur); i++)
        {
            List<String> leaders = leadersOfDept(cur);
            if (!leaders.isEmpty())
            {
                return leaders;
            }
            cur = parentDept(cur);
        }
        return new ArrayList<>();
    }

    private List<String> leadersOfDept(String deptId)
    {
        List<String> result = new ArrayList<>();
        String leader = one("select leader from sys_dept where dept_id = ? and del_flag = '0'", deptId);
        if (blank(leader))
        {
            return result;
        }
        for (String token : leader.split("[,，、;；/\\s]+"))
        {
            String v = token.trim();
            if (v.isEmpty())
            {
                continue;
            }
            if (isNumeric(v) && !usersBy("user_id", v).isEmpty())
            {
                result.add(v);
            }
            else
            {
                result.addAll(many("select user_id from sys_user where del_flag = '0' and status = '0' "
                        + "and (nick_name = ? or user_name = ?)", v, v));
            }
        }
        return result;
    }

    /** 表单字段值 → 用户ID（支持单值、逗号分隔、多选数组） */
    private List<String> resolveUserTokens(Object value)
    {
        List<String> result = new ArrayList<>();
        if (value == null)
        {
            return result;
        }
        List<String> tokens = new ArrayList<>();
        if (value instanceof Collection)
        {
            for (Object o : (Collection<?>) value)
            {
                if (o != null)
                {
                    tokens.add(String.valueOf(o));
                }
            }
        }
        else
        {
            for (String t : String.valueOf(value).split("[,，、;；]"))
            {
                tokens.add(t);
            }
        }
        for (String token : tokens)
        {
            String v = token == null ? "" : token.trim();
            if (v.isEmpty())
            {
                continue;
            }
            if (isNumeric(v) && !usersBy("user_id", v).isEmpty())
            {
                result.add(v);
            }
            else
            {
                result.addAll(many("select user_id from sys_user where del_flag = '0' and status = '0' "
                        + "and (user_name = ? or nick_name = ?)", v, v));
            }
        }
        return result;
    }

    /** 表单字段值 → 部门ID（支持单值、逗号分隔、多选数组；数字按部门ID校验，文本按部门名称匹配） */
    private List<String> resolveDeptTokens(Object value)
    {
        List<String> result = new ArrayList<>();
        if (value == null)
        {
            return result;
        }
        List<String> tokens = new ArrayList<>();
        if (value instanceof Collection)
        {
            for (Object o : (Collection<?>) value)
            {
                if (o != null)
                {
                    tokens.add(String.valueOf(o));
                }
            }
        }
        else
        {
            for (String t : String.valueOf(value).split("[,，、;；]"))
            {
                tokens.add(t);
            }
        }
        for (String token : tokens)
        {
            String v = token == null ? "" : token.trim();
            if (v.isEmpty())
            {
                continue;
            }
            if (isNumeric(v) && one("select dept_id from sys_dept where dept_id = ? and del_flag = '0'", v) != null)
            {
                result.add(v);
            }
            else
            {
                result.addAll(many("select dept_id from sys_dept where dept_name = ? and del_flag = '0'", v));
            }
        }
        return result;
    }

    // ==================== JDBC 封装 ====================

    /** 惰性获取 JdbcTemplate（引擎监听器在引擎初始化期执行，避免容器启动期的注入顺序问题） */
    private JdbcTemplate jdbc()
    {
        return SpringUtils.getBean(JdbcTemplate.class);
    }

    private String one(String sql, Object... args)
    {
        List<String> list = many(sql, args);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<String> many(String sql, Object... args)
    {
        try
        {
            return jdbc().query(sql, (rs, i) -> rs.getString(1), args);
        }
        catch (Exception e)
        {
            log.warn("参与者解析查询失败：{}（{}）", e.getMessage(), sql);
            return new ArrayList<>();
        }
    }

    private static boolean isNumeric(String v)
    {
        return v != null && v.matches("\\d+");
    }

    private static boolean blank(String v)
    {
        return v == null || v.trim().isEmpty();
    }
}
