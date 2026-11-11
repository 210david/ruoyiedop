package com.ruoyi.flowable.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
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
import com.ruoyi.flowable.config.CurrentNodeProcessDiagramGenerator;
import org.flowable.engine.ProcessEngine;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.api.TaskQuery;
import org.flowable.engine.task.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.flowable.config.FlowReturnConstants;
import com.ruoyi.flowable.config.FlowSystemFields;
import com.ruoyi.flowable.domain.FlowInstance;
import com.ruoyi.flowable.domain.FlowTask;
import com.ruoyi.flowable.domain.dto.FlowCompleteDTO;
import com.ruoyi.flowable.domain.dto.FlowStartDTO;
import com.ruoyi.flowable.domain.FlowProcdefExt;
import com.ruoyi.flowable.mapper.FlowProcdefExtMapper;
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
    private static final Logger log = LoggerFactory.getLogger(FlowProcessServiceImpl.class);

    /** 发起节点签字意见的批注前缀（发起环节没有任务，用流程级批注存） */
    private static final String START_COMMENT_PREFIX = "【start】";

    /** 管理员强制结束流程的删除原因前缀（前端据此区分「强制结束」与「撤销」） */
    private static final String ADMIN_END_PREFIX = "【adminEnd】";

    /** 管理员撤销流程的删除原因前缀 */
    private static final String ADMIN_CANCEL_PREFIX = "【adminCancel】";

    /** 管理员干预（节点跳转）的批注前缀 */
    private static final String ADMIN_INTERVENE_PREFIX = "【intervene】";

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

    @Autowired
    private FlowProcdefExtMapper flowProcdefExtMapper;

    @Autowired
    private com.ruoyi.system.service.ISysUserService sysUserService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private com.ruoyi.flowable.service.IFlowFormService flowFormService;

    /** 流程图「已查看人员」记录（泛微式：谁打开过该节点但还没办理） */
    @Autowired
    private com.ruoyi.flowable.service.FlowNodeViewService flowNodeViewService;

    /** 审批意见类型标识 */

    @Override
    @Transactional
    public String startProcess(FlowStartDTO dto)
    {
        // 发起路由：优先取活动版本（用户指定的当前启用版本），未设置或已挂起时回退"启用中的最新版本"
        ProcessDefinition definition = null;
        List<FlowProcdefExt> extList = flowProcdefExtMapper.selectFlowProcdefExtByProcKey(dto.getProcessDefinitionKey());
        FlowProcdefExt activeExt = (extList == null) ? null
                : extList.stream().filter(e -> "1".equals(e.getActiveFlag())).findFirst().orElse(null);
        if (activeExt != null && "1".equals(activeExt.getActiveFlag()))
        {
            ProcessDefinition activeDef = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(activeExt.getProcdefId()).singleResult();
            if (activeDef != null && !activeDef.isSuspended())
            {
                definition = activeDef;
            }
        }
        if (definition == null)
        {
            definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(dto.getProcessDefinitionKey())
                    .orderByProcessDefinitionVersion().desc()
                    .list().stream()
                    .filter(d -> !d.isSuspended())
                    .findFirst().orElse(null);
        }
        if (definition == null)
        {
            throw new ServiceException("流程定义不存在、未部署或所有版本均已停用：" + dto.getProcessDefinitionKey());
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
            saveStartComment(instance.getId(), variables);
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
        else if ("canceled".equals(query.getStatus()))
        {
            // 已撤销=已结束且带删除原因，Flowable 原生查询不支持，内存过滤
            hisQuery.finished();
        }
        // 流程标题内存过滤（实例名称 Flowable 历史查询不支持）
        List<HistoricProcessInstance> instances = hisQuery.list();
        List<FlowInstance> list = new ArrayList<>();
        for (HistoricProcessInstance hpi : instances)
        {
            FlowInstance fi = convertInstance(hpi);
            if (!matchInstanceFilter(fi, query))
            {
                continue;
            }
            list.add(fi);
        }
        return list;
    }

    /** 流程实例列表内存过滤：流程标题/发起人/发起时间区间/已撤销状态（Flowable 原生查询不支持的条件） */
    private boolean matchInstanceFilter(FlowInstance fi, FlowInstance query)
    {
        if ("canceled".equals(query.getStatus())
                && !"canceled".equals(fi.getStatus()))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(query.getTitle())
                && (fi.getTitle() == null || !fi.getTitle().contains(query.getTitle())))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(query.getStartUserName())
                && (fi.getStartUserName() == null || !fi.getStartUserName().contains(query.getStartUserName())))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(query.getCurrentActivityName())
                && (fi.getCurrentActivityName() == null || !fi.getCurrentActivityName().contains(query.getCurrentActivityName())))
        {
            return false;
        }
        java.util.Map<String, Object> params = query.getParams();
        if (params != null)
        {
            String begin = (String) params.get("beginTime");
            String end = (String) params.get("endTime");
            try
            {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                if (StringUtils.isNotEmpty(begin) && fi.getStartTime() != null
                        && fi.getStartTime().before(sdf.parse(begin)))
                {
                    return false;
                }
                if (StringUtils.isNotEmpty(end) && fi.getStartTime() != null)
                {
                    java.util.Date endDate = sdf.parse(end);
                    // 结束日期含当天（23:59:59）
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTime(endDate);
                    c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                    if (fi.getStartTime().after(c.getTime()))
                    {
                        return false;
                    }
                }
            }
            catch (java.text.ParseException ignored) { }
        }
        return true;
    }

    @Override
    public List<FlowInstance> selectInvolvedProcessList(FlowInstance query)
    {
        Long userId = SecurityUtils.getUserId();
        HistoricProcessInstanceQuery hisQuery = historyService.createHistoricProcessInstanceQuery()
                .involvedUser(String.valueOf(userId))
                .orderByProcessInstanceStartTime().desc();
        if (StringUtils.isNotEmpty(query.getProcessDefinitionName()))
        {
            hisQuery.processDefinitionNameLike("%" + query.getProcessDefinitionName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionKey()))
        {
            hisQuery.processDefinitionKey(query.getProcessDefinitionKey());
        }
        if (StringUtils.isNotEmpty(query.getInstanceId()))
        {
            hisQuery.processInstanceId(query.getInstanceId());
        }
        if ("running".equals(query.getStatus()))
        {
            hisQuery.unfinished();
        }
        else if ("completed".equals(query.getStatus()) || "canceled".equals(query.getStatus()))
        {
            hisQuery.finished();
        }
        List<HistoricProcessInstance> instances = hisQuery.list();
        List<FlowInstance> list = new ArrayList<>();
        for (HistoricProcessInstance hpi : instances)
        {
            FlowInstance fi = convertInstance(hpi);
            if (!matchInstanceFilter(fi, query))
            {
                continue;
            }
            // 标注我是否为发起人
            fi.setStartUserId(userId.equals(fi.getStartUserId()) ? userId : fi.getStartUserId());
            list.add(fi);
        }
        return list;
    }

    @Override
    public FlowInstance selectInstanceDetail(String instanceId)
    {
        // 注意：HistoricProcessInstanceQuery 默认不装载流程变量，不加 includeProcessVariables() 时
        // hpi.getProcessVariables() 恒为空 Map —— 详情页/我的流程/已办/查询页的表单值全靠它，
        // 漏了这句就会出现"表单字段全空、明细表没有行"的现象（变量其实一直在 ACT_HI_VARINST 里）。
        HistoricProcessInstance hpi;
        try
        {
            hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).includeProcessVariables().singleResult();
        }
        catch (Exception e)
        {
            // 历史实例里若存在反序列化失败的变量（旧版 Serializable 值），退回不带变量的查询，
            // 保证详情页仍能打开，只是表单值显示为空
            hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
        }
        if (hpi == null)
        {
            throw new ServiceException("流程实例不存在");
        }
        FlowInstance fi = convertInstance(hpi);
        fi.setVariables(hpi.getProcessVariables() == null
                ? new java.util.HashMap<>() : hpi.getProcessVariables());
        return fi;
    }

    // ==================== 流程监控（系统管理员干预，对标泛微流程监控） ====================

    @Override
    public List<FlowInstance> selectMonitorProcessList(FlowInstance query)
    {
        HistoricProcessInstanceQuery hisQuery = historyService.createHistoricProcessInstanceQuery()
                .orderByProcessInstanceStartTime().desc();
        if (StringUtils.isNotEmpty(query.getProcessDefinitionName()))
        {
            hisQuery.processDefinitionNameLike("%" + query.getProcessDefinitionName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionKey()))
        {
            hisQuery.processDefinitionKey(query.getProcessDefinitionKey());
        }
        if (StringUtils.isNotEmpty(query.getInstanceId()))
        {
            hisQuery.processInstanceId(query.getInstanceId());
        }
        if ("running".equals(query.getStatus()))
        {
            hisQuery.unfinished();
        }
        else if ("completed".equals(query.getStatus()) || "canceled".equals(query.getStatus()))
        {
            hisQuery.finished();
        }
        List<HistoricProcessInstance> instances = hisQuery.list();
        List<FlowInstance> list = new ArrayList<>();
        for (HistoricProcessInstance hpi : instances)
        {
            FlowInstance fi = convertInstance(hpi);
            if (!matchInstanceFilter(fi, query))
            {
                continue;
            }
            list.add(fi);
        }
        return list;
    }

    @Override
    public Map<String, Object> countMonitor()
    {
        // 单次查询直接按结束时间/删除原因分组统计，避免逐条 convert 带来的 N 次查询
        long all = 0, running = 0, completed = 0, canceled = 0;
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().list();
        for (HistoricProcessInstance hpi : instances)
        {
            all++;
            if (hpi.getEndTime() == null)
            {
                running++;
            }
            else if (StringUtils.isNotEmpty(hpi.getDeleteReason()))
            {
                canceled++;
            }
            else
            {
                completed++;
            }
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("all", all);
        result.put("running", running);
        result.put("completed", completed);
        result.put("canceled", canceled);
        return result;
    }

    @Override
    public Map<String, Object> selectMonitorNodes(String instanceId)
    {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (hpi == null)
        {
            throw new ServiceException("流程实例不存在");
        }
        BpmnModel model = repositoryService.getBpmnModel(hpi.getProcessDefinitionId());
        if (model == null)
        {
            throw new ServiceException("流程定义不存在，无法干预");
        }
        // 当前停留节点（并行/会签时可能多个）
        List<String> activeIds = new ArrayList<>();
        List<String> activeNames = new ArrayList<>();
        if (hpi.getEndTime() == null)
        {
            for (String id : runtimeService.getActiveActivityIds(instanceId))
            {
                activeIds.add(id);
                FlowElement el = model.getFlowElement(id);
                activeNames.add(el == null || el.getName() == null ? id : el.getName());
            }
        }
        // 当前待办任务（含办理人）
        List<Map<String, Object>> currentTasks = new ArrayList<>();
        if (hpi.getEndTime() == null)
        {
            for (Task task : taskService.createTaskQuery().processInstanceId(instanceId)
                    .orderByTaskCreateTime().asc().list())
            {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("taskId", task.getId());
                m.put("taskName", task.getName());
                m.put("taskDefinitionKey", task.getTaskDefinitionKey());
                m.put("assignee", task.getAssignee());
                m.put("assigneeName", StringUtils.isEmpty(task.getAssignee()) ? null
                        : resolveUserNickname(task.getAssignee()));
                m.put("createTime", task.getCreateTime());
                currentTasks.add(m);
            }
        }
        // 可跳转节点：全部用户任务（递归含子流程内部节点）
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (UserTask ut : model.getMainProcess().findFlowElementsOfType(UserTask.class, true))
        {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", ut.getId());
            m.put("name", StringUtils.isNotEmpty(ut.getName()) ? ut.getName() : ut.getId());
            m.put("multiInstance", ut.getLoopCharacteristics() != null);
            m.put("current", activeIds.contains(ut.getId()));
            nodes.add(m);
        }
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("instanceId", instanceId);
        data.put("title", StringUtils.isNotEmpty(hpi.getName()) ? hpi.getName() : hpi.getProcessDefinitionName());
        data.put("processDefinitionName", hpi.getProcessDefinitionName());
        data.put("status", hpi.getEndTime() == null ? "running" : "finished");
        data.put("currentActivityIds", activeIds);
        data.put("currentActivityNames", activeNames);
        data.put("currentTasks", currentTasks);
        data.put("nodes", nodes);
        return data;
    }

    @Override
    @Transactional
    public int interveneInstance(String instanceId, String targetActivityId, Long assigneeId, String comment)
    {
        if (StringUtils.isEmpty(instanceId) || StringUtils.isEmpty(targetActivityId))
        {
            throw new ServiceException("流程实例与目标节点不能为空");
        }
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (instance == null)
        {
            throw new ServiceException("流程已结束，无法干预");
        }
        BpmnModel model = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
        if (model == null)
        {
            throw new ServiceException("流程定义不存在，无法干预");
        }
        FlowElement target = model.getFlowElement(targetActivityId);
        if (!(target instanceof UserTask))
        {
            throw new ServiceException("目标节点不是用户任务节点，无法干预");
        }
        // 当前活动节点：只保留用户任务（并行/会签可能多个），无用户任务时退回全部活动
        List<String> allActive = runtimeService.getActiveActivityIds(instanceId);
        List<String> fromIds = new ArrayList<>();
        for (String id : allActive)
        {
            if (model.getFlowElement(id) instanceof UserTask)
            {
                fromIds.add(id);
            }
        }
        if (fromIds.isEmpty())
        {
            fromIds.addAll(allActive);
        }
        if (fromIds.isEmpty())
        {
            throw new ServiceException("流程当前没有可干预的活动节点");
        }
        String targetName = StringUtils.isNotEmpty(target.getName()) ? target.getName() : targetActivityId;
        // 已在目标节点（且只有这一个活动）：只做出派/意见，不触发节点跳转
        boolean sameNode = fromIds.size() == 1 && fromIds.contains(targetActivityId);
        if (!sameNode)
        {
            try
            {
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(instanceId)
                        .moveActivityIdsToSingleActivityId(fromIds, targetActivityId)
                        .changeState();
            }
            catch (Exception e)
            {
                throw new ServiceException("流程干预失败：" + e.getMessage());
            }
        }
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(instanceId)
                .taskDefinitionKey(targetActivityId).list();
        if (assigneeId != null)
        {
            for (Task task : tasks)
            {
                taskService.setAssignee(task.getId(), String.valueOf(assigneeId));
            }
        }
        StringBuilder msg = new StringBuilder(ADMIN_INTERVENE_PREFIX)
                .append("管理员干预至「").append(targetName).append("」");
        if (assigneeId != null)
        {
            msg.append("，指定办理人「").append(resolveUserNickname(String.valueOf(assigneeId))).append("」");
        }
        if (StringUtils.isNotEmpty(comment))
        {
            msg.append("：").append(comment);
        }
        if (!tasks.isEmpty())
        {
            taskService.addComment(tasks.get(0).getId(), instanceId, "comment", "【intervene】" + msg.substring(ADMIN_INTERVENE_PREFIX.length()));
        }
        else
        {
            taskService.addComment(null, instanceId, "comment", msg.toString());
        }
        return 1;
    }

    /**
     * 强制收回（对标泛微）：把流程从当前节点回退到上一节点，由上一节点原办理人重新办理。
     *
     * <p>与「干预」的区别：干预可以跳到任意节点、不改变历史语义；收回是专门的一步回退，
     * 会自动定位上一节点、恢复原办理人。按泛微口径，收回属于管理员后台动作：
     * 不要求填写意见，也不在流程记录里留下批注，仅写入系统操作日志。</p>
     */
    @Override
    @Transactional
    public Map<String, Object> recallInstance(String instanceId, String reason)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            throw new ServiceException("流程实例标识不能为空");
        }
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (instance == null)
        {
            throw new ServiceException("流程已结束，无法收回");
        }
        BpmnModel model = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
        if (model == null)
        {
            throw new ServiceException("流程定义不存在，无法收回");
        }
        // 1) 当前停留节点（并行/会签可能有多个）
        List<String> fromIds = new ArrayList<>();
        StringBuilder fromName = new StringBuilder();
        List<Task> currentTasks = taskService.createTaskQuery().processInstanceId(instanceId)
                .orderByTaskCreateTime().asc().list();
        for (Task t : currentTasks)
        {
            String key = t.getTaskDefinitionKey();
            if (StringUtils.isEmpty(key) || fromIds.contains(key))
            {
                continue;
            }
            fromIds.add(key);
            if (fromName.length() > 0)
            {
                fromName.append("、");
            }
            fromName.append(StringUtils.isNotEmpty(t.getName()) ? t.getName() : key);
        }
        if (fromIds.isEmpty())
        {
            for (String id : runtimeService.getActiveActivityIds(instanceId))
            {
                if (model.getFlowElement(id) instanceof UserTask && !fromIds.contains(id))
                {
                    fromIds.add(id);
                }
            }
        }
        if (fromIds.isEmpty())
        {
            throw new ServiceException("流程当前没有待办节点，无法收回");
        }
        // 2) 上游节点：沿 BPMN 连线逆向回溯，得到所有可能的「上一节点」（由近到远）
        List<String> upstream = findUpstreamUserTasks(model, fromIds);
        if (upstream.isEmpty())
        {
            // 当前已是首个审批环节：能否收回取决于流程定义里有没有「发起人节点」
            // （部署时自动注入的 applyNode；早期部署的流程没有该节点，退回/收回都无处可去）
            String curName = fromName.length() > 0 ? fromName.toString() : fromIds.get(0);
            String tip = findInitiatorNode(model) == null
                    ? "该流程未生成「发起人节点」（重新部署流程模型后即可收回发起）"
                    : "上一环节是流程发起";
            throw new ServiceException("当前节点「" + curName + "」已是首个审批环节，" + tip + "，没有可收回的上一节点");
        }
        // 3) 优先取最近一次真正走过的上游节点：历史里已办结、且属于上游的用户任务
        String prevId = null;
        String prevAssignee = null;
        for (HistoricTaskInstance h : historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId).finished()
                .orderByHistoricTaskInstanceEndTime().desc().list())
        {
            String key = h.getTaskDefinitionKey();
            if (StringUtils.isEmpty(key) || fromIds.contains(key) || !upstream.contains(key))
            {
                continue;
            }
            prevId = key;
            prevAssignee = h.getAssignee();
            break;
        }
        // 4) 历史上没有（刚发起/走过网关）：取图上最邻近的上游用户任务
        if (prevId == null)
        {
            prevId = upstream.get(0);
        }
        UserTask prevTask = (UserTask) model.getFlowElement(prevId);
        // 回到发起人（申请）节点时，办理人取流程发起人
        if (StringUtils.isEmpty(prevAssignee) && isInitiatorNode(prevTask))
        {
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            prevAssignee = hpi == null ? null : hpi.getStartUserId();
        }
        String prevName = StringUtils.isNotEmpty(prevTask.getName()) ? prevTask.getName() : prevId;
        // 会签/串签节点不指定单人办理人，由引擎按参与者集合重新生成
        boolean multi = prevTask.getLoopCharacteristics() != null;
        java.util.Set<String> beforeIds = new java.util.HashSet<>();
        for (Task t : currentTasks)
        {
            beforeIds.add(t.getId());
        }
        // 5) 节点回退：按泛微口径，收回不写流转意见、不写退回标记、不动业务流程数据，
        //    流程记录里不额外留痕（仅系统操作日志记录本次管理动作）
        try
        {
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(instanceId)
                    .moveActivityIdsToSingleActivityId(fromIds, prevId)
                    .changeState();
        }
        catch (Exception e)
        {
            log.error("流程强制收回失败：instance={}, target={}, error={}", instanceId, prevId, e.getMessage(), e);
            throw new ServiceException("强制收回失败：" + e.getMessage());
        }
        // 6) 回到上一节点后，办理人恢复为该节点上次的实际办理人（会签/串签交给参与者集合重新生成）
        for (Task t : taskService.createTaskQuery().processInstanceId(instanceId).list())
        {
            if (beforeIds.contains(t.getId()))
            {
                continue;
            }
            if (!multi && StringUtils.isNotEmpty(prevAssignee))
            {
                taskService.setAssignee(t.getId(), prevAssignee);
            }
        }
        // 7) 清理收回前的办理痕迹：上一节点回到「未办理」，原来那条「同意 + 意见」不再显示
        clearRecalledTrace(instanceId, beforeIds, prevId, isInitiatorNode(prevTask));
        log.info("强制收回：instance={}，{} → {}，操作人 {}{}", instanceId, fromName, prevName,
                resolveUserNickname(String.valueOf(SecurityUtils.getUserId())),
                StringUtils.isEmpty(reason) ? "" : "，备注：" + reason);
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("fromActivityName", fromName.toString());
        data.put("prevActivityId", prevId);
        data.put("prevActivityName", prevName);
        data.put("prevAssignee", StringUtils.isEmpty(prevAssignee) ? null : prevAssignee);
        data.put("prevAssigneeName", StringUtils.isEmpty(prevAssignee) ? null : resolveUserNickname(prevAssignee));
        return data;
    }

    /**
     * 强制收回后清理办理痕迹。
     *
     * <p>泛微口径：收回 = 上一节点重新办理，所以收回后流程记录里不应该再挂着上一轮的「同意 + 意见」。
     * 这里做两件事：</p>
     * <p>1) 本次被挪走的当前节点任务谁都没办理过，历史任务行直接删掉，流水里不留一行「未办理」；</p>
     * <p>2) 上一节点最近一轮的办理记录（历史任务行 + 签字意见）一并删掉，
     *    重新办理时看到的是「未办理」而不是上一次的结论；</p>
     * <p>3) 收回到发起人节点时不删发起记录，只清当前节点的空行。</p>
     *
     * <p>清理失败只记告警，不影响已经完成的节点回退。</p>
     */
    private void clearRecalledTrace(String instanceId, java.util.Set<String> endedTaskIds, String prevId,
            boolean prevIsInitiator)
    {
        for (String taskId : endedTaskIds)
        {
            dropHistoricTask(taskId);
        }
        if (prevIsInitiator)
        {
            return;
        }
        // 上一节点最近一轮已办结的任务（本次刚结束的那些属于当前节点，排除掉）
        List<HistoricTaskInstance> prevTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId).taskDefinitionKey(prevId).finished()
                .orderByHistoricTaskInstanceEndTime().desc().list();
        for (HistoricTaskInstance h : prevTasks)
        {
            if (!endedTaskIds.contains(h.getId()))
            {
                dropHistoricTask(h.getId());
                break;
            }
        }
    }

    /**
     * 删除一条已办结的历史任务及其办理意见（收回后不留上一轮痕迹）。
     *
     * <p>先按任务删意见（ACT_HI_COMMENT），再删历史任务行；
     * 任何异常只告警——收回本身已经生效，不能因为清理失败又报错。</p>
     */
    private void dropHistoricTask(String taskId)
    {
        try
        {
            HistoricTaskInstance h = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
            // 只有已办结（有结束时间）的任务才能删；运行中的待办不能动
            if (h == null || h.getEndTime() == null)
            {
                return;
            }
            for (Comment c : taskService.getTaskComments(taskId))
            {
                taskService.deleteComment(c.getId());
            }
            historyService.deleteHistoricTaskInstance(taskId);
            log.info("强制收回：已清除办理记录 task={}（{}）", taskId, h.getTaskDefinitionKey());
        }
        catch (Exception e)
        {
            log.warn("强制收回：清除办理记录失败 task={}，{}", taskId, e.getMessage());
        }
    }

    /**
     * 沿 BPMN 连线逆向回溯，按「由近到远」返回当前节点的上游用户任务节点，用于定位「上一节点」。
     *
     * <p>网关、事件等非用户任务节点只参与回溯、不作为结果；遇到用户任务即停止向更上游扩散，
     * 保证取到的是最邻近的那个环节。并行分支时可能返回多个候选。</p>
     */
    private List<String> findUpstreamUserTasks(BpmnModel model, List<String> currentIds)
    {
        List<String> result = new ArrayList<>();
        java.util.Set<String> visited = new java.util.HashSet<>(currentIds);
        java.util.Deque<String> queue = new java.util.ArrayDeque<>(currentIds);
        while (!queue.isEmpty())
        {
            FlowElement el = model.getFlowElement(queue.poll());
            if (!(el instanceof org.flowable.bpmn.model.FlowNode))
            {
                continue;
            }
            for (org.flowable.bpmn.model.SequenceFlow flow : ((org.flowable.bpmn.model.FlowNode) el).getIncomingFlows())
            {
                String sourceId = flow.getSourceRef();
                if (StringUtils.isEmpty(sourceId) || !visited.add(sourceId))
                {
                    continue;
                }
                FlowElement source = model.getFlowElement(sourceId);
                if (source instanceof UserTask)
                {
                    if (!result.contains(sourceId))
                    {
                        result.add(sourceId);
                    }
                    continue;
                }
                queue.add(sourceId);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public int adminEndProcess(String instanceId, String action, String reason)
    {
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (instance == null)
        {
            throw new ServiceException("流程实例不存在或已结束");
        }
        boolean forceEnd = "end".equals(action);
        String prefix = forceEnd ? ADMIN_END_PREFIX : ADMIN_CANCEL_PREFIX;
        String actionText = forceEnd ? "强制结束" : "撤销";
        String msg = StringUtils.isNotEmpty(reason) ? reason : ("管理员" + actionText);
        // 意见要在删除实例前写入（删除后任务已不存在）
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(instanceId).list();
        String comment = prefix + actionText + "：" + msg;
        if (!tasks.isEmpty())
        {
            taskService.addComment(tasks.get(0).getId(), instanceId, "comment",
                    "【" + (forceEnd ? "forceEnd" : "cancel") + "】" + actionText + "：" + msg);
        }
        else
        {
            taskService.addComment(null, instanceId, "comment", comment);
        }
        runtimeService.deleteProcessInstance(instanceId, prefix + msg);
        writeBackLeaveStatus(instanceId, "4", msg);
        return 1;
    }

    @Override
    @Transactional
    public int deleteProcessInstance(String instanceId)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            throw new ServiceException("流程实例标识不能为空");
        }
        boolean exist = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).count() > 0;
        if (!exist)
        {
            throw new ServiceException("流程实例不存在");
        }
        // 运行中先停止，再连历史一起删除（否则历史表残留、流程图仍能找到实例）
        if (runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).count() > 0)
        {
            runtimeService.deleteProcessInstance(instanceId, "【adminDelete】管理员删除流程实例");
        }
        historyService.deleteHistoricProcessInstance(instanceId);
        // 抄送记录随流程一起清理，避免「待阅」里留下打不开的死链
        try
        {
            jdbcTemplate.update("DELETE FROM flow_cc WHERE instance_id = ?", instanceId);
        }
        catch (Exception ignored)
        {
        }
        return 1;
    }

    @Override
    @Transactional
    public int adminAssignTask(String taskId, Long targetUserId, String comment)
    {
        if (targetUserId == null)
        {
            throw new ServiceException("请选择新的办理人");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已办理");
        }
        taskService.setAssignee(taskId, String.valueOf(targetUserId));
        taskService.addComment(taskId, task.getProcessInstanceId(), "comment",
                "【assign】管理员改派办理人：" + resolveUserNickname(String.valueOf(targetUserId))
                        + (StringUtils.isNotEmpty(comment) ? "，" + comment : ""));
        return 1;
    }

    /** 同步回写请假示例业务表状态（3=驳回 4=撤销/终止），失败不影响流程操作 */
    private void writeBackLeaveStatus(String instanceId, String status, String opinion)
    {
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
                    update.setStatus(status);
                    update.setAuditOpinion(opinion);
                    update.setUpdateBy(SecurityUtils.getUsername());
                    flowLeaveMapper.updateFlowLeave(update);
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    @Override
    @Transactional
    public int terminateTask(String taskId, String reason)
    {
        if (StringUtils.isEmpty(taskId))
        {
            throw new ServiceException("任务标识不能为空");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已办理");
        }
        String currentUserId = String.valueOf(SecurityUtils.getUserId());
        if (!currentUserId.equals(task.getAssignee()))
        {
            if (StringUtils.isEmpty(task.getAssignee()))
            {
                // 候选池任务（指定部门/角色/岗位解析出多人）同样允许候选人终止：先认领再终止，
                // 与 complete / addSign 的候选处理保持一致，否则前台待办点「终止」会误报无权限
                long candidate = taskService.createTaskQuery().taskId(taskId)
                        .taskCandidateUser(currentUserId).count();
                if (candidate == 0)
                {
                    throw new ServiceException("您不是该任务的办理人或候选人");
                }
                taskService.claim(taskId, currentUserId);
            }
            else
            {
                throw new ServiceException("您不是该任务的办理人");
            }
        }
        String instanceId = task.getProcessInstanceId();
        String msg = StringUtils.isNotEmpty(reason) ? reason : "办理人终止";
        // 意见需在删除实例前写入历史（删除后任务已不存在，写不进去）
        taskService.addComment(taskId, instanceId, "comment", "【terminate】" + msg);
        runtimeService.deleteProcessInstance(instanceId, msg);
        writeBackLeaveStatus(instanceId, "4", msg);
        return 1;
    }

    @Override
    public List<FlowTask> selectTodoList(FlowTask query)
    {
        Long userId = SecurityUtils.getUserId();
        // taskCandidateOrAssigned：既包含直接指派给本人的任务，也包含「指定部门/角色/岗位」这类
        // 多人候选池里的任务（否则候选人看不到待办）
        TaskQuery taskQuery = taskService.createTaskQuery()
                .taskCandidateOrAssigned(String.valueOf(userId))
                .orderByTaskCreateTime().desc();
        if (StringUtils.isNotEmpty(query.getTaskName()))
        {
            taskQuery.taskNameLike("%" + query.getTaskName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionName()))
        {
            taskQuery.processDefinitionNameLike("%" + query.getProcessDefinitionName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionKey()))
        {
            taskQuery.processDefinitionKey(query.getProcessDefinitionKey());
        }
        List<Task> tasks = taskQuery.list();
        List<FlowTask> list = new ArrayList<>();
        for (Task task : tasks)
        {
            FlowTask ft = convertTask(task);
            // 退回标记：被上游驳回退回的待办，在待办「退回」页签单独归类
            fillReturnInfo(ft, task.getId());
            if (!matchReturnFilter(ft, query))
            {
                continue;
            }
            if (matchTaskFilter(ft, query))
            {
                list.add(ft);
            }
        }
        return list;
    }

    /** 读取任务级退回变量，填到待办行上（flowReturned/flowReturnFrom/flowReturnBy/flowReturnTime） */
    private void fillReturnInfo(FlowTask ft, String taskId)
    {
        Object flag = null;
        try
        {
            flag = taskService.getVariableLocal(taskId, FlowReturnConstants.VAR_RETURNED);
        }
        catch (Exception e)
        {
            return;
        }
        boolean returned = flag != null && ("1".equals(String.valueOf(flag)) || Boolean.TRUE.equals(flag));
        ft.setReturned(returned);
        if (!returned)
        {
            return;
        }
        ft.setReturnFrom(asString(taskService.getVariableLocal(taskId, FlowReturnConstants.VAR_RETURN_FROM)));
        ft.setReturnByName(asString(taskService.getVariableLocal(taskId, FlowReturnConstants.VAR_RETURN_BY)));
        ft.setReturnTime(asString(taskService.getVariableLocal(taskId, FlowReturnConstants.VAR_RETURN_TIME)));
    }

    /** 待办页签过滤：returnedOnly=true 只看被退回的，false 只看正常待办，null 全部 */
    private boolean matchReturnFilter(FlowTask ft, FlowTask query)
    {
        Boolean only = query.getReturnedOnly();
        if (only == null)
        {
            return true;
        }
        boolean returned = Boolean.TRUE.equals(ft.getReturned());
        return only == returned;
    }

    private String asString(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    /** 任务列表内存过滤：流程标题/创建人/创建时间区间（这些条件 Flowable 原生查询不支持） */
    private boolean matchTaskFilter(FlowTask ft, FlowTask query)
    {
        if (StringUtils.isNotEmpty(query.getTitle())
                && (ft.getTitle() == null || !ft.getTitle().contains(query.getTitle())))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(query.getStartUserName())
                && (ft.getStartUserName() == null || !ft.getStartUserName().contains(query.getStartUserName())))
        {
            return false;
        }
        java.util.Map<String, Object> params = query.getParams();
        if (params != null)
        {
            String begin = (String) params.get("beginTime");
            String end = (String) params.get("endTime");
            try
            {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                if (StringUtils.isNotEmpty(begin) && ft.getCreateTime() != null
                        && ft.getCreateTime().before(sdf.parse(begin)))
                {
                    return false;
                }
                if (StringUtils.isNotEmpty(end) && ft.getCreateTime() != null)
                {
                    java.util.Date endDate = sdf.parse(end);
                    // 结束日期含当天（23:59:59）
                    java.util.Calendar c = java.util.Calendar.getInstance();
                    c.setTime(endDate);
                    c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                    if (ft.getCreateTime().after(c.getTime()))
                    {
                        return false;
                    }
                }
            }
            catch (java.text.ParseException ignored) { }
        }
        return true;
    }

    @Override
    public Map<String, Object> countTodoMap()
    {
        Long userId = SecurityUtils.getUserId();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(String.valueOf(userId)).list();
        long returned = 0L;
        for (Task task : tasks)
        {
            try
            {
                Object flag = taskService.getVariableLocal(task.getId(), FlowReturnConstants.VAR_RETURNED);
                if (flag != null && ("1".equals(String.valueOf(flag)) || Boolean.TRUE.equals(flag)))
                {
                    returned++;
                }
            }
            catch (Exception ignored)
            {
            }
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("all", (long) tasks.size());
        result.put("returned", returned);
        return result;
    }

    @Override
    public long countTodo()
    {
        Long userId = SecurityUtils.getUserId();
        return taskService.createTaskQuery().taskCandidateOrAssigned(String.valueOf(userId)).count();
    }

    /**
     * 我的已办。口径对标泛微「已办事宜」：
     * 1）我办理过的任务（历史已结束、办理人=我）；
     * 2）我发起的流程——发起（提交）本身就是一次办理，发起人即使还没审批过任何节点，
     *    该流程也应出现在已办里（环节显示「发起申请」，结果显示「发起」）。
     * 同一流程实例只保留一条，取我最近一次动作，避免同一流程重复占行。
     */
    @Override
    public List<FlowTask> selectDoneList(FlowTask query)
    {
        Long userId = SecurityUtils.getUserId();
        String uid = String.valueOf(userId);
        java.util.LinkedHashMap<String, FlowTask> rowMap = new java.util.LinkedHashMap<>();
        // 1）我办理过的任务（完成时间倒序，putIfAbsent 即保留该实例内我的最后一次办理）
        HistoricTaskInstanceQuery hisQuery = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(uid)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc();
        if (StringUtils.isNotEmpty(query.getProcessDefinitionName()))
        {
            hisQuery.processDefinitionNameLike("%" + query.getProcessDefinitionName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionKey()))
        {
            hisQuery.processDefinitionKey(query.getProcessDefinitionKey());
        }
        for (HistoricTaskInstance task : hisQuery.list())
        {
            FlowTask ft = convertHistoricTask(task);
            // 取该任务的审批意见（type=comment），解析【outcome】前缀
            List<Comment> comments = taskService.getTaskComments(task.getId());
            if (!comments.isEmpty())
            {
                applyComment(ft, comments.get(0));
            }
            if (!matchDoneFilter(ft, query))
            {
                continue;
            }
            rowMap.putIfAbsent(ft.getInstanceId(), ft);
        }
        // 2）我发起的流程：已被上面「我办理过的任务」覆盖的实例不再重复计入
        HistoricProcessInstanceQuery instQuery = historyService.createHistoricProcessInstanceQuery()
                .startedBy(uid)
                .orderByProcessInstanceStartTime().desc();
        if (StringUtils.isNotEmpty(query.getProcessDefinitionName()))
        {
            instQuery.processDefinitionNameLike("%" + query.getProcessDefinitionName() + "%");
        }
        if (StringUtils.isNotEmpty(query.getProcessDefinitionKey()))
        {
            instQuery.processDefinitionKey(query.getProcessDefinitionKey());
        }
        for (HistoricProcessInstance hpi : instQuery.list())
        {
            if (rowMap.containsKey(hpi.getId()))
            {
                continue;
            }
            FlowTask ft = new FlowTask();
            ft.setInstanceId(hpi.getId());
            ft.setProcessDefinitionId(hpi.getProcessDefinitionId());
            ft.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
            ft.setProcessDefinitionName(hpi.getProcessDefinitionName());
            ft.setBusinessKey(hpi.getBusinessKey());
            ft.setTitle(StringUtils.isNotEmpty(hpi.getName()) ? hpi.getName() : hpi.getProcessDefinitionName());
            ft.setStartUserName(resolveUserNickname(hpi.getStartUserId()));
            ft.setUrgency(resolveUrgency(hpi.getProcessDefinitionKey(), hpi.getBusinessKey()));
            // 发起环节：办理时间取发起时间
            ft.setCreateTime(hpi.getStartTime());
            ft.setEndTime(hpi.getStartTime());
            ft.setTaskName("发起申请");
            ft.setOutcome("submit");
            fillInstanceStatus(ft, hpi);
            if (!matchDoneFilter(ft, query))
            {
                continue;
            }
            rowMap.put(ft.getInstanceId(), ft);
        }
        List<FlowTask> list = new ArrayList<>(rowMap.values());
        // 统一按办理时间倒序（我发起的流程与办理过的任务混排）
        list.sort((a, b) -> {
            java.util.Date ta = a.getEndTime() != null ? a.getEndTime() : a.getCreateTime();
            java.util.Date tb = b.getEndTime() != null ? b.getEndTime() : b.getCreateTime();
            if (ta == null && tb == null)
            {
                return 0;
            }
            if (ta == null)
            {
                return 1;
            }
            if (tb == null)
            {
                return -1;
            }
            return tb.compareTo(ta);
        });
        return list;
    }

    /**
     * 已办列表内存过滤：流程标题/创建人/办理时间区间。
     * 与待办不同，已办的时间筛选按「办理时间（endTime）」而非「创建时间」，
     * 否则我发起后隔天办理的任务会被筛掉。
     */
    private boolean matchDoneFilter(FlowTask ft, FlowTask query)
    {
        // 页签口径：全部 / 未结束（running）/ 已结束（completed + canceled）
        String scope = query.getStatusScope();
        if (StringUtils.isNotEmpty(scope) && !"all".equals(scope))
        {
            if ("running".equals(scope) && !"running".equals(ft.getStatus()))
            {
                return false;
            }
            if ("finished".equals(scope) && "running".equals(ft.getStatus()))
            {
                return false;
            }
        }
        if (StringUtils.isNotEmpty(query.getTaskName())
                && (ft.getTaskName() == null || !ft.getTaskName().contains(query.getTaskName())))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(query.getTitle())
                && (ft.getTitle() == null || !ft.getTitle().contains(query.getTitle())))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(query.getStartUserName())
                && (ft.getStartUserName() == null || !ft.getStartUserName().contains(query.getStartUserName())))
        {
            return false;
        }
        java.util.Map<String, Object> params = query.getParams();
        if (params == null)
        {
            return true;
        }
        String begin = (String) params.get("beginTime");
        String end = (String) params.get("endTime");
        java.util.Date time = ft.getEndTime() != null ? ft.getEndTime() : ft.getCreateTime();
        try
        {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            if (StringUtils.isNotEmpty(begin) && time != null && time.before(sdf.parse(begin)))
            {
                return false;
            }
            if (StringUtils.isNotEmpty(end) && time != null)
            {
                java.util.Calendar c = java.util.Calendar.getInstance();
                c.setTime(sdf.parse(end));
                // 结束日期含当天
                c.add(java.util.Calendar.DAY_OF_MONTH, 1);
                if (time.after(c.getTime()))
                {
                    return false;
                }
            }
        }
        catch (java.text.ParseException ignored) { }
        return true;
    }

    /**
     * 已办页签数量统计：全部 / 未结束 / 已结束。
     * 一次全量查询后按流程状态归类，避免前端为三个页签分别发一次全量列表请求。
     */
    @Override
    public Map<String, Object> countDone()
    {
        List<FlowTask> list = selectDoneList(new FlowTask());
        long running = 0L;
        for (FlowTask ft : list)
        {
            if ("running".equals(ft.getStatus()))
            {
                running++;
            }
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("all", (long) list.size());
        result.put("running", running);
        result.put("finished", (long) list.size() - running);
        return result;
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
            if (task.getAssignee() == null || task.getAssignee().isEmpty())
            {
                // 候选任务（指定部门/角色/岗位解析出多人时进候选池）：认领后办理，先到先得
                long candidate = taskService.createTaskQuery().taskId(dto.getTaskId())
                        .taskCandidateUser(String.valueOf(userId)).count();
                if (candidate == 0)
                {
                    throw new ServiceException("您不是该任务的办理人或候选人");
                }
                taskService.claim(dto.getTaskId(), String.valueOf(userId));
            }
            else
            {
                throw new ServiceException("您不是该任务的办理人");
            }
        }
        String outcome = StringUtils.isNotEmpty(dto.getOutcome()) ? dto.getOutcome() : "approved";
        // 加签（委派）任务：Flowable 不允许 complete，必须 resolve —— 办理后任务回到原办理人继续办理，
        // 流程不推进（否则引擎抛 "A delegated Task cannot be completed, but should be resolved instead"）
        boolean delegated = org.flowable.task.api.DelegationState.PENDING.equals(task.getDelegationState());
        // 驳回（退回）：把流程退回选定的前置节点，而不是继续走向下一节点
        if ("rejected".equals(outcome) && !delegated)
        {
            return returnToNode(task, dto, outcome);
        }
        // 记录审批意见：type 必须为 "comment"（Flowable 默认查询仅返回该类型），
        // outcome 编码进 FULL_MSG_ 前缀【outcome:xxx】便于读取
        taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(),
                "comment", "【" + outcome + "】" + (dto.getComment() == null ? "" : dto.getComment()));
        if (dto.getVariables() != null && !dto.getVariables().isEmpty())
        {
            if (delegated)
            {
                taskService.resolveTask(dto.getTaskId(), dto.getVariables());
            }
            else
            {
                taskService.complete(dto.getTaskId(), dto.getVariables());
            }
        }
        else
        {
            if (delegated)
            {
                taskService.resolveTask(dto.getTaskId());
            }
            else
            {
                taskService.complete(dto.getTaskId());
            }
        }
        if (delegated)
        {
            // 委派办理只是把任务交回原办理人，流程未推进，无需走结束回写
            return 1;
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

    /**
     * 驳回（退回）到指定节点。
     *
     * 泛微口径：驳回不是「流程继续往下走」，而是把执行流从当前节点挪回前面的某个节点，
     * 该节点办理人重新出现在待办里（并归到待办「退回」页签）。
     *
     * 实现要点：
     * <ul>
     * <li>退回意见必须在跳转前写入（跳转后当前任务被删除，再写 comment 就找不到任务了）；</li>
     * <li>用 {@code moveActivityIdsToSingleActivityId} 做节点跳转，并行/会签的多个当前节点一起挪；</li>
     * <li>跳转后新产生的任务打上任务级变量 flowReturned=1，前端据此归类到「退回」页签；
     *     任务级变量随任务结束失效，不会污染后续环节。</li>
     * </ul>
     */
    private int returnToNode(Task task, FlowCompleteDTO dto, String outcome)
    {
        String instanceId = task.getProcessInstanceId();
        String targetId = dto.getTargetActivityId();
        if (StringUtils.isEmpty(targetId))
        {
            throw new ServiceException("请选择要退回的环节");
        }
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (instance == null)
        {
            throw new ServiceException("流程已结束，无法退回");
        }
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (model == null)
        {
            throw new ServiceException("流程定义不存在，无法退回");
        }
        FlowElement targetEl = model.getFlowElement(targetId);
        if (!(targetEl instanceof UserTask))
        {
            throw new ServiceException("退回目标环节不存在或不是用户任务环节");
        }
        UserTask targetTask = (UserTask) targetEl;
        // 安全校验：只允许退回「已经走过的环节」或「发起人（申请）节点」，防止前端乱传节点ID
        boolean initiatorNode = isInitiatorNode(targetTask);
        if (!initiatorNode && !isVisitedUserTask(instanceId, targetId))
        {
            throw new ServiceException("只能退回到已经流转过的环节");
        }
        String targetName = StringUtils.isNotEmpty(targetTask.getName()) ? targetTask.getName() : targetId;
        String fromName = StringUtils.isNotEmpty(task.getName()) ? task.getName() : task.getTaskDefinitionKey();
        String userId = String.valueOf(SecurityUtils.getUserId());
        String userName = resolveUserNickname(userId);
        String opinion = dto.getComment() == null ? "" : dto.getComment();
        // 1) 退回意见先落库（type 固定 comment，outcome 编码进前缀，与同意保持一致）
        StringBuilder text = new StringBuilder("【").append(outcome).append("】退回至「").append(targetName).append("」");
        if (StringUtils.isNotEmpty(stripHtml(opinion)))
        {
            text.append("：").append(opinion);
        }
        taskService.addComment(task.getId(), instanceId, "comment", text.toString());
        // 2) 记录跳转前仍活动的任务，用于识别跳转后新产生的任务
        java.util.Set<String> beforeIds = new java.util.HashSet<>();
        for (Task t : taskService.createTaskQuery().processInstanceId(instanceId).list())
        {
            beforeIds.add(t.getId());
        }
        // 3) 当前活动节点（并行/会签可能有多个）：只保留用户任务节点，跳转时一起挪走
        List<String> fromIds = new ArrayList<>();
        for (String id : runtimeService.getActiveActivityIds(instanceId))
        {
            if (model.getFlowElement(id) instanceof UserTask && !fromIds.contains(id))
            {
                fromIds.add(id);
            }
        }
        if (fromIds.isEmpty())
        {
            fromIds.add(task.getTaskDefinitionKey());
        }
        try
        {
            runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(instanceId)
                    .moveActivityIdsToSingleActivityId(fromIds, targetId)
                    .changeState();
        }
        catch (Exception e)
        {
            log.error("流程退回失败：instance={}, target={}, error={}", instanceId, targetId, e.getMessage(), e);
            throw new ServiceException("退回失败：" + e.getMessage());
        }
        // 4) 新产生的任务打「退回」标记（任务级变量，任务办结即失效）
        String now = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        List<Task> afterTasks = taskService.createTaskQuery().processInstanceId(instanceId).list();
        int marked = 0;
        for (Task t : afterTasks)
        {
            if (beforeIds.contains(t.getId()))
            {
                continue;
            }
            taskService.setVariableLocal(t.getId(), FlowReturnConstants.VAR_RETURNED, "1");
            taskService.setVariableLocal(t.getId(), FlowReturnConstants.VAR_RETURN_FROM, fromName);
            taskService.setVariableLocal(t.getId(), FlowReturnConstants.VAR_RETURN_BY, userName);
            taskService.setVariableLocal(t.getId(), FlowReturnConstants.VAR_RETURN_TIME, now);
            marked++;
        }
        // 5) 流程级变量：最近一次退回信息（流程图/流程状态/业务表单可展示）
        runtimeService.setVariable(instanceId, "flowLastReturnFrom", fromName);
        runtimeService.setVariable(instanceId, "flowLastReturnTo", targetName);
        log.info("流程退回：instance={}，{} → {}，新任务 {} 个，操作人 {}", instanceId, fromName, targetName, marked, userName);
        return 1;
    }

    @Override
    public List<Map<String, Object>> selectReturnableNodes(String taskId)
    {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已办理");
        }
        List<Map<String, Object>> nodes = new ArrayList<>();
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (model == null)
        {
            return nodes;
        }
        String instanceId = task.getProcessInstanceId();
        String currentKey = task.getTaskDefinitionKey();
        // 1) 发起人（申请）节点：部署时注入的「提交申请」节点，正常发起不经过，只作为退回目标
        UserTask initiatorNode = findInitiatorNode(model);
        if (initiatorNode != null && !initiatorNode.getId().equals(currentKey))
        {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", initiatorNode.getId());
            m.put("name", StringUtils.isNotEmpty(initiatorNode.getName()) ? initiatorNode.getName() : "提交申请");
            m.put("type", FlowReturnConstants.NODE_TYPE_INITIATOR);
            m.put("lastHandler", resolveUserNickname(getInstanceStartUser(instanceId)));
            nodes.add(m);
        }
        // 2) 已经走过的用户任务节点：按首次经过的先后顺序返回（同一节点多轮只出现一次）
        java.util.LinkedHashMap<String, Map<String, Object>> visited = new java.util.LinkedHashMap<>();
        try
        {
            List<HistoricActivityInstance> acts = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(instanceId)
                    .activityType("userTask")
                    .orderByHistoricActivityInstanceStartTime().asc()
                    .list();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (HistoricActivityInstance act : acts)
            {
                String id = act.getActivityId();
                if (StringUtils.isEmpty(id) || id.equals(currentKey) || visited.containsKey(id))
                {
                    continue;
                }
                if (!(model.getFlowElement(id) instanceof UserTask))
                {
                    continue;
                }
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", id);
                m.put("name", StringUtils.isNotEmpty(act.getActivityName()) ? act.getActivityName() : id);
                m.put("type", FlowReturnConstants.NODE_TYPE_USER_TASK);
                m.put("lastHandler", StringUtils.isEmpty(act.getAssignee()) ? null : resolveUserNickname(act.getAssignee()));
                m.put("time", act.getEndTime() == null ? null : sdf.format(act.getEndTime()));
                visited.put(id, m);
            }
        }
        catch (Exception e)
        {
            log.warn("查询可退回节点失败：{}", e.getMessage());
        }
        nodes.addAll(visited.values());
        return nodes;
    }

    /** 查找发起人（申请）节点：部署时注入、带 initiatorNode 扩展属性的用户任务 */
    private UserTask findInitiatorNode(BpmnModel model)
    {
        if (model.getMainProcess() == null)
        {
            return null;
        }
        for (UserTask ut : model.getMainProcess().findFlowElementsOfType(UserTask.class, true))
        {
            if (isInitiatorNode(ut))
            {
                return ut;
            }
        }
        return null;
    }

    /** 节点是否为发起人（申请）节点 */
    private boolean isInitiatorNode(UserTask task)
    {
        Map<String, java.util.List<org.flowable.bpmn.model.ExtensionAttribute>> attrs = task.getAttributes();
        if (attrs == null)
        {
            return false;
        }
        java.util.List<org.flowable.bpmn.model.ExtensionAttribute> list =
                attrs.get(FlowReturnConstants.ATTR_INITIATOR_NODE);
        if (list == null || list.isEmpty())
        {
            return false;
        }
        String value = list.get(0).getValue();
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /** 该节点是否已经在本实例流转过（历史有已办结的用户任务） */
    private boolean isVisitedUserTask(String instanceId, String activityId)
    {
        try
        {
            return historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(instanceId)
                    .activityId(activityId)
                    .finished()
                    .count() > 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /** 流程发起人ID（退回发起人节点时展示用） */
    private String getInstanceStartUser(String instanceId)
    {
        try
        {
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId).singleResult();
            return hpi == null ? null : hpi.getStartUserId();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /** 去掉富文本标签，仅用于判断意见是否为空 */
    private String stripHtml(String html)
    {
        if (StringUtils.isEmpty(html))
        {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").replace("&nbsp;", "").trim();
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
        // 转办同样要限定在「本人任务 / 本人在候选池」范围内，否则任何有办理权限的人都能把别人的任务转走
        String currentUserId = String.valueOf(SecurityUtils.getUserId());
        if (!currentUserId.equals(task.getAssignee()))
        {
            if (StringUtils.isEmpty(task.getAssignee()))
            {
                long candidate = taskService.createTaskQuery().taskId(taskId)
                        .taskCandidateUser(currentUserId).count();
                if (candidate == 0)
                {
                    throw new ServiceException("您不是该任务的办理人或候选人");
                }
                taskService.claim(taskId, currentUserId);
            }
            else
            {
                throw new ServiceException("您不是该任务的办理人");
            }
        }
        taskService.setAssignee(taskId, String.valueOf(targetUserId));
        // type 固定为 "comment"，转办标识编码进消息前缀
        taskService.addComment(taskId, task.getProcessInstanceId(), "comment",
                "【delegate】转办：" + (comment == null ? "" : comment));
        return 1;
    }

    /**
     * 发起时把表单里的「签字意见」写成流程级批注（taskId 为空）。
     * 发起环节没有任务，不存下来的话「审批意见」里的发起行就永远是空白的；
     * 存成批注而不是只靠流程变量，是为了后续节点即使改写 signOpinion 变量，发起时的原话也不会被覆盖。
     */
    private void saveStartComment(String instanceId, Map<String, Object> variables)
    {
        try
        {
            if (variables == null || variables.get(FlowSystemFields.FIELD_SIGN_OPINION) == null)
            {
                return;
            }
            String sign = String.valueOf(variables.get(FlowSystemFields.FIELD_SIGN_OPINION));
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(sign.replaceAll("<[^>]*>", "").replace("&nbsp;", "").trim()))
            {
                return;
            }
            taskService.addComment(null, instanceId, "comment", START_COMMENT_PREFIX + sign);
        }
        catch (Exception e)
        {
            log.warn("发起签字意见写入失败：{}", e.getMessage());
        }
    }

    /**
     * 发起节点的签字意见：优先取发起时写入的流程级批注，旧实例回退到流程变量 signOpinion。
     */
    private String startSignOpinion(String instanceId)
    {
        try
        {
            List<Comment> comments = taskService.getProcessInstanceComments(instanceId);
            for (Comment comment : comments)
            {
                String msg = comment.getFullMessage();
                if (msg != null && msg.startsWith(START_COMMENT_PREFIX))
                {
                    return msg.substring(START_COMMENT_PREFIX.length());
                }
            }
        }
        catch (Exception e)
        {
            log.warn("发起签字意见读取失败：{}", e.getMessage());
        }
        try
        {
            org.flowable.variable.api.history.HistoricVariableInstance var = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(instanceId)
                    .variableName(FlowSystemFields.FIELD_SIGN_OPINION)
                    .singleResult();
            if (var != null && var.getValue() != null)
            {
                String text = String.valueOf(var.getValue());
                if (StringUtils.isNotEmpty(text))
                {
                    return text;
                }
            }
        }
        catch (Exception e)
        {
            log.warn("发起签字意见变量读取失败：{}", e.getMessage());
        }
        return null;
    }

    @Override
    public List<FlowTask> selectHistoryTasks(String instanceId)
    {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();
        // 节点候选人：Flowable 在任务创建时就把候选链接写进 act_hi_identitylink（任务结束也不清理），
        // 据此把「候选池里的一个任务」还原成「每个候选人一行」，与泛微流程状态一致
        Map<String, List<String>> candidatesByTask = selectTaskCandidates(instanceId);
        List<FlowTask> list = new ArrayList<>();
        // 发起节点（开始事件）：发起人=实例发起人，接收/操作时间=实例开始时间
        if (hpi != null)
        {
            FlowTask start = new FlowTask();
            start.setTaskId("__start__");
            start.setTaskName("发起");
            start.setInstanceId(instanceId);
            start.setCreateTime(hpi.getStartTime());
            start.setEndTime(hpi.getStartTime());
            start.setOutcome("start");
            start.setState("done");
            // 发起时在表单里填的签字意见（否则发起行永远显示「—」）
            start.setComment(startSignOpinion(instanceId));
            if (StringUtils.isNotEmpty(hpi.getStartUserId()))
            {
                start.setAssigneeName(resolveUserNickname(hpi.getStartUserId()));
            }
            list.add(start);
        }
        for (HistoricTaskInstance task : tasks)
        {
            FlowTask ft = convertHistoricTask(task);
            List<Comment> comments = taskService.getTaskComments(task.getId());
            for (Comment comment : comments)
            {
                applyComment(ft, comment);
            }
            boolean finished = task.getEndTime() != null;
            String assignee = task.getAssignee();
            if (StringUtils.isNotEmpty(assignee))
            {
                ft.setAssigneeName(resolveUserNickname(assignee));
            }
            List<String> candidates = candidatesByTask.get(task.getId());
            // 尚未有人认领的候选池任务：不再输出「待认领」空行，改为每个候选人各一行
            if (StringUtils.isEmpty(assignee) && candidates != null && !candidates.isEmpty())
            {
                for (String userId : candidates)
                {
                    list.add(buildCandidateRow(ft, userId, finished ? "undone" : "pending"));
                }
                continue;
            }
            // 已办理以「有办理意见」为准：撤销/终止时引擎会给未办任务补 endTime，但没人真正办理
            ft.setState(finished ? (StringUtils.isNotEmpty(ft.getOutcome()) ? "done" : "undone") : "pending");
            list.add(ft);
            // 已有人办理：同节点其余候选人各补一行「未办理」，一眼看出流程发给了谁、谁没处理
            if (finished && candidates != null)
            {
                for (String userId : candidates)
                {
                    if (userId.equals(assignee))
                    {
                        continue;
                    }
                    list.add(buildCandidateRow(ft, userId, "undone"));
                }
            }
        }
        // 结束节点（结束事件）：流程已结束时补充"流程结束"记录
        if (hpi != null && hpi.getEndTime() != null)
        {
            FlowTask end = new FlowTask();
            end.setTaskId("__end__");
            end.setTaskName("流程结束");
            end.setInstanceId(instanceId);
            end.setCreateTime(hpi.getStartTime());
            end.setEndTime(hpi.getEndTime());
            end.setOutcome(StringUtils.isNotEmpty(hpi.getDeleteReason()) ? "canceled" : "end");
            end.setState("done");
            list.add(end);
        }
        return list;
    }

    /**
     * 查询流程实例下各节点的候选人（act_hi_identitylink 的 candidate 链接）。
     *
     * <p>运行中任务与已结束任务都有记录，因此流程状态里能同时还原「当前谁可以办」和「当时发给了谁」。
     * 候选组（GROUP_ID_）暂不展开：节点派发统一走 addCandidateUser，不走候选组。</p>
     *
     * @param instanceId 流程实例ID
     * @return taskId → 候选人用户ID列表（按派发顺序去重）
     */
    private Map<String, List<String>> selectTaskCandidates(String instanceId)
    {
        Map<String, List<String>> result = new java.util.HashMap<>();
        try
        {
            String sql = "SELECT l.TASK_ID_ AS taskId, l.USER_ID_ AS userId "
                    + "FROM act_hi_identitylink l JOIN act_hi_taskinst t ON t.ID_ = l.TASK_ID_ "
                    + "WHERE t.PROC_INST_ID_ = ? AND l.TYPE_ = 'candidate' AND l.USER_ID_ IS NOT NULL "
                    + "ORDER BY l.CREATE_TIME_ ASC";
            jdbcTemplate.query(sql, rs -> {
                String taskId = rs.getString("taskId");
                String userId = rs.getString("userId");
                if (StringUtils.isEmpty(taskId) || StringUtils.isEmpty(userId))
                {
                    return;
                }
                List<String> users = result.computeIfAbsent(taskId, k -> new ArrayList<>());
                if (!users.contains(userId))
                {
                    users.add(userId);
                }
            }, instanceId);
        }
        catch (Exception e)
        {
            log.warn("查询节点候选人失败：{}", e.getMessage());
        }
        return result;
    }

    /**
     * 以节点主记录为模板，生成某位候选人的参与行（同一任务的其余候选人）。
     *
     * @param base   节点主记录
     * @param userId 候选人用户ID
     * @param state  done / pending / undone
     */
    private FlowTask buildCandidateRow(FlowTask base, String userId, String state)
    {
        FlowTask row = new FlowTask();
        row.setTaskId(base.getTaskId());
        row.setTaskName(base.getTaskName());
        row.setTaskDefinitionKey(base.getTaskDefinitionKey());
        row.setInstanceId(base.getInstanceId());
        row.setProcessDefinitionId(base.getProcessDefinitionId());
        row.setProcessDefinitionName(base.getProcessDefinitionName());
        row.setProcessDefinitionKey(base.getProcessDefinitionKey());
        row.setBusinessKey(base.getBusinessKey());
        row.setTitle(base.getTitle());
        row.setUrgency(base.getUrgency());
        row.setStartUserName(base.getStartUserName());
        row.setStatus(base.getStatus());
        row.setCurrentTaskName(base.getCurrentTaskName());
        row.setCreateTime(base.getCreateTime());
        row.setEndTime(base.getEndTime());
        try
        {
            row.setAssigneeId(Long.valueOf(userId));
        }
        catch (NumberFormatException ignored) { }
        row.setAssigneeName(resolveUserNickname(userId));
        row.setState(state);
        row.setCandidate(Boolean.TRUE);
        return row;
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
            // 当前停留节点：未结束的活动（红框标注）
            List<String> currentActivityIds = new ArrayList<>();
            for (HistoricActivityInstance hai : activityInstances)
            {
                if (hai.getEndTime() == null && !"sequenceFlow".equals(hai.getActivityType()))
                {
                    currentActivityIds.add(hai.getActivityId());
                }
            }
            CurrentNodeProcessDiagramGenerator diagramGenerator =
                    new CurrentNodeProcessDiagramGenerator();
            try (InputStream in = diagramGenerator.generateDiagramWithCurrent(bpmnModel, "png",
                    highLightedActivities, highLightedFlows, currentActivityIds,
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

    @Override
    public Map<String, Object> selectInstanceDiagram(String instanceId)
    {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (hpi == null)
        {
            throw new ServiceException("流程实例不存在");
        }
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("instanceId", instanceId);
        data.put("processDefinitionName", hpi.getProcessDefinitionName());
        // 流程是否已结束：前端据此决定「当前节点」还用不用标
        data.put("finished", hpi.getEndTime() != null);
        data.put("xml", readProcessDefinitionXml(hpi.getProcessDefinitionId()));
        java.util.LinkedHashSet<String> doneIds = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> activeIds = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<String> doneFlowIds = new java.util.LinkedHashSet<>();
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId).list();
        for (HistoricActivityInstance hai : activities)
        {
            if ("sequenceFlow".equals(hai.getActivityType()))
            {
                // 已走过的连线：前端标绿，体现流转路径
                if (hai.getEndTime() != null)
                {
                    doneFlowIds.add(hai.getActivityId());
                }
                continue;
            }
            if (hai.getEndTime() == null)
            {
                // 未结束的活动 = 当前停留节点（会签/并行时可能多个）
                activeIds.add(hai.getActivityId());
            }
            else
            {
                doneIds.add(hai.getActivityId());
            }
        }
        data.put("doneIds", new ArrayList<>(doneIds));
        data.put("activeIds", new ArrayList<>(activeIds));
        data.put("doneFlowIds", new ArrayList<>(doneFlowIds));
        // 按节点聚合的运行期人员（节点上显示审批人 + 点节点看已操作/未审批/已查看）
        data.put("nodes", buildNodeParticipants(instanceId, hpi, activities));
        return data;
    }

    /**
     * 流程图节点人员信息（对标泛微运行期流程图）。
     *
     * <p>返回结构（key = 节点 activityId，与 BPMN 图形 ID 一致）：</p>
     * <pre>
     * {
     *   nodeId: 'Activity_xxx',
     *   name: '部门审批',
     *   done:    [ {userId, userName, taskId, action, comment, time} ],  // 已操作人员（有办理意见）
     *   pending: [ {userId, userName, taskId, kind} ],                  // 未审批操作人
     *            // kind: assignee=已签收待办 / candidate=候选池未签收 / skipped=撤销终止时被跳过
     *   viewed:  [ {userId, userName, time} ]                           // 已查看但未办理
     * }
     * </pre>
     *
     * @param instanceId 流程实例ID
     * @param hpi        历史流程实例
     * @param activities 该实例的历史活动（复用上层查询结果，开始节点据此定位）
     */
    private Map<String, Object> buildNodeParticipants(String instanceId, HistoricProcessInstance hpi,
            List<HistoricActivityInstance> activities)
    {
        Map<String, Object> nodes = new java.util.LinkedHashMap<>();
        Map<String, String> nameCache = new java.util.HashMap<>();
        // 1) 发起节点（开始事件）：发起人即该节点的操作人，与泛微在开始节点下显示申请人一致
        if (hpi != null && StringUtils.isNotEmpty(hpi.getStartUserId()))
        {
            for (HistoricActivityInstance hai : activities)
            {
                if (!"startEvent".equals(hai.getActivityType()))
                {
                    continue;
                }
                Map<String, Object> node = nodeEntry(nodes, hai.getActivityId(), hai.getActivityName());
                Map<String, Object> person = personEntry(hpi.getStartUserId(), nameCache);
                person.put("action", "start");
                person.put("time", hpi.getStartTime());
                doneList(node).add(person);
                break;
            }
        }
        // 2) 用户任务：按 taskDefinitionKey 聚合（同一节点被退回重办时会有多条任务记录）
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();
        // 候选池：任务创建时写入的 candidate 链接，任务结束也不清理，据此还原「当时发给了谁」
        Map<String, List<String>> candidatesByTask = selectTaskCandidates(instanceId);
        for (HistoricTaskInstance task : tasks)
        {
            String nodeId = task.getTaskDefinitionKey();
            if (StringUtils.isEmpty(nodeId))
            {
                continue;
            }
            Map<String, Object> node = nodeEntry(nodes, nodeId, task.getName());
            String assignee = task.getAssignee();
            if (task.getEndTime() != null)
            {
                // 已结束任务：有办理意见才算「已操作」；撤销/终止时引擎补了 endTime 但没人真正办理
                String outcome = null;
                String comment = null;
                for (Comment c : taskService.getTaskComments(task.getId()))
                {
                    String msg = c.getFullMessage();
                    if (StringUtils.isEmpty(msg))
                    {
                        continue;
                    }
                    if (msg.startsWith("【") && msg.contains("】"))
                    {
                        int end = msg.indexOf("】");
                        outcome = msg.substring(1, end);
                        comment = msg.substring(end + 1);
                    }
                    else
                    {
                        comment = msg;
                    }
                }
                if (StringUtils.isEmpty(assignee))
                {
                    continue;
                }
                if (StringUtils.isNotEmpty(outcome))
                {
                    Map<String, Object> person = personEntry(assignee, nameCache);
                    person.put("taskId", task.getId());
                    person.put("action", outcome);
                    person.put("comment", comment);
                    person.put("time", task.getEndTime());
                    // 同一节点被退回重办时同一人会办理多次，这里不去重，各自的动作与时间都有意义
                    doneList(node).add(person);
                }
                else
                {
                    Map<String, Object> person = personEntry(assignee, nameCache);
                    person.put("taskId", task.getId());
                    person.put("kind", "skipped");
                    addPending(node, person);
                }
                continue;
            }
            // 运行中任务：签收人 + 其余候选人都是「未审批操作人」
            if (StringUtils.isNotEmpty(assignee))
            {
                Map<String, Object> person = personEntry(assignee, nameCache);
                person.put("taskId", task.getId());
                person.put("kind", "assignee");
                addPending(node, person);
            }
            List<String> candidates = candidatesByTask.get(task.getId());
            if (candidates != null)
            {
                for (String candidate : candidates)
                {
                    if (candidate.equals(assignee))
                    {
                        continue;
                    }
                    Map<String, Object> person = personEntry(candidate, nameCache);
                    person.put("taskId", task.getId());
                    person.put("kind", "candidate");
                    addPending(node, person);
                }
            }
        }
        // 3) 已查看：只保留「看过但还没办理」的人，已操作/待办的人不必在查看列表里重复出现
        Map<String, List<Map<String, Object>>> viewers = flowNodeViewService.selectViewersByNode(instanceId);
        for (Map.Entry<String, List<Map<String, Object>>> entry : viewers.entrySet())
        {
            Map<String, Object> node = asNode(nodes.get(entry.getKey()));
            if (node == null)
            {
                continue;
            }
            java.util.Set<String> handled = new java.util.HashSet<>();
            for (Map<String, Object> p : doneList(node))
            {
                handled.add(String.valueOf(p.get("userId")));
            }
            for (Map<String, Object> p : pendingList(node))
            {
                handled.add(String.valueOf(p.get("userId")));
            }
            for (Map<String, Object> viewer : entry.getValue())
            {
                if (!handled.contains(String.valueOf(viewer.get("userId"))))
                {
                    viewedList(node).add(viewer);
                }
            }
        }
        return nodes;
    }

    /** 取（或新建）某节点的流程图人员信息容器 */
    @SuppressWarnings("unchecked")
    private Map<String, Object> nodeEntry(Map<String, Object> nodes, String nodeId, String name)
    {
        Map<String, Object> node = asNode(nodes.get(nodeId));
        if (node == null)
        {
            node = new java.util.LinkedHashMap<>();
            node.put("nodeId", nodeId);
            node.put("name", StringUtils.isNotEmpty(name) ? name : nodeId);
            node.put("done", new ArrayList<Map<String, Object>>());
            node.put("pending", new ArrayList<Map<String, Object>>());
            node.put("viewed", new ArrayList<Map<String, Object>>());
            nodes.put(nodeId, node);
        }
        return node;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asNode(Object raw)
    {
        return raw instanceof Map ? (Map<String, Object>) raw : null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> doneList(Map<String, Object> node)
    {
        return (List<Map<String, Object>>) node.get("done");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> pendingList(Map<String, Object> node)
    {
        return (List<Map<String, Object>>) node.get("pending");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> viewedList(Map<String, Object> node)
    {
        return (List<Map<String, Object>>) node.get("viewed");
    }

    /** 未审批操作人按人去重：同一人可能同时挂多条未办任务（会签/退回重办） */
    private void addPending(Map<String, Object> node, Map<String, Object> person)
    {
        String userId = String.valueOf(person.get("userId"));
        for (Map<String, Object> exist : pendingList(node))
        {
            if (userId.equals(String.valueOf(exist.get("userId"))))
            {
                return;
            }
        }
        pendingList(node).add(person);
    }

    /** 节点人员基础信息（userId 用字符串，与 Flowable assignee 保持一致） */
    private Map<String, Object> personEntry(String userId, Map<String, String> nameCache)
    {
        Map<String, Object> person = new java.util.LinkedHashMap<>();
        person.put("userId", userId);
        person.put("userName", nameCache.computeIfAbsent(userId, this::resolveUserNickname));
        return person;
    }

    @Override
    public void recordNodeView(String taskId)
    {
        if (StringUtils.isEmpty(taskId))
        {
            return;
        }
        try
        {
            HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId).singleResult();
            if (task == null || StringUtils.isEmpty(task.getProcessInstanceId()))
            {
                return;
            }
            Long userId = SecurityUtils.getUserId();
            flowNodeViewService.recordView(task.getProcessInstanceId(), task.getTaskDefinitionKey(), task.getId(),
                    userId, resolveUserNickname(String.valueOf(userId)));
        }
        catch (Exception e)
        {
            // 查看记录仅用于流程图展示，失败不影响办理
            log.debug("记录流程节点查看失败：{}", e.getMessage());
        }
    }

    /** 读取流程定义 BPMN XML（实例图由前端 bpmn-js 实时渲染，需 xml + bpmndi 图形段） */
    private String readProcessDefinitionXml(String processDefinitionId)
    {
        ProcessDefinition def = repositoryService.getProcessDefinition(processDefinitionId);
        if (def == null)
        {
            throw new ServiceException("流程定义不存在：" + processDefinitionId);
        }
        try (InputStream in = repositoryService.getResourceAsStream(def.getDeploymentId(), def.getResourceName()))
        {
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new ServiceException("读取流程图数据失败：" + e.getMessage());
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
        // 流程标题：发起时写入的实例名称（单据名-申请人-日期），为空回退流程定义名
        String instName = hpi.getName();
        fi.setTitle(StringUtils.isNotEmpty(instName) ? instName : hpi.getProcessDefinitionName());
        // 紧急程度：来自业务表系统字段 urgency（无业务表或未填时为 null）
        fi.setUrgency(resolveUrgency(hpi.getProcessDefinitionKey(), hpi.getBusinessKey()));
        // 流程编号：来自业务表系统字段 processNo（发起时按编号规则生成）
        fi.setProcessNo(resolveBusinessColumn(hpi.getProcessDefinitionKey(), hpi.getBusinessKey(), "processNo"));
        fi.setStartUserName(resolveUserNickname(hpi.getStartUserId()));
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
            fi.setDeleteReason(hpi.getDeleteReason());
            fi.setEndType(resolveEndType(hpi.getDeleteReason()));
        }
        else
        {
            fi.setStatus("completed");
            fi.setEndType("normal");
        }
        return fi;
    }

    /**
     * 解析流程中断方式：删除原因前缀由本服务写入（【adminEnd】/【adminCancel】/【terminate】），
     * 老数据的「发起人撤销」「办理人终止」按关键字兜底识别。
     */
    private String resolveEndType(String deleteReason)
    {
        if (StringUtils.isEmpty(deleteReason))
        {
            return "canceled";
        }
        if (deleteReason.contains(ADMIN_END_PREFIX))
        {
            return "forceEnd";
        }
        if (deleteReason.contains("终止"))
        {
            return "forceEnd";
        }
        return "canceled";
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

    /** 补充流程名称、业务主键、发起人、流程标题 */
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
                // 流程标题：发起时写入的实例名称（单据名-申请人-日期），为空回退流程定义名
                String instName = hpi.getName();
                ft.setTitle(StringUtils.isNotEmpty(instName) ? instName : hpi.getProcessDefinitionName());
                // 紧急程度：来自业务表系统字段 urgency（用于待办优先级展示/排序）
                ft.setUrgency(resolveUrgency(hpi.getProcessDefinitionKey(), hpi.getBusinessKey()));
                ft.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
                // 创建人：发起人用户ID转昵称
                if (StringUtils.isEmpty(ft.getStartUserName()) && StringUtils.isNotEmpty(hpi.getStartUserId()))
                {
                    ft.setStartUserName(resolveUserNickname(hpi.getStartUserId()));
                }
                // 流程状态 + 当前所在环节（已办列表展示「办理中/已办结/已撤销」）
                fillInstanceStatus(ft, hpi);
            }
        }
        catch (Exception ignored) { }
    }

    /**
     * 回填流程实例状态：running 进行中 / completed 已办结 / canceled 已撤销（带删除原因）
     * 并进行中的实例回填当前所在环节名称。
     */
    private void fillInstanceStatus(FlowTask ft, HistoricProcessInstance hpi)
    {
        if (hpi == null)
        {
            return;
        }
        if (hpi.getEndTime() == null)
        {
            ft.setStatus("running");
            List<HistoricActivityInstance> active = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(hpi.getId()).unfinished().list();
            if (!active.isEmpty())
            {
                ft.setCurrentTaskName(active.get(0).getActivityName());
            }
        }
        else if (StringUtils.isNotEmpty(hpi.getDeleteReason()))
        {
            ft.setStatus("canceled");
        }
        else
        {
            ft.setStatus("completed");
        }
    }

    /**
     * 解析流程的紧急程度：从流程绑定表单的业务表读取系统字段 urgency。
     * 无绑定表单、无业务主键或表未生成时返回 null（不阻断列表展示）。
     *
     * @param processDefinitionKey 流程定义Key
     * @param businessKey          业务数据行ID
     * @return 紧急程度值（1普通 2紧急 3特急），取不到返回 null
     */
    private String resolveUrgency(String processDefinitionKey, String businessKey)
    {
        return resolveBusinessColumn(processDefinitionKey, businessKey, "urgency");
    }

    /**
     * 读取业务表某一列的值（系统字段回显：紧急程度 / 流程编号）。
     * 无绑定表单、无业务主键、表未生成或缺列时返回 null（不阻断列表展示）。
     *
     * @param processDefinitionKey 流程定义Key
     * @param businessKey          业务数据行ID
     * @param column               列名（仅限平台内部常量，不来自用户输入）
     * @return 列值，取不到返回 null
     */
    private String resolveBusinessColumn(String processDefinitionKey, String businessKey, String column)
    {
        if (StringUtils.isEmpty(processDefinitionKey) || StringUtils.isEmpty(businessKey))
        {
            return null;
        }
        try
        {
            com.ruoyi.flowable.domain.FlowForm form =
                    flowFormService.selectFlowFormByProcessKey(processDefinitionKey);
            if (form == null || StringUtils.isEmpty(form.getTableName()))
            {
                return null;
            }
            Long cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                    Long.class, form.getTableName(), column);
            if (cnt == null || cnt == 0)
            {
                return null;
            }
            return jdbcTemplate.queryForObject(
                    "SELECT `" + column + "` FROM `" + form.getTableName() + "` WHERE id = ?", String.class, businessKey);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    /** 按用户ID解析昵称，失败返回原值 */
    private String resolveUserNickname(String userId)
    {
        try
        {
            Long id = Long.valueOf(userId);
            com.ruoyi.common.core.domain.entity.SysUser user = sysUserService.selectUserById(id);
            return user != null && StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : userId;
        }
        catch (Exception e)
        {
            return userId;
        }
    }

    /** 流程分类树：按最新版本流程定义按分类分组，count=当前用户该流程待办数，供待办/查询页左侧树使用 */
    @Override
    public List<Map<String, Object>> selectProcessTree()
    {
        Long userId = SecurityUtils.getUserId();
        List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .latestVersion().active().orderByProcessDefinitionCategory().asc()
                .orderByProcessDefinitionName().asc().list();
        List<Map<String, Object>> tree = new ArrayList<>();
        Map<String, Map<String, Object>> byCategory = new java.util.LinkedHashMap<>();
        for (ProcessDefinition def : defs)
        {
            String rawCategory = def.getCategory();
            // 空分类或 BPMN 命名空间归入「未分类」
            if (StringUtils.isEmpty(rawCategory) || rawCategory.contains("://"))
            {
                rawCategory = "未分类";
            }
            String category = rawCategory;
            Map<String, Object> node = byCategory.get(category);
            if (node == null)
            {
                node = new java.util.HashMap<>();
                node.put("id", category);
                String label = DictUtils.getDictLabel("flowable_category", category, ",");
                if (StringUtils.isEmpty(label))
                {
                    label = category;
                }
                node.put("label", label);
                node.put("children", new ArrayList<Map<String, Object>>());
                node.put("count", 0);
                byCategory.put(category, node);
            }
            long cnt = taskService.createTaskQuery().taskAssignee(String.valueOf(userId))
                    .processDefinitionKey(def.getKey()).count();
            Map<String, Object> leaf = new java.util.HashMap<>();
            leaf.put("id", def.getKey());
            leaf.put("label", def.getName());
            leaf.put("key", def.getKey());
            leaf.put("count", cnt);
            ((List<Map<String, Object>>) node.get("children")).add(leaf);
            node.put("count", (int) node.get("count") + (int) cnt);
        }
        tree.addAll(byCategory.values());
        return tree;
    }
}
