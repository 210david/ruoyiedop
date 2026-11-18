package com.ruoyi.flowable.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import com.ruoyi.common.event.FlowFormSubmitEvent;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.flowable.service.FlowArchiveHandler;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.flowable.config.FlowReturnConstants;
import com.ruoyi.flowable.config.FlowSystemFields;
import com.ruoyi.flowable.domain.FlowCc;
import com.ruoyi.flowable.domain.FlowDelegate;
import com.ruoyi.flowable.domain.FlowDraft;
import com.ruoyi.flowable.domain.FlowInstance;
import com.ruoyi.flowable.domain.FlowTask;
import com.ruoyi.flowable.domain.dto.FlowCompleteDTO;
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

    /**
     * 管理员干预批注解析。
     *
     * <p>新格式：{@code 【intervene】管理员干预「来源节点」→「目标节点」，指定办理人「张三」：说明}；
     * 旧格式：{@code 【intervene】管理员干预至「目标节点」：说明}（无来源节点与指定办理人）。
     * 两种格式都要能还原成流程状态/流程图里的干预记录。</p>
     */
    private static final Pattern INTERVENE_MSG_PATTERN = Pattern.compile(
            "^【intervene】管理员干预(?:至)?「([^」]+)」(?:→「([^」]+)」)?(?:，指定办理人「([^」]*)」)?(?:：(.*))?$",
            Pattern.DOTALL);

    /** 干预批注与任务结束时间允许的误差（毫秒）：用于还原「哪个节点被干预跳过」 */
    private static final long INTERVENE_TIME_TOLERANCE = 5000L;

    /** 管理员复活已结束流程的批注前缀（复活 = 重建运行实例并停在指定节点） */
    private static final String ADMIN_REVIVE_PREFIX = "【revive】";

    /** 节点「强制收回」扩展属性：flowable:allowRecall（未配置默认允许） */
    private static final String ATTR_ALLOW_RECALL = "allowRecall";

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
    private com.ruoyi.system.service.ISysUserService sysUserService;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private com.ruoyi.flowable.service.IFlowFormService flowFormService;

    /** 表单规则引擎：办理提交时复算 + 校验 */
    @Autowired
    private com.ruoyi.flowable.service.FlowFormRuleService formRuleService;

    /** 流程定义版本解析（活动版本优先） */
    @Autowired
    private com.ruoyi.flowable.service.FlowDefinitionResolver flowDefinitionResolver;

    /** 流程图「已查看人员」记录（泛微式：谁打开过该节点但还没办理） */
    @Autowired
    private com.ruoyi.flowable.service.FlowNodeViewService flowNodeViewService;

    /** 流程请求登记（流程实例 -> 短数字流程编号） */
    @Autowired
    private com.ruoyi.flowable.service.FlowRequestService flowRequestService;

    /** 我的待阅（待办页「待阅」/「全部」页签口径） */
    @Autowired
    private com.ruoyi.flowable.service.IFlowCcService flowCcService;

    /** 流程转办记录（转办过程留痕，流程状态/流程图展示用） */
    @Autowired
    private com.ruoyi.flowable.mapper.FlowDelegateMapper flowDelegateMapper;

    /** 我的草稿（待办页「草稿」/「全部」页签口径） */
    @Autowired
    private com.ruoyi.flowable.service.IFlowDraftService flowDraftService;

    /** 流程模型（设计器最新版优先，节点字段权限读取与前端 fieldPerms 口径一致） */
    @Autowired
    private com.ruoyi.flowable.service.FlowRuntimeModelService flowRuntimeModelService;

    /** 审批意见类型标识 */

    @Override
    public List<FlowInstance> selectMyProcessList(FlowInstance query)
    {
        Long userId = SecurityUtils.getUserId();
        HistoricProcessInstanceQuery hisQuery = historyService.createHistoricProcessInstanceQuery()
                .startedBy(String.valueOf(userId))
                .orderByProcessInstanceStartTime().desc();
        if (query.getRequestId() != null)
        {
            // 流程编号（短数字）反查引擎实例ID，未登记时按数字实例ID兜底
            String requestInstanceId = flowRequestService.instanceIdOf(query.getRequestId());
            hisQuery.processInstanceId(StringUtils.isNotEmpty(requestInstanceId)
                    ? requestInstanceId : String.valueOf(query.getRequestId()));
        }
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

    /** 流程实例列表内存过滤：流程标题/发起人/发起时间区间/已完成、已撤销状态（Flowable 原生查询不支持的条件） */
    private boolean matchInstanceFilter(FlowInstance fi, FlowInstance query)
    {
        if ("canceled".equals(query.getStatus())
                && !"canceled".equals(fi.getStatus()))
        {
            return false;
        }
        // 已结束的实例里包含「正常办结」与「已撤销」，两者在列表/页签上分开统计，
        // 这里必须再按实例状态精确过滤，否则「已完成」页签会把已撤销的流程也算进去
        if ("completed".equals(query.getStatus())
                && !"completed".equals(fi.getStatus()))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(query.getTitle())
                && (fi.getTitle() == null || !fi.getTitle().contains(query.getTitle())))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(query.getProcessNo())
                && (fi.getProcessNo() == null || !fi.getProcessNo().contains(query.getProcessNo())))
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
        String uid = String.valueOf(userId);
        // 被抄送也是参与：抄送给我的流程必须在「流程查询」里能查到，
        // 否则李华只能从待阅入口进入，查询不到该流程
        java.util.Set<String> ccInstanceIds = new java.util.LinkedHashSet<>();
        for (FlowCc cc : flowCcService.selectMyCcList(new FlowCc()))
        {
            if (StringUtils.isNotEmpty(cc.getInstanceId()))
            {
                ccInstanceIds.add(cc.getInstanceId());
            }
        }
        // 我转办出去的流程同样算参与（转办后任务虽已交给他人，但仍应能查询到）
        for (FlowDelegate d : flowDelegateMapper.selectByFromUser(userId))
        {
            if (StringUtils.isNotEmpty(d.getInstanceId()))
            {
                ccInstanceIds.add(d.getInstanceId());
            }
        }
        HistoricProcessInstanceQuery hisQuery = historyService.createHistoricProcessInstanceQuery();
        if (ccInstanceIds.isEmpty())
        {
            hisQuery.involvedUser(uid);
        }
        else
        {
            // 我发起/审批过的 OR 抄送给我（两种参与方式取并集）
            hisQuery.or().involvedUser(uid).processInstanceIds(ccInstanceIds).endOr();
        }
        hisQuery.orderByProcessInstanceStartTime().desc();
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
        if (query.getRequestId() != null)
        {
            // 流程编号（短数字）反查引擎实例ID，未登记时按数字实例ID兜底
            String requestInstanceId = flowRequestService.instanceIdOf(query.getRequestId());
            hisQuery.processInstanceId(StringUtils.isNotEmpty(requestInstanceId)
                    ? requestInstanceId : String.valueOf(query.getRequestId()));
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
        if (query.getRequestId() != null)
        {
            // 流程编号（短数字）反查引擎实例ID，未登记时按数字实例ID兜底
            String requestInstanceId = flowRequestService.instanceIdOf(query.getRequestId());
            hisQuery.processInstanceId(StringUtils.isNotEmpty(requestInstanceId)
                    ? requestInstanceId : String.valueOf(query.getRequestId()));
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
                activeNames.add(taskDisplayName(hpi.getProcessDefinitionId(), id,
                        el == null || el.getName() == null ? id : el.getName()));
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
                m.put("taskName", taskDisplayName(task.getProcessDefinitionId(), task.getTaskDefinitionKey(), task.getName()));
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
            m.put("name", taskDisplayName(hpi.getProcessDefinitionId(), ut.getId(),
                    StringUtils.isNotEmpty(ut.getName()) ? ut.getName() : ut.getId()));
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
        // 干预批注带上「来源节点 → 目标节点」，流程状态/流程图据此还原整条干预轨迹
        StringBuilder fromNames = new StringBuilder();
        for (String id : fromIds)
        {
            FlowElement fromEl = model.getFlowElement(id);
            String name = fromEl != null && StringUtils.isNotEmpty(fromEl.getName()) ? fromEl.getName() : id;
            if (fromNames.length() > 0)
            {
                fromNames.append("、");
            }
            fromNames.append(name);
        }
        StringBuilder msg = new StringBuilder(ADMIN_INTERVENE_PREFIX).append("管理员干预");
        if (!sameNode && fromNames.length() > 0)
        {
            msg.append("「").append(fromNames).append("」→「").append(targetName).append("」");
        }
        else
        {
            msg.append("至「").append(targetName).append("」");
        }
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
        // 回到发起人节点时对外显示开始节点名称（「发起申请」），不暴露内部的「重新提交」
        String prevName = isInitiatorNode(prevTask) ? startEventDisplayName(model)
                : StringUtils.isNotEmpty(prevTask.getName()) ? prevTask.getName() : prevId;
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
     * 我的已办「收回」：下一节点办理前，本节点办理人把流程收回重新办理。
     */
    @Override
    @Transactional
    public Map<String, Object> recallMyInstance(String instanceId, String reason)
    {
        if (!canRecallMyInstance(instanceId))
        {
            throw new ServiceException("当前无法收回：需在下一节点办理前，且本节点已开启强制收回");
        }
        return recallInstance(instanceId, reason);
    }

    @Override
    public boolean canRecallInstance(String instanceId)
    {
        try
        {
            return canRecallMyInstance(instanceId);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * 我的已办收回校验：满足以下任一情形即可收回。
     *
     * <p>情形一（中间环节）：① 流程仍在运行；② 我是该实例最近一次的办理人；
     * ③ 该节点开启了强制收回；④ 我办理之后没有任何任务办结（下一节点尚未办理）；
     * ⑤ 我办理的节点是当前待办节点的直接上一环节。</p>
     *
     * <p>情形二（发起环节）：① 流程仍在运行；② 我是流程发起人；③ 开始节点开启了强制收回；
     * ④ 我提交之后没有任何任务办结（下一审批节点尚未办理）；
     * ⑤ 当前待办节点直接挂在开始节点之后。</p>
     */
    private boolean canRecallMyInstance(String instanceId)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            return false;
        }
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (instance == null)
        {
            return false;
        }
        BpmnModel model = repositoryService.getBpmnModel(instance.getProcessDefinitionId());
        if (model == null)
        {
            return false;
        }
        String uid = String.valueOf(SecurityUtils.getUserId());
        List<HistoricTaskInstance> myTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId).taskAssignee(uid).finished()
                .orderByHistoricTaskInstanceEndTime().desc().listPage(0, 1);
        // 我还没办结过任何任务：可能是我刚发起、下一审批环节还没办理，此时允许收回发起
        if (myTasks.isEmpty())
        {
            return canRecallInitiatorSubmit(instance, model, uid);
        }
        HistoricTaskInstance mine = myTasks.get(0);
        if (!(model.getFlowElement(mine.getTaskDefinitionKey()) instanceof UserTask))
        {
            return false;
        }
        // 节点级开关：设计器未配置时默认允许收回
        if (!allowRecall(model.getFlowElement(mine.getTaskDefinitionKey())))
        {
            return false;
        }
        // 下一节点是否已办理：我办理之后只要还有任务办结，流程就已往前走，不能再收回
        for (HistoricTaskInstance h : historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId).finished().list())
        {
            if (h.getId().equals(mine.getId()) || h.getEndTime() == null || mine.getEndTime() == null)
            {
                continue;
            }
            if (h.getEndTime().after(mine.getEndTime()))
            {
                return false;
            }
        }
        // 当前待办节点必须直接挂在我办理的节点后面
        List<String> currentIds = new ArrayList<>();
        for (Task t : taskService.createTaskQuery().processInstanceId(instanceId).list())
        {
            String key = t.getTaskDefinitionKey();
            if (StringUtils.isNotEmpty(key) && !currentIds.contains(key))
            {
                currentIds.add(key);
            }
        }
        return !currentIds.isEmpty()
                && findUpstreamUserTasks(model, currentIds).contains(mine.getTaskDefinitionKey());
    }

    /**
     * 发起环节收回校验：我提交后、下一审批节点尚未办理时，收回「发起申请」重新提交。
     *
     * <p>Flowable 无法把执行流停在开始事件上，收回目标取部署时注入的发起人节点（applyNode）；
     * 早期部署的流程没有该节点，收回后无处可去，因此不提供收回。</p>
     */
    private boolean canRecallInitiatorSubmit(ProcessInstance instance, BpmnModel model, String uid)
    {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instance.getId()).singleResult();
        if (hpi == null || !uid.equals(hpi.getStartUserId()))
        {
            return false;
        }
        UserTask initiatorNode = findInitiatorNode(model);
        if (initiatorNode == null)
        {
            return false;
        }
        // 开始节点的强制收回开关（设计器配在开始事件上），未配置默认允许
        if (!allowInitiatorRecall(model, initiatorNode))
        {
            return false;
        }
        // 下一审批节点是否已办理：提交之后只要还有任务办结，就不能再收回发起
        if (historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instance.getId()).finished().count() > 0)
        {
            return false;
        }
        // 当前待办节点必须直接挂在开始节点后面
        List<String> currentIds = new ArrayList<>();
        for (Task t : taskService.createTaskQuery().processInstanceId(instance.getId()).list())
        {
            String key = t.getTaskDefinitionKey();
            if (StringUtils.isNotEmpty(key) && !currentIds.contains(key))
            {
                currentIds.add(key);
            }
        }
        return !currentIds.isEmpty()
                && findUpstreamUserTasks(model, currentIds).contains(initiatorNode.getId());
    }

    /** 开始节点是否允许收回发起：开关配在开始事件上，未配置时回落到发起人节点配置（默认允许） */
    private boolean allowInitiatorRecall(BpmnModel model, UserTask initiatorNode)
    {
        if (model.getMainProcess() != null)
        {
            for (org.flowable.bpmn.model.StartEvent start
                    : model.getMainProcess().findFlowElementsOfType(org.flowable.bpmn.model.StartEvent.class, true))
            {
                Map<String, java.util.List<org.flowable.bpmn.model.ExtensionAttribute>> attrs = start.getAttributes();
                if (attrs != null && attrs.containsKey(ATTR_ALLOW_RECALL))
                {
                    return allowRecall(start);
                }
            }
        }
        return allowRecall(initiatorNode);
    }

    /** 节点是否允许办理人收回（flowable:allowRecall，未配置默认允许） */
    private boolean allowRecall(FlowElement element)
    {
        Map<String, java.util.List<org.flowable.bpmn.model.ExtensionAttribute>> attrs = element.getAttributes();
        if (attrs == null)
        {
            return true;
        }
        java.util.List<org.flowable.bpmn.model.ExtensionAttribute> list = attrs.get(ATTR_ALLOW_RECALL);
        if (list == null || list.isEmpty())
        {
            return true;
        }
        String value = list.get(0).getValue();
        return !("false".equalsIgnoreCase(value) || "0".equals(value) || "否".equals(value));
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
        String processKey = instance.getProcessDefinitionKey();
        String businessKey = instance.getBusinessKey();
        runtimeService.deleteProcessInstance(instanceId, prefix + msg);
        // 主动分发终止回调：Flowable 的 PROCESS_CANCELLED 事件在删除实例时并不总会触发，
        // 只靠事件会导致台账停在「审批中」、请假冻结额度不释放
        dispatchCancelled(processKey, instanceId, businessKey);
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
        ProcessInstance running = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId)
                .singleResult();
        if (running != null)
        {
            runtimeService.deleteProcessInstance(instanceId, "【adminDelete】管理员删除流程实例");
            // 删除实例同样要通知业务台账关闭，否则台账会永远停在「审批中」
            dispatchCancelled(running.getProcessDefinitionKey(), instanceId, running.getBusinessKey());
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
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        String processKey = processInstance == null ? null : processInstance.getProcessDefinitionKey();
        String businessKey = processInstance == null ? null : processInstance.getBusinessKey();
        runtimeService.deleteProcessInstance(instanceId, msg);
        // 流程终止后同步业务台账状态（Flowable 的 PROCESS_CANCELLED 事件在部分场景不触发，这里主动分发）
        dispatchCancelled(processKey, instanceId, businessKey);
        writeBackLeaveStatus(instanceId, "4", msg);
        return 1;
    }

    /**
     * 主动分发「流程终止」回调给所有业务归档处理器
     *
     * Flowable 的 PROCESS_CANCELLED 事件在删除实例的多种路径下不会触发，
     * 因此凡是由我们主动删除流程实例的地方，都在删除后调用本方法兜底，
     * 保证台账状态（已撤销）与冻结额度能同步释放。业务处理器需自行保证幂等。
     */
    private void dispatchCancelled(String processKey, String instanceId, String businessKey)
    {
        if (StringUtils.isEmpty(processKey))
        {
            return;
        }
        for (FlowArchiveHandler handler : SpringUtils.getBeansOfType(FlowArchiveHandler.class).values())
        {
            try
            {
                handler.cancelled(processKey, instanceId, businessKey);
            }
            catch (Exception e)
            {
                log.error("流程终止归档失败：processKey={}, instanceId={}, handler={}, error={}", processKey,
                        instanceId, handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
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
            // 取该任务的审批意见（type=comment），跳过转办/抄送等操作留痕，解析【outcome】前缀
            applyTaskComments(ft, taskService.getTaskComments(task.getId()));
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
        // 3）抄送我已阅的流程：抄送本身不是办理，但「已阅」是一次处理动作，
        //    按泛微口径应在已办中可查（环节显示「XX（抄送）」，办理时间取阅读时间）
        for (FlowCc cc : flowCcService.selectMyCcList(new FlowCc()))
        {
            if (!"1".equals(cc.getReadFlag()) || cc.getReadTime() == null)
            {
                continue;
            }
            if (rowMap.containsKey(cc.getInstanceId()))
            {
                continue;
            }
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(cc.getInstanceId()).singleResult();
            if (hpi == null)
            {
                continue;
            }
            if (StringUtils.isNotEmpty(query.getProcessDefinitionKey())
                    && !query.getProcessDefinitionKey().equals(hpi.getProcessDefinitionKey()))
            {
                continue;
            }
            if (StringUtils.isNotEmpty(query.getProcessDefinitionName())
                    && (hpi.getProcessDefinitionName() == null
                            || !hpi.getProcessDefinitionName().contains(query.getProcessDefinitionName())))
            {
                continue;
            }
            FlowTask ft = new FlowTask();
            ft.setInstanceId(hpi.getId());
            ft.setProcessDefinitionId(hpi.getProcessDefinitionId());
            ft.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
            ft.setProcessDefinitionName(hpi.getProcessDefinitionName());
            ft.setBusinessKey(hpi.getBusinessKey());
            ft.setTitle(StringUtils.isNotEmpty(hpi.getName()) ? hpi.getName()
                    : (StringUtils.isNotEmpty(cc.getTitle()) ? cc.getTitle() : hpi.getProcessDefinitionName()));
            ft.setStartUserName(resolveUserNickname(hpi.getStartUserId()));
            ft.setUrgency(resolveUrgency(hpi.getProcessDefinitionKey(), hpi.getBusinessKey()));
            ft.setTaskName(StringUtils.isNotEmpty(cc.getTaskName()) ? cc.getTaskName() + "（抄送）" : "抄送");
            ft.setOutcome("cc");
            ft.setCreateTime(cc.getCreateTime());
            ft.setEndTime(cc.getReadTime());
            ft.setAssigneeId(userId);
            ft.setAssigneeName(resolveUserNickname(uid));
            ft.setReceiverName(cc.getSenderName());
            fillInstanceStatus(ft, hpi);
            if (!matchDoneFilter(ft, query))
            {
                continue;
            }
            rowMap.putIfAbsent(cc.getInstanceId(), ft);
        }
        // 4）我转办过的流程：转办也是一次处理动作（任务已交给他人继续办理），已办中可查
        for (FlowDelegate d : flowDelegateMapper.selectByFromUser(userId))
        {
            if (rowMap.containsKey(d.getInstanceId()))
            {
                continue;
            }
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(d.getInstanceId()).singleResult();
            if (hpi == null)
            {
                continue;
            }
            if (StringUtils.isNotEmpty(query.getProcessDefinitionKey())
                    && !query.getProcessDefinitionKey().equals(hpi.getProcessDefinitionKey()))
            {
                continue;
            }
            if (StringUtils.isNotEmpty(query.getProcessDefinitionName())
                    && (hpi.getProcessDefinitionName() == null
                            || !hpi.getProcessDefinitionName().contains(query.getProcessDefinitionName())))
            {
                continue;
            }
            FlowTask ft = new FlowTask();
            ft.setInstanceId(hpi.getId());
            ft.setProcessDefinitionId(hpi.getProcessDefinitionId());
            ft.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
            ft.setProcessDefinitionName(hpi.getProcessDefinitionName());
            ft.setBusinessKey(hpi.getBusinessKey());
            ft.setTitle(hpi.getName());
            ft.setStartUserName(resolveUserNickname(hpi.getStartUserId()));
            ft.setUrgency(resolveUrgency(hpi.getProcessDefinitionKey(), hpi.getBusinessKey()));
            ft.setTaskName(StringUtils.isNotEmpty(d.getTaskName()) ? d.getTaskName() + "（转办）" : "转办");
            ft.setOutcome("delegate");
            ft.setCreateTime(d.getCreateTime());
            ft.setEndTime(d.getCreateTime());
            ft.setAssigneeId(userId);
            ft.setAssigneeName(resolveUserNickname(uid));
            ft.setReceiverName(d.getToName());
            ft.setComment(d.getComment());
            fillInstanceStatus(ft, hpi);
            if (!matchDoneFilter(ft, query))
            {
                continue;
            }
            rowMap.putIfAbsent(d.getInstanceId(), ft);
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
        // 表单规则：办理提交前按同一份规则复算 + 校验，防止绕过前端
        applyFormRules(task, dto, false);
        // 联动事件数据准备：合并任务变量与本次提交变量快照（办理后任务变量随实例清空）
        Map<String, Object> mergedVars = new HashMap<>(taskService.getVariables(task.getId()));
        if (dto.getVariables() != null)
        {
            mergedVars.putAll(dto.getVariables());
        }
        org.flowable.engine.runtime.ProcessInstance runningPi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        String businessKey = runningPi == null ? null : runningPi.getBusinessKey();
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
        // 发布表单提交事件：业务模块（OA车辆/司机/印章等）监听后写回关联关系，失败不影响办理
        publishFormSubmitEvent(FlowFormSubmitEvent.PHASE_COMPLETE, task.getProcessDefinitionId(),
                task.getProcessInstanceId(), businessKey, mergedVars,
                task.getTaskDefinitionKey(), task.getName());
        return 1;
    }

    /**
     * 暂存表单修改（退回发起人场景）：只把可编辑字段写入流程变量，不办理、不流转、不写意见。
     * 校验当前办理人，防止越权改别人手上的流程数据。
     */
    @Override
    @Transactional
    public int saveTaskForm(FlowCompleteDTO dto)
    {
        if (dto.getVariables() == null || dto.getVariables().isEmpty())
        {
            throw new ServiceException("没有需要保存的表单数据");
        }
        Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
        if (task == null)
        {
            throw new ServiceException("任务不存在或已办理");
        }
        Long userId = SecurityUtils.getUserId();
        if (!String.valueOf(userId).equals(task.getAssignee()))
        {
            long candidate = taskService.createTaskQuery().taskId(dto.getTaskId())
                    .taskCandidateUser(String.valueOf(userId)).count();
            if (candidate == 0)
            {
                throw new ServiceException("您不是该任务的办理人或候选人");
            }
            taskService.claim(dto.getTaskId(), String.valueOf(userId));
        }
        // 暂存也复算 derive 规则（合计/人民币大写等），保证计算值随变量落库；
        // 但跳过必填与校验拦截——暂存允许未填完，提交时才做完整校验
        applyFormRules(task, dto, true);
        taskService.setVariables(dto.getTaskId(), dto.getVariables());
        // 暂存也发布联动事件（调度指派车辆/驾驶员暂存后即可写回关联），失败不影响暂存
        Map<String, Object> savedVars = new HashMap<>(taskService.getVariables(task.getId()));
        org.flowable.engine.runtime.ProcessInstance savePi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        publishFormSubmitEvent(FlowFormSubmitEvent.PHASE_SAVE, task.getProcessDefinitionId(),
                task.getProcessInstanceId(), savePi == null ? null : savePi.getBusinessKey(), savedVars,
                task.getTaskDefinitionKey(), task.getName());
        return 1;
    }

    /**
     * 发布流程表单提交联动事件。
     * 业务模块（如 OA 车辆/司机/印章台账）通过 AFTER_COMMIT 监听写回关联关系，
     * flowable 模块不反向依赖业务模块；事件发布/监听链路任何异常都不影响流程主流程。
     */
    private void publishFormSubmitEvent(String phase, String processDefinitionId, String processInstanceId,
            String businessKey, Map<String, Object> variables)
    {
        publishFormSubmitEvent(phase, processDefinitionId, processInstanceId, businessKey, variables, null, null);
    }

    /**
     * 发布流程表单提交联动事件（携带当前办理节点信息，供业务侧按节点判定状态流转）。
     */
    private void publishFormSubmitEvent(String phase, String processDefinitionId, String processInstanceId,
            String businessKey, Map<String, Object> variables, String taskDefinitionKey, String taskName)
    {
        try
        {
            org.flowable.engine.repository.ProcessDefinition pd = repositoryService.getProcessDefinition(processDefinitionId);
            if (pd == null)
            {
                return;
            }
            com.ruoyi.flowable.domain.FlowForm form = flowFormService.selectFlowFormByProcessKey(pd.getKey());
            SpringUtils.publishEvent(new FlowFormSubmitEvent(this, phase, pd.getKey(),
                    form == null ? null : form.getFormId(), processInstanceId, businessKey,
                    form == null ? null : form.getTableName(), variables, taskDefinitionKey, taskName));
        }
        catch (Exception e)
        {
            log.warn("流程表单提交联动事件发布失败：{}", e.getMessage());
        }
    }

    /**
     * 办理提交前执行表单规则：合并“已有变量 + 本次提交变量”，复算后写回本次变量。
     * 校验失败直接抛 ServiceException，流程不会推进。
     */
    private void applyFormRules(Task task, FlowCompleteDTO dto, boolean skipValidate)
    {
        if (dto.getVariables() == null)
        {
            dto.setVariables(new HashMap<>());
        }
        ProcessDefinition pd = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        if (pd == null)
        {
            return;
        }
        com.ruoyi.flowable.domain.FlowForm form = flowFormService.selectFlowFormByProcessKey(pd.getKey());
        if (form == null || StringUtils.isEmpty(form.getExtJson()))
        {
            return;
        }
        // 节点编辑上下文门控（对标泛微「节点表单设计器内实现联动」）：
        // 发起人（申请填单）节点始终执行；普通办理节点仅在节点字段权限配置了可编辑字段时执行；
        // 纯审批只读节点跳过规则复算与校验——后续节点是否可编辑/必填由节点表单设置控制
        if (!isNodeEditableContext(task))
        {
            return;
        }
        Map<String, Object> merged = new HashMap<>(taskService.getVariables(task.getId()));
        merged.putAll(dto.getVariables());
        formRuleService.applyAndValidate(form.getExtJson(), form.getFieldsJson(), merged, skipValidate);
        for (Map.Entry<String, Object> e : merged.entrySet())
        {
            if (!"detailData".equals(e.getKey()))
            {
                dto.getVariables().put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * 当前任务节点是否处于「可编辑上下文」：
     * 发起人节点（含退回重填）视为全可编辑；普通节点按 BPMN 扩展属性 fieldPerms 判断，
     * 任一字段配为 edit 即视为可编辑节点，未配置或全只读则跳过表单规则。
     */
    private boolean isNodeEditableContext(Task task)
    {
        if (FlowReturnConstants.INITIATOR_NODE_ID.equals(task.getTaskDefinitionKey()))
        {
            return true;
        }
        try
        {
            org.flowable.bpmn.model.BpmnModel model = flowRuntimeModelService.getByDefinitionId(task.getProcessDefinitionId());
            if (model == null || model.getMainProcess() == null)
            {
                return false;
            }
            org.flowable.bpmn.model.FlowElement el = model.getFlowElement(task.getTaskDefinitionKey());
            if (el == null)
            {
                return false;
            }
            // 开始事件视为发起上下文（部分流程发起人任务 key 即开始事件 id）
            if (el instanceof org.flowable.bpmn.model.StartEvent)
            {
                return true;
            }
            String permJson = readNodeAttr(el, "fieldPerms");
            if (StringUtils.isEmpty(permJson))
            {
                return false;
            }
            com.alibaba.fastjson2.JSONObject perms = com.alibaba.fastjson2.JSON.parseObject(permJson);
            return perms != null && perms.containsValue("edit");
        }
        catch (Exception e)
        {
            log.warn("读取节点字段权限失败，按只读节点跳过表单规则: {}", task.getTaskDefinitionKey(), e);
            return false;
        }
    }

    /** 读取节点上的扩展属性值（BPMN 中形如 flowable:fieldPerms），与 FlowTaskController 同口径 */
    private String readNodeAttr(org.flowable.bpmn.model.FlowElement task, String name)
    {
        Map<String, java.util.List<org.flowable.bpmn.model.ExtensionAttribute>> attrs = task.getAttributes();
        if (attrs == null)
        {
            return "";
        }
        java.util.List<org.flowable.bpmn.model.ExtensionAttribute> list = attrs.get(name);
        if (list == null || list.isEmpty())
        {
            return "";
        }
        String value = list.get(0).getValue();
        return value == null ? "" : value;
    }

    /**
     * 驳回（退回）到指定节点：把执行流从当前节点挪回前置节点，该节点办理人重新出现在待办里。
     * 退回意见必须在跳转前写入（跳转后当前任务被删除）；并行/会签的多个当前节点一起挪；
     * 跳转后的新任务打上任务级变量 flowReturned=1，前端据此归到「退回」页签。
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
        // 安全校验：只允许退回「当前环节之前已经走过的环节」或「发起人（申请）节点」，防止前端乱传节点ID
        java.util.Set<String> upstream = upstreamActivityIds(model, task.getTaskDefinitionKey());
        if (!upstream.contains(targetId))
        {
            throw new ServiceException("只能退回到当前环节之前已经办理过的环节");
        }
        boolean initiatorNode = isInitiatorNode(targetTask);
        if (!initiatorNode && !isVisitedUserTask(instanceId, targetId))
        {
            throw new ServiceException("只能退回到已经流转过的环节");
        }
        String targetName = initiatorNode ? startEventDisplayName(model)
                : StringUtils.isNotEmpty(targetTask.getName()) ? targetTask.getName() : targetId;
        String fromName = taskDisplayName(task.getProcessDefinitionId(), task.getTaskDefinitionKey(),
                StringUtils.isNotEmpty(task.getName()) ? task.getName() : task.getTaskDefinitionKey());
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
        // 只允许退回到当前环节的上游（之前流经的环节），不允许往后面的节点退
        java.util.Set<String> upstream = upstreamActivityIds(model, currentKey);
        // 1) 发起人（申请）节点：部署时注入的「发起人（重新提交）」节点，正常发起不经过，只作为退回目标
        UserTask initiatorNode = findInitiatorNode(model);
        if (initiatorNode != null && !initiatorNode.getId().equals(currentKey)
                && upstream.contains(initiatorNode.getId()))
        {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", initiatorNode.getId());
            m.put("name", startEventDisplayName(model));
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
                if (StringUtils.isEmpty(id) || id.equals(currentKey) || visited.containsKey(id)
                        || !upstream.contains(id))
                {
                    continue;
                }
                // 发起人节点已作为「开始」单独放在首位，历史里不要再重复出一条「重新提交」
                if (FlowReturnConstants.INITIATOR_NODE_ID.equals(id))
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

    /** 计算当前节点的所有上游节点ID（沿连线向前回溯），用于保证「只能往前面退」 */
    private java.util.Set<String> upstreamActivityIds(BpmnModel model, String currentKey)
    {
        java.util.Set<String> result = new java.util.HashSet<>();
        FlowElement current = model.getFlowElement(currentKey);
        if (!(current instanceof org.flowable.bpmn.model.FlowNode))
        {
            return result;
        }
        java.util.Deque<String> stack = new java.util.ArrayDeque<>();
        for (org.flowable.bpmn.model.SequenceFlow sf
                : ((org.flowable.bpmn.model.FlowNode) current).getIncomingFlows())
        {
            stack.push(sf.getSourceRef());
        }
        while (!stack.isEmpty())
        {
            String id = stack.pop();
            if (StringUtils.isEmpty(id) || !result.add(id))
            {
                continue;
            }
            FlowElement element = model.getFlowElement(id);
            if (element instanceof org.flowable.bpmn.model.FlowNode)
            {
                for (org.flowable.bpmn.model.SequenceFlow sf
                        : ((org.flowable.bpmn.model.FlowNode) element).getIncomingFlows())
                {
                    stack.push(sf.getSourceRef());
                }
            }
        }
        return result;
    }

    /** 退回选项对用户显示原开始事件名称，不暴露内部的「重新提交」技术节点 */
    private String startEventDisplayName(BpmnModel model)
    {
        if (model.getMainProcess() != null)
        {
            for (org.flowable.bpmn.model.StartEvent start
                    : model.getMainProcess().findFlowElementsOfType(org.flowable.bpmn.model.StartEvent.class, true))
            {
                if (StringUtils.isNotEmpty(start.getName()))
                {
                    return start.getName();
                }
            }
        }
        return "开始";
    }

    /** 任务显示名称：发起人（申请）节点统一显示开始节点名称，不向用户暴露「重新提交」技术节点 */
    private String taskDisplayName(String processDefinitionId, String taskDefinitionKey, String rawName)
    {
        if (!FlowReturnConstants.INITIATOR_NODE_ID.equals(taskDefinitionKey))
        {
            return rawName;
        }
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        return model == null ? rawName : startEventDisplayName(model);
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
        // 转办过程留痕：谁（原办理人）在什么时间把哪个环节转给了谁，供流程状态/流程图回溯
        FlowDelegate record = new FlowDelegate();
        record.setInstanceId(task.getProcessInstanceId());
        record.setTaskId(taskId);
        record.setTaskName(task.getName());
        record.setFromId(SecurityUtils.getUserId());
        record.setFromName(resolveUserNickname(currentUserId));
        record.setToId(targetUserId);
        record.setToName(resolveUserNickname(String.valueOf(targetUserId)));
        record.setComment(comment == null || comment.length() <= 500 ? comment : comment.substring(0, 500));
        flowDelegateMapper.insertDelegate(record);
        return 1;
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
        // 转办记录：按任务归并，转办过的任务，被转办人的接收时间从「转办时刻」起算
        Map<String, List<FlowDelegate>> delegatesByTask = new HashMap<>();
        for (FlowDelegate d : flowDelegateMapper.selectByInstance(instanceId))
        {
            if (StringUtils.isNotEmpty(d.getTaskId()))
            {
                delegatesByTask.computeIfAbsent(d.getTaskId(), k -> new ArrayList<>()).add(d);
            }
        }
        // 管理员干预：批注还原为「干预」操作行，并把被跳过的节点标记出来
        List<Map<String, Object>> interveneRecords = selectInterveneRecords(instanceId);
        Set<String> skippedTaskIds = resolveInterveneSkippedTaskIds(tasks, interveneRecords);
        List<FlowTask> list = new ArrayList<>();
        // 上一个非发起人节点的办理结果：用于判断发起人节点的再次提交是不是「退回后重新提交」
        String prevNonInitiatorOutcome = null;
        // 发起节点（开始事件）：发起人=实例发起人，接收/操作时间=实例开始时间
        if (hpi != null)
        {
            FlowTask start = new FlowTask();
            start.setTaskId("__start__");
            // 与退回后的发起人节点保持一致，统一显示开始节点的名称
            BpmnModel startModel = repositoryService.getBpmnModel(hpi.getProcessDefinitionId());
            start.setTaskName(startModel == null ? "开始" : startEventDisplayName(startModel));
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
            // 任务被转办过：当前办理人从最后一次转办时刻接手，接收时间与耗时都从那一刻算
            List<FlowDelegate> transfers = delegatesByTask.get(task.getId());
            if (transfers != null && !transfers.isEmpty())
            {
                Date lastTransfer = transfers.get(transfers.size() - 1).getCreateTime();
                if (lastTransfer != null)
                {
                    ft.setCreateTime(lastTransfer);
                }
            }
            // 一个任务可能有多条批注（转办 + 最终办理意见），取最新的一条办理意见；
            // 转办/抄送属于操作留痕，已由 __delegate__ / __cc__ 行单独展示，不能覆盖签字意见
            applyTaskComments(ft, taskService.getTaskComments(task.getId()));
            if (FlowReturnConstants.INITIATOR_NODE_ID.equals(task.getTaskDefinitionKey()))
            {
                // 发起人节点（开始）：初次提交由 __start__ 行代表，强制收回后的重提也不留痕；
                // 只有被「退回」到发起人后的再次提交，才作为一条新的开始记录展示
                if (!isReturnOutcome(prevNonInitiatorOutcome))
                {
                    ft.setInitiatorPending(Boolean.TRUE);
                }
                else if (task.getEndTime() != null && !isReturnOutcome(ft.getOutcome()))
                {
                    // 退回后的重提已完成：消费掉「退回」上下文，避免其后管理员强制收回
                    // 产生的发起人待办因退回上下文残留而被误判为重提记录展示出来
                    prevNonInitiatorOutcome = null;
                }
            }
            else if (task.getEndTime() != null && StringUtils.isNotEmpty(ft.getOutcome()))
            {
                prevNonInitiatorOutcome = ft.getOutcome();
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
            // 被管理员干预跳过（任务被强制结束但没人办理）：流程状态里明确标注，避免误读成普通未办理
            if (skippedTaskIds.contains(task.getId()))
            {
                ft.setSkipped(Boolean.TRUE);
            }
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
        // 抄送人所在的环节接收时间：抄送动作发生在该任务上，耗时从任务接收开始计算
        Map<String, Date> taskStartTimes = new HashMap<>();
        for (HistoricTaskInstance task : tasks)
        {
            taskStartTimes.put(task.getId(), task.getCreateTime());
        }
        // 手动/节点抄送：作为一条操作记录写入流程状态（对标泛微：抄送也要留痕）
        list.addAll(buildCcRows(instanceId, taskStartTimes));
        // 转办：与抄送一样逐次留痕（转办人 → 被转办人 + 耗时 + 转办说明）
        list.addAll(buildDelegateRows(instanceId, taskStartTimes, delegatesByTask));
        // 管理员干预：保留管理员的操作记录（谁、何时、从哪个节点调到哪、说明）
        list.addAll(buildInterveneRows(instanceId, tasks, skippedTaskIds, interveneRecords));
        // 点开查看：把查看时间补到当前待办/候选行（对标泛微「已查看」操作状态 + 操作时间）
        fillNodeViewTimes(instanceId, list);
        // 抄送、查看与流转记录按发生时间交错展示（开始记录最早，天然排在最前）
        // 同一时刻：管理员干预发生在目标节点任务生成之前，干预行排在目标节点记录前
        list.sort((a, b) -> {
            Date ta = a.getCreateTime();
            Date tb = b.getCreateTime();
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
            int result = ta.compareTo(tb);
            if (result != 0)
            {
                return result;
            }
            boolean aIntervene = "intervene".equals(a.getOutcome());
            boolean bIntervene = "intervene".equals(b.getOutcome());
            return aIntervene == bIntervene ? 0 : (aIntervene ? -1 : 1);
        });
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
        row.setSkipped(base.getSkipped());
        return row;
    }

    /**
     * 管理员干预记录：从流程批注还原（谁、何时、来源节点、目标节点、指定办理人、说明）。
     *
     * <p>批注由 {@link #interveneInstance} 写入，兼容新旧两种文案格式；
     * 干预记录只用于展示，查询失败不影响流程状态与流程图。</p>
     *
     * @param instanceId 流程实例ID
     * @return 干预记录（按时间正序）
     */
    private List<Map<String, Object>> selectInterveneRecords(String instanceId)
    {
        List<Map<String, Object>> records = new ArrayList<>();
        try
        {
            List<Comment> comments = taskService.getProcessInstanceComments(instanceId);
            if (comments == null || comments.isEmpty())
            {
                return records;
            }
            for (Comment c : comments)
            {
                String msg = c.getFullMessage();
                if (StringUtils.isEmpty(msg) || !msg.startsWith(ADMIN_INTERVENE_PREFIX))
                {
                    continue;
                }
                Map<String, Object> rec = new LinkedHashMap<>();
                rec.put("commentId", c.getId());
                rec.put("userId", c.getUserId());
                rec.put("userName", StringUtils.isNotEmpty(c.getUserId())
                        ? resolveUserNickname(c.getUserId()) : "系统管理员");
                rec.put("time", c.getTime());
                Matcher m = INTERVENE_MSG_PATTERN.matcher(msg);
                if (m.matches())
                {
                    String first = m.group(1);
                    String second = m.group(2);
                    boolean hasFrom = StringUtils.isNotEmpty(second);
                    rec.put("fromName", hasFrom ? first : null);
                    rec.put("toName", hasFrom ? second : first);
                    rec.put("assigneeName", m.group(3));
                    rec.put("comment", m.group(4));
                }
                else
                {
                    // 历史数据格式不标准时，至少把批注原文保留下来
                    rec.put("toName", msg.substring(ADMIN_INTERVENE_PREFIX.length()));
                }
                records.add(rec);
            }
            records.sort((a, b) -> {
                Date ta = (Date) a.get("time");
                Date tb = (Date) b.get("time");
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
                return ta.compareTo(tb);
            });
        }
        catch (Exception e)
        {
            log.warn("查询管理员干预记录失败：{}", e.getMessage());
        }
        return records;
    }

    /**
     * 被管理员干预跳过的任务：任务已结束、没人写过办理意见，且结束时间与干预时间吻合。
     *
     * @param tasks    实例下的历史任务
     * @param records  干预记录
     * @return 被跳过的任务ID集合
     */
    private Set<String> resolveInterveneSkippedTaskIds(List<HistoricTaskInstance> tasks,
            List<Map<String, Object>> records)
    {
        Set<String> ids = new HashSet<>();
        if (tasks == null || tasks.isEmpty() || records == null || records.isEmpty())
        {
            return ids;
        }
        for (HistoricTaskInstance task : tasks)
        {
            if (task.getEndTime() == null
                    || pickOpinionComment(taskService.getTaskComments(task.getId())) != null)
            {
                continue;
            }
            for (Map<String, Object> rec : records)
            {
                Date time = (Date) rec.get("time");
                if (time != null && Math.abs(task.getEndTime().getTime() - time.getTime()) <= INTERVENE_TIME_TOLERANCE)
                {
                    ids.add(task.getId());
                    break;
                }
            }
        }
        return ids;
    }

    /**
     * 干预的来源节点名：新格式批注直接带来源节点；旧格式按「被跳过任务 + 同一时刻」还原。
     */
    private String resolveInterveneFromName(Map<String, Object> rec, List<HistoricTaskInstance> tasks,
            Set<String> skippedTaskIds)
    {
        Object fromName = rec.get("fromName");
        if (fromName != null && StringUtils.isNotEmpty(String.valueOf(fromName)))
        {
            return String.valueOf(fromName);
        }
        Date time = (Date) rec.get("time");
        if (time == null || tasks == null)
        {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (HistoricTaskInstance task : tasks)
        {
            if (!skippedTaskIds.contains(task.getId()) || task.getEndTime() == null)
            {
                continue;
            }
            if (Math.abs(task.getEndTime().getTime() - time.getTime()) > INTERVENE_TIME_TOLERANCE)
            {
                continue;
            }
            String name = StringUtils.isNotEmpty(task.getName()) ? task.getName() : task.getTaskDefinitionKey();
            if (name != null && !names.contains(name))
            {
                names.add(name);
            }
        }
        return names.isEmpty() ? null : String.join("、", names);
    }

    /**
     * 干预记录补全「指定办理人」：旧批注未写办理人时，用目标节点的实际办理人兜底，
     * 让流程状态/流程图浮层都能显示「人事审核：高洁」而不只是节点名。
     */
    private void fillInterveneAssigneeName(List<Map<String, Object>> records, List<HistoricTaskInstance> tasks)
    {
        if (records == null || records.isEmpty() || tasks == null || tasks.isEmpty())
        {
            return;
        }
        for (Map<String, Object> rec : records)
        {
            Object current = rec.get("assigneeName");
            if (current != null && StringUtils.isNotEmpty(String.valueOf(current)))
            {
                continue;
            }
            String toName = rec.get("toName") == null ? null : String.valueOf(rec.get("toName"));
            if (StringUtils.isEmpty(toName))
            {
                continue;
            }
            HistoricTaskInstance matched = null;
            for (HistoricTaskInstance task : tasks)
            {
                if (!toName.equals(task.getName()) || StringUtils.isEmpty(task.getAssignee()))
                {
                    continue;
                }
                // 优先取仍在办理的任务，其次取最近一条已结束的任务
                if (task.getEndTime() == null)
                {
                    matched = task;
                    break;
                }
                if (matched == null)
                {
                    matched = task;
                }
            }
            if (matched != null)
            {
                rec.put("assigneeName", resolveUserNickname(matched.getAssignee()));
            }
        }
    }

    /**
     * 干预目标节点的任务生成时间：干预批注写入前，目标节点任务已由引擎创建；
     * 取与干预时间最接近的那条任务生成时间作为干预时刻，保证「干预」排在「目标节点待办」之前。
     * （同一时刻下干预行优先，见流程记录排序）
     */
    private Date resolveInterveneTargetCreateTime(Date time, String toName, List<HistoricTaskInstance> tasks)
    {
        if (time == null || StringUtils.isEmpty(toName) || tasks == null)
        {
            return null;
        }
        Date matched = null;
        for (HistoricTaskInstance task : tasks)
        {
            if (!toName.equals(task.getName()) || task.getCreateTime() == null)
            {
                continue;
            }
            long diff = task.getCreateTime().getTime() - time.getTime();
            if (diff < -INTERVENE_TIME_TOLERANCE || diff > INTERVENE_TIME_TOLERANCE)
            {
                continue;
            }
            if (matched == null || task.getCreateTime().after(matched))
            {
                matched = task.getCreateTime();
            }
        }
        return matched;
    }

    /**
     * 管理员干预操作行：办理人=管理员，动作=干预，接收者=目标节点 + 指定办理人，意见=变动轨迹 + 说明。
     * 干预行与被干预节点（未办理）分开展示，节点行只呈现办理人自己的操作情况。
     */
    private List<FlowTask> buildInterveneRows(String instanceId, List<HistoricTaskInstance> tasks,
            Set<String> skippedTaskIds, List<Map<String, Object>> records)
    {
        List<FlowTask> rows = new ArrayList<>();
        if (records == null)
        {
            return rows;
        }
        fillInterveneAssigneeName(records, tasks);
        for (Map<String, Object> rec : records)
        {
            Date time = (Date) rec.get("time");
            String toName = rec.get("toName") == null ? "指定节点" : String.valueOf(rec.get("toName"));
            String assigneeName = rec.get("assigneeName") == null ? null : String.valueOf(rec.get("assigneeName"));
            String fromName = resolveInterveneFromName(rec, tasks, skippedTaskIds);
            // 干预时刻取目标节点任务的生成时间：干预是「因」，目标节点是「果」，排序时干预在前
            Date targetTime = resolveInterveneTargetCreateTime(time, toName, tasks);
            Date showTime = targetTime != null ? targetTime : time;
            FlowTask row = new FlowTask();
            row.setTaskId("__intervene__" + rec.get("commentId"));
            row.setTaskName("管理员干预");
            row.setInstanceId(instanceId);
            row.setCreateTime(showTime);
            row.setEndTime(showTime);
            row.setOutcome("intervene");
            row.setState("done");
            row.setAssigneeName(String.valueOf(rec.get("userName")));
            try
            {
                row.setAssigneeId(Long.valueOf(String.valueOf(rec.get("userId"))));
            }
            catch (NumberFormatException ignored) { }
            row.setReceiverName(toName + (StringUtils.isNotEmpty(assigneeName) ? "：" + assigneeName : ""));
            StringBuilder note = new StringBuilder("管理员");
            if (StringUtils.isNotEmpty(fromName))
            {
                note.append("将流程由「").append(fromName).append("」调整至「").append(toName).append("」");
            }
            else
            {
                note.append("干预至「").append(toName).append("」");
            }
            String remark = rec.get("comment") == null ? null : String.valueOf(rec.get("comment"));
            if (StringUtils.isNotEmpty(remark))
            {
                note.append("：").append(remark);
            }
            row.setComment(note.toString());
            rows.add(row);
        }
        return rows;
    }

    /**
     * 抄送操作记录：把 flow_cc 中的抄送转换为流程状态里的一条「抄送」操作。
     *
     * <p>节点自动抄送与办理人手动抄送都会写入 flow_cc，这里统一还原为：
     * 抄送人（操作人）、抄送环节、抄送时间、接收人、接收人是否已查看。</p>
     *
     * @param instanceId     流程实例ID
     * @param taskStartTimes taskId → 任务接收时间（抄送行的接收时间取该任务到达时间，便于计算耗时）
     * @return 抄送记录行
     */
    private List<FlowTask> buildCcRows(String instanceId, Map<String, Date> taskStartTimes)
    {
        List<FlowTask> rows = new ArrayList<>();
        try
        {
            List<FlowCc> ccs = flowCcService.selectByInstance(instanceId);
            for (FlowCc cc : ccs)
            {
                FlowTask row = new FlowTask();
                row.setTaskId("__cc__" + cc.getCcId());
                row.setTaskName(StringUtils.isNotEmpty(cc.getTaskName()) ? cc.getTaskName() : "抄送");
                row.setInstanceId(instanceId);
                row.setCreateTime(cc.getCreateTime());
                // 抄送记录的操作人 = 抄送接收人：接收时间=抄送时间，操作时间=阅读时间，未读则无操作时间
                row.setEndTime(cc.getReadTime() != null ? cc.getReadTime() : cc.getCreateTime());
                row.setOutcome("cc");
                row.setState("done");
                row.setAssigneeId(cc.getReceiverId());
                row.setAssigneeName(cc.getReceiverName());
                // 接收者列展示抄送人（前端渲染为「由 XX 抄送」）
                row.setReceiverName(cc.getSenderName());
                row.setViewTime(cc.getReadTime());
                // 批示意见列已移除，保留 comment 供办理页/详情页其他区域复用
                String receiver = StringUtils.isNotEmpty(cc.getReceiverName())
                        ? cc.getReceiverName() : String.valueOf(cc.getReceiverId());
                boolean read = "1".equals(cc.getReadFlag());
                String readText = read ? "已查看" : "未查看";
                if (read && cc.getReadTime() != null)
                {
                    readText += " " + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, cc.getReadTime());
                }
                row.setComment("抄送给：" + receiver + "（" + readText + "）");
                // 抄送人本人也是一次操作：记录「周伟 抄送 → 李华」，保证任何操作都有痕迹
                FlowTask send = new FlowTask();
                send.setTaskId("__ccSend__" + cc.getCcId());
                send.setTaskName(row.getTaskName());
                send.setInstanceId(instanceId);
                // 接收时间 = 所属任务到达抄送人的时间（不是抄送动作时间），这样抄送耗时 = 抄送时间 - 接收时间
                Date taskStart = cc.getTaskId() == null ? null : taskStartTimes.get(cc.getTaskId());
                send.setCreateTime(taskStart != null ? taskStart : cc.getCreateTime());
                send.setEndTime(cc.getCreateTime());
                send.setOutcome("ccSend");
                send.setState("done");
                send.setAssigneeId(cc.getSenderId());
                send.setAssigneeName(cc.getSenderName());
                send.setReceiverName(cc.getReceiverName());
                send.setComment(row.getComment());
                // 同一秒内先展示抄送动作，再展示被抄送人的阅读记录
                rows.add(send);
                rows.add(row);
            }
        }
        catch (Exception e)
        {
            log.warn("查询流程抄送记录失败：{}", e.getMessage());
        }
        return rows;
    }

    /**
     * 转办过程记录：把 flow_delegate 中的转办逐次还原为流程状态里的「转办」操作。
     *
     * <p>同一任务多次转办时，转办行的接收时间按链式计算：
     * 第一次取任务到达时间，之后取上一次转办时间；操作时间取本次转办时间，
     * 因此每条转办都能看出「这个人拿了多久才转出去」。</p>
     *
     * @param instanceId     流程实例ID
     * @param taskStartTimes taskId → 任务接收时间
     * @param delegatesByTask taskId → 转办记录（按转办时间正序）
     * @return 转办记录行
     */
    private List<FlowTask> buildDelegateRows(String instanceId, Map<String, Date> taskStartTimes,
            Map<String, List<FlowDelegate>> delegatesByTask)
    {
        List<FlowTask> rows = new ArrayList<>();
        for (Map.Entry<String, List<FlowDelegate>> entry : delegatesByTask.entrySet())
        {
            Date receivedTime = taskStartTimes.get(entry.getKey());
            for (FlowDelegate d : entry.getValue())
            {
                FlowTask row = new FlowTask();
                row.setTaskId("__delegate__" + d.getDelegateId());
                row.setTaskName(StringUtils.isNotEmpty(d.getTaskName()) ? d.getTaskName() : "转办");
                row.setInstanceId(instanceId);
                row.setCreateTime(receivedTime != null ? receivedTime : d.getCreateTime());
                row.setEndTime(d.getCreateTime());
                row.setOutcome("delegate");
                row.setState("done");
                row.setAssigneeId(d.getFromId());
                row.setAssigneeName(d.getFromName());
                // 接收者列展示被转办人
                row.setReceiverName(d.getToName());
                row.setComment(d.getComment());
                rows.add(row);
                // 下一棒：被转办人从本次转办时刻接手
                receivedTime = d.getCreateTime();
            }
        }
        return rows;
    }

    /**
     * 把节点查看时间补到对应的待办/候选行上。
     *
     * <p>数据源 flow_node_viewer（办理人/候选人打开办理页或流程详情时上报），
     * 按「节点 + 用户」匹配，只影响展示，不影响流转。</p>
     *
     * @param instanceId 流程实例ID
     * @param list       已生成的流转记录
     */
    private void fillNodeViewTimes(String instanceId, List<FlowTask> list)
    {
        Map<String, List<Map<String, Object>>> viewersByNode = flowNodeViewService.selectViewersByNode(instanceId);
        if (viewersByNode.isEmpty())
        {
            return;
        }
        Map<String, Object> viewTimes = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : viewersByNode.entrySet())
        {
            for (Map<String, Object> viewer : entry.getValue())
            {
                viewTimes.put(entry.getKey() + "#" + viewer.get("userId"), viewer.get("time"));
            }
        }
        for (FlowTask row : list)
        {
            if (!"pending".equals(row.getState()) || StringUtils.isEmpty(row.getTaskDefinitionKey())
                    || row.getAssigneeId() == null)
            {
                continue;
            }
            Object time = viewTimes.get(row.getTaskDefinitionKey() + "#" + row.getAssigneeId());
            if (time instanceof java.util.Date)
            {
                row.setViewTime((java.util.Date) time);
            }
        }
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
        // 管理员干预轨迹：图上把被跳过的节点标灰、点击目标节点能看到谁在什么时候把流程调过来的
        BpmnModel model = repositoryService.getBpmnModel(hpi.getProcessDefinitionId());
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricTaskInstanceStartTime().asc()
                .list();
        List<Map<String, Object>> interveneRecords = selectInterveneRecords(instanceId);
        Set<String> skippedTaskIds = resolveInterveneSkippedTaskIds(historicTasks, interveneRecords);
        // 旧批注没有指定办理人时，用目标节点的实际办理人补全
        fillInterveneAssigneeName(interveneRecords, historicTasks);
        List<Map<String, Object>> interventions = new ArrayList<>();
        Set<String> skippedNodeIds = new LinkedHashSet<>();
        for (Map<String, Object> rec : interveneRecords)
        {
            Date time = (Date) rec.get("time");
            String toName = rec.get("toName") == null ? "指定节点" : String.valueOf(rec.get("toName"));
            Map<String, Object> item = new LinkedHashMap<>(rec);
            item.put("toId", resolveUserTaskIdByName(model, toName));
            List<String> fromIds = new ArrayList<>();
            List<String> fromNames = new ArrayList<>();
            for (HistoricTaskInstance task : historicTasks)
            {
                if (!skippedTaskIds.contains(task.getId()) || task.getEndTime() == null || time == null)
                {
                    continue;
                }
                if (Math.abs(task.getEndTime().getTime() - time.getTime()) > INTERVENE_TIME_TOLERANCE)
                {
                    continue;
                }
                if (StringUtils.isNotEmpty(task.getTaskDefinitionKey())
                        && !fromIds.contains(task.getTaskDefinitionKey()))
                {
                    fromIds.add(task.getTaskDefinitionKey());
                    skippedNodeIds.add(task.getTaskDefinitionKey());
                }
                String name = StringUtils.isNotEmpty(task.getName()) ? task.getName() : task.getTaskDefinitionKey();
                if (name != null && !fromNames.contains(name))
                {
                    fromNames.add(name);
                }
            }
            item.put("fromIds", fromIds);
            item.put("fromNames", fromNames);
            item.put("fromName", resolveInterveneFromName(rec, historicTasks, skippedTaskIds));
            interventions.add(item);
        }
        data.put("interventions", interventions);
        data.put("skippedIds", new ArrayList<>(skippedNodeIds));
        // 按节点聚合的运行期人员（节点上显示审批人 + 点节点看已操作/未审批/已查看）
        Map<String, Object> nodes = buildNodeParticipants(instanceId, hpi, activities);
        // 干预记录挂到目标节点：浮层里展示「管理员干预」明细
        for (Map<String, Object> item : interventions)
        {
            String toId = item.get("toId") == null ? null : String.valueOf(item.get("toId"));
            if (StringUtils.isEmpty(toId))
            {
                continue;
            }
            Map<String, Object> node = nodeEntry(nodes, toId, String.valueOf(item.get("toName")));
            interveneList(node).add(item);
        }
        data.put("nodes", nodes);
        return data;
    }

    /** 按节点名称在流程定义里定位用户任务ID（干预批注只存名称，图上需要 ID） */
    private String resolveUserTaskIdByName(BpmnModel model, String name)
    {
        if (model == null || model.getMainProcess() == null || StringUtils.isEmpty(name))
        {
            return null;
        }
        for (FlowElement element : model.getMainProcess().getFlowElements())
        {
            if (element instanceof UserTask && name.equals(element.getName()))
            {
                return element.getId();
            }
        }
        return null;
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
        // 各节点知情人（办理人 + 候选人 + 抄送接收人 + 转办参与人）：已查看人员只在这些人里取，
        // 避免把旁观者算成节点查看人，同时保留抄送、转办人员的查看记录
        Map<String, Set<String>> participantsByNode = new HashMap<>();
        Map<String, String> nodeByTask = new HashMap<>();
        for (HistoricTaskInstance task : tasks)
        {
            if (StringUtils.isEmpty(task.getTaskDefinitionKey()))
            {
                continue;
            }
            nodeByTask.put(task.getId(), task.getTaskDefinitionKey());
            Set<String> participants = participantsByNode.computeIfAbsent(task.getTaskDefinitionKey(),
                    k -> new HashSet<>());
            if (StringUtils.isNotEmpty(task.getAssignee()))
            {
                participants.add(task.getAssignee());
            }
            List<String> candidates = candidatesByTask.get(task.getId());
            if (candidates != null)
            {
                participants.addAll(candidates);
            }
        }
        try
        {
            for (FlowDelegate d : flowDelegateMapper.selectByInstance(instanceId))
            {
                String nodeId = nodeByTask.get(d.getTaskId());
                if (StringUtils.isEmpty(nodeId))
                {
                    continue;
                }
                Set<String> participants = participantsByNode.computeIfAbsent(nodeId, k -> new HashSet<>());
                if (d.getFromId() != null)
                {
                    participants.add(String.valueOf(d.getFromId()));
                }
                if (d.getToId() != null)
                {
                    participants.add(String.valueOf(d.getToId()));
                }
            }
            for (FlowCc cc : flowCcService.selectByInstance(instanceId))
            {
                String nodeId = nodeByTask.get(cc.getTaskId());
                if (StringUtils.isEmpty(nodeId) || cc.getReceiverId() == null)
                {
                    continue;
                }
                participantsByNode.computeIfAbsent(nodeId, k -> new HashSet<>())
                        .add(String.valueOf(cc.getReceiverId()));
            }
        }
        catch (Exception e)
        {
            log.debug("汇总节点知情人失败：{}", e.getMessage());
        }
        // 节点查看记录：按「节点 + 用户」索引，给待办/已操作人员补「是否查看 + 查看时间」
        Map<String, Map<String, Object>> viewByNodeUser = new HashMap<>();
        Map<String, List<Map<String, Object>>> viewers = flowNodeViewService.selectViewersByNode(instanceId);
        for (Map.Entry<String, List<Map<String, Object>>> entry : viewers.entrySet())
        {
            for (Map<String, Object> viewer : entry.getValue())
            {
                viewByNodeUser.put(entry.getKey() + "#" + viewer.get("userId"), viewer);
            }
        }
        for (HistoricTaskInstance task : tasks)
        {
            String nodeId = task.getTaskDefinitionKey();
            if (StringUtils.isEmpty(nodeId))
            {
                continue;
            }
            Map<String, Object> node = nodeEntry(nodes, nodeId,
                    taskDisplayName(task.getProcessDefinitionId(), nodeId, task.getName()));
            String assignee = task.getAssignee();
            if (task.getEndTime() != null)
            {
                // 已结束任务：有办理意见才算「已操作」；撤销/终止时引擎补了 endTime 但没人真正办理
                // 只取真正的签字意见，转办/抄送等操作批注不覆盖办理结果
                Comment picked = pickOpinionComment(taskService.getTaskComments(task.getId()));
                String outcome = null;
                String comment = null;
                if (picked != null)
                {
                    FlowTask tmp = new FlowTask();
                    applyComment(tmp, picked);
                    outcome = tmp.getOutcome();
                    comment = tmp.getComment();
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
                    // 接收时间 / 操作时间：浮层里算「操作耗时」，与泛微流程状态口径一致
                    person.put("createTime", task.getCreateTime());
                    person.put("time", task.getEndTime());
                    attachViewTime(nodeId, person, viewByNodeUser);
                    // 同一节点被退回重办时同一人会办理多次，这里不去重，各自的动作与时间都有意义
                    doneList(node).add(person);
                }
                else
                {
                    Map<String, Object> person = personEntry(assignee, nameCache);
                    person.put("taskId", task.getId());
                    person.put("kind", "skipped");
                    person.put("createTime", task.getCreateTime());
                    attachViewTime(nodeId, person, viewByNodeUser);
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
                person.put("createTime", task.getCreateTime());
                attachViewTime(nodeId, person, viewByNodeUser);
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
                    person.put("createTime", task.getCreateTime());
                    attachViewTime(nodeId, person, viewByNodeUser);
                    addPending(node, person);
                }
            }
        }
        // 3) 已查看：只保留「看过但没挂在该节点人员列表里」的人，待办/已操作人员已直接带上查看时间
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
            Set<String> participants = participantsByNode.getOrDefault(entry.getKey(), java.util.Collections.emptySet());
            for (Map<String, Object> viewer : entry.getValue())
            {
                // 非该节点办理人/候选人的查看记录（如管理员打开办理页产生的）不展示
                if (!participants.contains(String.valueOf(viewer.get("userId"))))
                {
                    continue;
                }
                if (!handled.contains(String.valueOf(viewer.get("userId"))))
                {
                    viewedList(node).add(viewer);
                }
            }
        }
        // 4) 抄送记录：挂在抄送发生的节点上（flow_cc 只存任务ID，按历史任务还原节点）
        Map<String, String> taskNodeMap = new HashMap<>();
        for (HistoricTaskInstance task : tasks)
        {
            if (StringUtils.isNotEmpty(task.getTaskDefinitionKey()))
            {
                taskNodeMap.put(task.getId(), task.getTaskDefinitionKey());
            }
        }
        try
        {
            for (FlowCc cc : flowCcService.selectByInstance(instanceId))
            {
                String nodeId = taskNodeMap.get(cc.getTaskId());
                if (StringUtils.isEmpty(nodeId))
                {
                    continue;
                }
                Map<String, Object> node = nodeEntry(nodes, nodeId, cc.getTaskName());
                Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("ccId", cc.getCcId());
                item.put("senderId", cc.getSenderId());
                item.put("senderName", cc.getSenderName());
                item.put("receiverId", cc.getReceiverId());
                item.put("receiverName", cc.getReceiverName());
                item.put("readFlag", cc.getReadFlag());
                item.put("readTime", cc.getReadTime());
                item.put("time", cc.getCreateTime());
                ccList(node).add(item);
            }
        }
        catch (Exception e)
        {
            log.warn("查询流程抄送记录失败：{}", e.getMessage());
        }
        try
        {
            for (FlowDelegate d : flowDelegateMapper.selectByInstance(instanceId))
            {
                String nodeId = taskNodeMap.get(d.getTaskId());
                if (StringUtils.isEmpty(nodeId))
                {
                    continue;
                }
                Map<String, Object> node = nodeEntry(nodes, nodeId, d.getTaskName());
                Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("delegateId", d.getDelegateId());
                item.put("fromId", d.getFromId());
                item.put("fromName", d.getFromName());
                item.put("toId", d.getToId());
                item.put("toName", d.getToName());
                item.put("comment", d.getComment());
                item.put("time", d.getCreateTime());
                delegateList(node).add(item);
            }
        }
        catch (Exception e)
        {
            log.warn("查询流程转办记录失败：{}", e.getMessage());
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
            node.put("cc", new ArrayList<Map<String, Object>>());
            node.put("delegate", new ArrayList<Map<String, Object>>());
            node.put("intervene", new ArrayList<Map<String, Object>>());
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> ccList(Map<String, Object> node)
    {
        return (List<Map<String, Object>>) node.get("cc");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> delegateList(Map<String, Object> node)
    {
        return (List<Map<String, Object>>) node.get("delegate");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> interveneList(Map<String, Object> node)
    {
        return (List<Map<String, Object>>) node.get("intervene");
    }

    /**
     * 给节点人员补「是否查看 + 查看时间」。
     *
     * <p>查看记录按「节点 + 用户」匹配；有记录即视为该办理人已查看，
     * 流程图浮层直接显示「已查看 时间」，未查看显示「未查看」。</p>
     */
    @SuppressWarnings("unchecked")
    private void attachViewTime(String nodeId, Map<String, Object> person,
            Map<String, Map<String, Object>> viewByNodeUser)
    {
        Map<String, Object> viewer = viewByNodeUser.get(nodeId + "#" + person.get("userId"));
        person.put("viewed", viewer != null);
        if (viewer != null)
        {
            person.put("viewTime", viewer.get("time"));
        }
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
            Long userId = SecurityUtils.getUserId();
            if (userId == null)
            {
                return;
            }
            // 办理人/候选人，以及该任务的抄送接收人、转办参与人才算「查看该节点」；
            // 与节点无关的旁观者（如管理员直接打开办理页）不记录，避免节点下凭空多出「已查看人员」
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null || StringUtils.isEmpty(task.getProcessInstanceId()) || !isViewableBy(task, userId))
            {
                return;
            }
            flowNodeViewService.recordView(task.getProcessInstanceId(), task.getTaskDefinitionKey(), task.getId(),
                    userId, resolveUserNickname(String.valueOf(userId)));
        }
        catch (Exception e)
        {
            // 查看记录仅用于流程图展示，失败不影响办理
            log.debug("记录流程节点查看失败：{}", e.getMessage());
        }
    }

    /**
     * 当前用户能否把该任务算作「本人查看」。
     *
     * <p>除办理人/候选人外，该任务的抄送接收人、转办参与人（转办人/被转办人）也属于节点知情人，
     * 打开办理页同样要留下查看记录；没有任何关联的旁观者不记录。</p>
     */
    private boolean isViewableBy(Task task, Long userId)
    {
        String uid = String.valueOf(userId);
        if (taskService.createTaskQuery().taskId(task.getId()).taskCandidateOrAssigned(uid).count() > 0)
        {
            return true;
        }
        try
        {
            for (FlowDelegate d : flowDelegateMapper.selectByInstance(task.getProcessInstanceId()))
            {
                if (task.getId().equals(d.getTaskId())
                        && (userId.equals(d.getFromId()) || userId.equals(d.getToId())))
                {
                    return true;
                }
            }
            for (FlowCc cc : flowCcService.selectByInstance(task.getProcessInstanceId()))
            {
                if (task.getId().equals(cc.getTaskId()) && userId.equals(cc.getReceiverId()))
                {
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            log.debug("校验节点查看人失败：{}", e.getMessage());
        }
        return false;
    }

    @Override
    public void recordInstanceView(String instanceId)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            return;
        }
        try
        {
            Long userId = SecurityUtils.getUserId();
            if (userId == null)
            {
                return;
            }
            String uid = String.valueOf(userId);
            // 只记录「本人是当前节点办理人/候选人」的查看，避免抄送人翻阅详情也被算成节点操作人
            List<Task> tasks = taskService.createTaskQuery()
                    .processInstanceId(instanceId)
                    .taskCandidateOrAssigned(uid)
                    .list();
            for (Task task : tasks)
            {
                flowNodeViewService.recordView(instanceId, task.getTaskDefinitionKey(), task.getId(),
                        userId, resolveUserNickname(uid));
            }
            // 抄送接收人打开流程详情同样记录阅读，抄送记录才能体现「已查看 + 查看时间」
            flowCcService.markReadByInstance(instanceId);
        }
        catch (Exception e)
        {
            // 查看记录只用于展示，失败不影响查看详情
            log.debug("记录流程查看失败：{}", e.getMessage());
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

    /**
     * 是否「退回/驳回」类办理结果：退回发起人后的再次提交需要单独留痕。
     */
    private boolean isReturnOutcome(String outcome)
    {
        if (StringUtils.isEmpty(outcome))
        {
            return false;
        }
        String o = outcome.trim();
        return "rejected".equalsIgnoreCase(o) || "reject".equalsIgnoreCase(o) || "return".equalsIgnoreCase(o)
                || o.contains("退回") || o.contains("驳回");
    }

    /**
     * 任务批注是否仅为操作留痕（转办、抄送等）。
     *
     * <p>这些记录已经由 flow_delegate / flow_cc 还原成独立的操作行，
     * 不能再当作「签字意见」，否则会把真正的办理意见覆盖掉。</p>
     */
    private boolean isOperationComment(String msg)
    {
        return msg != null && (msg.startsWith("【delegate】") || msg.startsWith("【addSign】")
                || msg.startsWith("【cc】") || msg.startsWith("【ccSend】")
                // 管理员干预不是办理人签字意见：由单独的「管理员干预」记录展示
                || msg.startsWith(ADMIN_INTERVENE_PREFIX));
    }

    /**
     * 从任务批注里挑出真正的签字意见：跳过转办/抄送等操作留痕，取时间最新的一条。
     *
     * <p>同一任务被转办后由新办理人办结时会有多条批注（转办 + 同意/驳回），
     * 只有最后一条办理意见才代表这个任务的办理结果。</p>
     */
    private Comment pickOpinionComment(List<Comment> comments)
    {
        if (comments == null || comments.isEmpty())
        {
            return null;
        }
        Comment picked = null;
        for (Comment c : comments)
        {
            if (c == null || StringUtils.isEmpty(c.getFullMessage()) || isOperationComment(c.getFullMessage()))
            {
                continue;
            }
            if (picked == null || (c.getTime() != null && (picked.getTime() == null || c.getTime().after(picked.getTime()))))
            {
                picked = c;
            }
        }
        return picked;
    }

    /** 把一个任务的批注合并成 FlowTask 的办理结果（outcome + 签字意见） */
    private void applyTaskComments(FlowTask ft, List<Comment> comments)
    {
        Comment picked = pickOpinionComment(comments);
        if (picked != null)
        {
            applyComment(ft, picked);
        }
    }

    /** 历史流程实例转视图对象 */
    private FlowInstance convertInstance(HistoricProcessInstance hpi)
    {
        FlowInstance fi = new FlowInstance();
        fi.setInstanceId(hpi.getId());
        // 流程编号：短数字请求ID（对标泛微 requestid），未登记时即时补登记，避免列表出现长UUID
        Long requestId = flowRequestService.requestIdOf(hpi.getId());
        if (requestId == null)
        {
            requestId = flowRequestService.register(hpi.getId(), hpi.getProcessDefinitionKey(), hpi.getBusinessKey());
        }
        fi.setRequestId(requestId);
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
                HistoricActivityInstance current = active.get(0);
                fi.setCurrentActivityName(taskDisplayName(hpi.getProcessDefinitionId(),
                        current.getActivityId(), current.getActivityName()));
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
        ft.setTaskName(taskDisplayName(task.getProcessDefinitionId(), task.getTaskDefinitionKey(), task.getName()));
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
        ft.setTaskName(taskDisplayName(task.getProcessDefinitionId(), task.getTaskDefinitionKey(), task.getName()));
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
                HistoricActivityInstance current = active.get(0);
                ft.setCurrentTaskName(taskDisplayName(hpi.getProcessDefinitionId(),
                        current.getActivityId(), current.getActivityName()));
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

    /**
     * 流程分类树：数量口径与右侧列表完全一致（同一份数据源、同一过滤条件），
     * 避免出现「左侧树显示 1 条、右侧列表为空」这类左右不一致的问题。
     *
     * @param scope  数据口径：todo 待办 / returned 退回 / cc 待阅 / draft 草稿 /
     *               all 待办-全部（待办+未读待阅+草稿）/ done 已办 /
     *               my 我的请求 / involved 流程查询 / monitor 流程监控
     * @param status 状态页签：my/involved/monitor 取 running|completed|canceled；done 取 running|finished
     */
    @Override
    public List<Map<String, Object>> selectProcessTree(String scope, String status)
    {
        Map<String, Long> counts = new java.util.LinkedHashMap<>();
        Map<String, String> names = new java.util.HashMap<>();
        String s = StringUtils.isEmpty(scope) ? "todo" : scope;
        if ("todo".equals(s) || "returned".equals(s) || "all".equals(s))
        {
            // 待办 / 退回 / 待办-全部：都从「我的待办任务」出发
            FlowTask todoQuery = new FlowTask();
            if ("returned".equals(s))
            {
                todoQuery.setReturnedOnly(true);
            }
            for (FlowTask ft : selectTodoList(todoQuery))
            {
                addTreeCount(counts, names, ft.getProcessDefinitionKey(), ft.getProcessDefinitionName());
            }
            if ("all".equals(s))
            {
                // 「全部」页签 = 待办 + 未读待阅 + 我的草稿（与 pendingList 合并口径一致）
                FlowCc ccQuery = new FlowCc();
                ccQuery.setReadFlag("0");
                for (FlowCc cc : flowCcService.selectMyCcList(ccQuery))
                {
                    addTreeCount(counts, names, cc.getProcessKey(), cc.getProcessName());
                }
                for (FlowDraft draft : flowDraftService.selectMyDraftList(new FlowDraft()))
                {
                    addTreeCount(counts, names, draft.getProcessKey(), draft.getProcessName());
                }
            }
        }
        else if ("cc".equals(s))
        {
            for (FlowCc cc : flowCcService.selectMyCcList(new FlowCc()))
            {
                addTreeCount(counts, names, cc.getProcessKey(), cc.getProcessName());
            }
        }
        else if ("draft".equals(s))
        {
            for (FlowDraft draft : flowDraftService.selectMyDraftList(new FlowDraft()))
            {
                addTreeCount(counts, names, draft.getProcessKey(), draft.getProcessName());
            }
        }
        else if ("done".equals(s))
        {
            FlowTask doneQuery = new FlowTask();
            doneQuery.setStatusScope(status);
            for (FlowTask ft : selectDoneList(doneQuery))
            {
                addTreeCount(counts, names, ft.getProcessDefinitionKey(), ft.getProcessDefinitionName());
            }
        }
        else if ("my".equals(s) || "involved".equals(s) || "monitor".equals(s))
        {
            FlowInstance query = new FlowInstance();
            query.setStatus(status);
            List<FlowInstance> instances = "my".equals(s) ? selectMyProcessList(query)
                    : ("involved".equals(s) ? selectInvolvedProcessList(query) : selectMonitorProcessList(query));
            for (FlowInstance fi : instances)
            {
                addTreeCount(counts, names, fi.getProcessDefinitionKey(), fi.getProcessDefinitionName());
            }
        }
        return buildProcessTree(counts, names);
    }

    /** 按流程定义Key累加数量（同一实例/任务只算一行） */
    private void addTreeCount(Map<String, Long> counts, Map<String, String> names, String key, String name)
    {
        if (StringUtils.isEmpty(key))
        {
            return;
        }
        counts.merge(key, 1L, Long::sum);
        if (StringUtils.isEmpty(names.get(key)) && StringUtils.isNotEmpty(name))
        {
            names.put(key, name);
        }
    }

    /** 把「定义Key -> 数量」组装成「分类 -> 流程」两层树，只保留有数据的流程 */
    private List<Map<String, Object>> buildProcessTree(Map<String, Long> counts, Map<String, String> fallbackNames)
    {
        Map<String, ProcessDefinition> defByKey = new java.util.HashMap<>();
        for (ProcessDefinition def : repositoryService.createProcessDefinitionQuery().latestVersion().list())
        {
            if (!defByKey.containsKey(def.getKey()))
            {
                defByKey.put(def.getKey(), def);
            }
        }
        java.text.Collator collator = java.text.Collator.getInstance(java.util.Locale.CHINA);
        List<Map<String, Object>> leaves = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counts.entrySet())
        {
            ProcessDefinition def = defByKey.get(entry.getKey());
            String category = def == null ? null : def.getCategory();
            // 空分类或 BPMN 命名空间归入「未分类」
            if (StringUtils.isEmpty(category) || category.contains("://"))
            {
                category = "未分类";
            }
            String name = def != null && StringUtils.isNotEmpty(def.getName())
                    ? def.getName() : fallbackNames.get(entry.getKey());
            Map<String, Object> leaf = new java.util.HashMap<>();
            leaf.put("id", entry.getKey());
            leaf.put("key", entry.getKey());
            leaf.put("label", StringUtils.isNotEmpty(name) ? name : entry.getKey());
            leaf.put("count", entry.getValue());
            leaf.put("_category", category);
            leaves.add(leaf);
        }
        leaves.sort((a, b) -> {
            int byCategory = collator.compare(String.valueOf(a.get("_category")), String.valueOf(b.get("_category")));
            return byCategory != 0 ? byCategory
                    : collator.compare(String.valueOf(a.get("label")), String.valueOf(b.get("label")));
        });
        Map<String, Map<String, Object>> byCategory = new java.util.LinkedHashMap<>();
        for (Map<String, Object> leaf : leaves)
        {
            String category = String.valueOf(leaf.remove("_category"));
            Map<String, Object> node = byCategory.get(category);
            if (node == null)
            {
                node = new java.util.HashMap<>();
                node.put("id", category);
                String label = DictUtils.getDictLabel("flowable_category", category, ",");
                node.put("label", StringUtils.isEmpty(label) ? category : label);
                node.put("children", new ArrayList<Map<String, Object>>());
                node.put("count", 0L);
                byCategory.put(category, node);
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
            children.add(leaf);
            node.put("count", (Long) node.get("count") + (Long) leaf.get("count"));
        }
        return new ArrayList<>(byCategory.values());
    }

    // ==================== 已结束流程的后续处理：复活 / 重新发起 ====================

    /**
     * 复活已结束/已中断的流程并停在指定节点。
     *
     * <p>为什么不能沿用「干预」：Flowable 流程结束时 act_ru_execution/act_ru_task 被清空，
     * changeActivityState 没有可迁移的执行对象。因此这里按「还原变量 → 重新启动 → 搬迁到目标节点」
     * 三步走，businessKey 保持原值，业务表那一条数据继续沿用（原流程编号不变），
     * 旧实例的历史记录不动，便于追溯。</p>
     */
    @Override
    @Transactional
    public Map<String, Object> reviveInstance(String instanceId, String targetActivityId, Long assigneeId,
            String comment)
    {
        if (StringUtils.isEmpty(instanceId) || StringUtils.isEmpty(targetActivityId))
        {
            throw new ServiceException("流程实例与目标节点不能为空");
        }
        if (runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult() != null)
        {
            throw new ServiceException("流程仍在运行中，请使用「干预」跳转节点");
        }
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (hpi == null)
        {
            throw new ServiceException("流程实例不存在");
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(hpi.getProcessDefinitionId()).singleResult();
        if (definition == null)
        {
            throw new ServiceException("流程定义不存在或已删除，无法复活");
        }
        if (definition.isSuspended())
        {
            throw new ServiceException("流程定义「" + definition.getName() + "」已挂起，请先激活后再复活");
        }
        BpmnModel model = repositoryService.getBpmnModel(definition.getId());
        FlowElement target = model == null ? null : model.getFlowElement(targetActivityId);
        if (!(target instanceof UserTask))
        {
            throw new ServiceException("目标节点不是用户任务节点，无法复活");
        }
        String targetName = StringUtils.isNotEmpty(target.getName()) ? target.getName() : targetActivityId;

        // 还原历史变量：业务字段、审批过程中产生的变量都带回来，避免复活后表单/分支条件取值缺失
        Map<String, Object> vars = readHistoricVariables(instanceId);
        vars.put("reviveFromInstanceId", instanceId);

        // 复活前先回滚业务台账：已办结的台账回到「审批中」，并回滚考勤/余额副作用
        for (FlowArchiveHandler handler : SpringUtils.getBeansOfType(FlowArchiveHandler.class).values())
        {
            try
            {
                handler.rollback(definition.getKey(), instanceId, hpi.getBusinessKey());
            }
            catch (Exception e)
            {
                log.error("流程复活回滚台账失败：processKey={}, instanceId={}, handler={}, error={}",
                        definition.getKey(), instanceId, handler.getClass().getSimpleName(), e.getMessage(), e);
            }
        }

        String title = StringUtils.isNotEmpty(hpi.getName()) ? hpi.getName() : hpi.getProcessDefinitionName();
        org.flowable.engine.runtime.ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(definition.getId())
                .name(title)
                .variables(vars);
        if (StringUtils.isNotEmpty(hpi.getBusinessKey()))
        {
            builder.businessKey(hpi.getBusinessKey());
        }
        // 发起人保持原实例的发起人（「退回发起人」节点、按发起人过滤待办都依赖它），取不到才用当前管理员
        String starterId = StringUtils.isNotEmpty(hpi.getStartUserId()) ? hpi.getStartUserId()
                : String.valueOf(SecurityUtils.getUserId());
        ProcessInstance revived;
        org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(starterId);
        try
        {
            revived = builder.start();
            // 启动后执行流会自然停在流程的第一个用户任务上，再搬到管理员指定的节点
            List<String> activeIds = runtimeService.getActiveActivityIds(revived.getId());
            List<String> fromIds = new ArrayList<>();
            for (String id : activeIds)
            {
                if (model.getFlowElement(id) instanceof UserTask)
                {
                    fromIds.add(id);
                }
            }
            if (fromIds.isEmpty())
            {
                fromIds.addAll(activeIds);
            }
            boolean alreadyAtTarget = fromIds.size() == 1 && fromIds.contains(targetActivityId);
            if (!alreadyAtTarget)
            {
                if (fromIds.isEmpty())
                {
                    throw new ServiceException("复活后流程没有可流转的活动节点，请检查流程配置");
                }
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(revived.getId())
                        .moveActivityIdsToSingleActivityId(fromIds, targetActivityId)
                        .changeState();
            }
        }
        finally
        {
            org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(null);
        }

        List<Task> tasks = taskService.createTaskQuery().processInstanceId(revived.getId())
                .taskDefinitionKey(targetActivityId).list();
        if (assigneeId != null)
        {
            for (Task task : tasks)
            {
                taskService.setAssignee(task.getId(), String.valueOf(assigneeId));
            }
        }
        StringBuilder msg = new StringBuilder(ADMIN_REVIVE_PREFIX)
                .append("管理员复活已结束流程至「").append(targetName).append("」");
        if (assigneeId != null)
        {
            msg.append("，指定办理人「").append(resolveUserNickname(String.valueOf(assigneeId))).append("」");
        }
        if (StringUtils.isNotEmpty(comment))
        {
            msg.append("：").append(comment);
        }
        taskService.addComment(tasks.isEmpty() ? null : tasks.get(0).getId(), revived.getId(), "comment",
                msg.toString());

        // 业务数据行继续沿用（同一单继续走），实例ID指向新实例，详情页从新实例取数据
        rebindBusinessInstanceId(definition.getKey(), hpi.getBusinessKey(), instanceId, revived.getId());
        // 业务台账的 flow_instance_id 同步切到新实例，否则后续办结/终止找不到台账
        for (FlowArchiveHandler handler : SpringUtils.getBeansOfType(FlowArchiveHandler.class).values())
        {
            try
            {
                handler.rebind(definition.getKey(), instanceId, revived.getId());
            }
            catch (Exception e)
            {
                log.error("流程复活回写台账实例ID失败：processKey={}, old={}, new={}, handler={}, error={}",
                        definition.getKey(), instanceId, revived.getId(), handler.getClass().getSimpleName(),
                        e.getMessage(), e);
            }
        }

        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("instanceId", revived.getId());
        data.put("sourceInstanceId", instanceId);
        data.put("processDefinitionId", definition.getId());
        data.put("processDefinitionKey", definition.getKey());
        data.put("title", title);
        data.put("targetActivityId", targetActivityId);
        data.put("targetActivityName", targetName);
        data.put("taskCount", tasks.size());
        data.put("assigneeId", assigneeId);
        data.put("assigneeName", assigneeId == null ? null : resolveUserNickname(String.valueOf(assigneeId)));
        return data;
    }

    /**
     * 以原流程的表单数据为模板重新发起一条新流程。
     *
     * <p>业务行数据优先于历史变量：审批过程中字段可能被改写，业务表才是最新值。
     * 流程编号不沿用原单（清空后按规则重新生成），避免两条流程共用同一个编号。</p>
     */
    @Override
    @Transactional
    public Map<String, Object> restartInstance(String instanceId, String title)
    {
        if (StringUtils.isEmpty(instanceId))
        {
            throw new ServiceException("流程实例不能为空");
        }
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceId).singleResult();
        if (hpi == null)
        {
            throw new ServiceException("流程实例不存在");
        }
        if (hpi.getEndTime() == null)
        {
            throw new ServiceException("流程仍在运行中，无需重新发起");
        }
        String processKey = hpi.getProcessDefinitionKey();
        if (StringUtils.isEmpty(processKey))
        {
            throw new ServiceException("流程定义标识为空，无法重新发起");
        }
        com.ruoyi.flowable.domain.FlowForm form = resolveBusinessForm(processKey, hpi.getBusinessKey(), instanceId);
        Map<String, Object> vars = readHistoricVariables(instanceId);
        // 业务数据行覆盖变量：字段最新值以业务表为准
        if (form != null && StringUtils.isNotEmpty(form.getTableName()) && StringUtils.isNotEmpty(hpi.getBusinessKey()))
        {
            try
            {
                Map<String, Object> row = jdbcTemplate.queryForMap(
                        "SELECT * FROM `" + form.getTableName() + "` WHERE id = ?", hpi.getBusinessKey());
                for (Map.Entry<String, Object> entry : row.entrySet())
                {
                    String key = entry.getKey();
                    if ("id".equals(key) || "process_instance_id".equals(key) || "create_by".equals(key)
                            || "create_time".equals(key) || "update_by".equals(key) || "update_time".equals(key))
                    {
                        continue;
                    }
                    vars.put(key, entry.getValue());
                }
            }
            catch (Exception e)
            {
                log.warn("重新发起：读取原单据数据失败（{}），改为使用历史变量：{}", hpi.getBusinessKey(), e.getMessage());
            }
        }
        // 重新发号：同一个流程编号不能挂在两条流程上
        vars.remove(FlowSystemFields.FIELD_PROCESS_NO);
        String sourceTitle = StringUtils.isNotEmpty(hpi.getName()) ? hpi.getName() : hpi.getProcessDefinitionName();
        String newTitle = StringUtils.isNotEmpty(title) ? title.trim() : sourceTitle + "（重新发起）";
        vars.put(FlowSystemFields.FIELD_TITLE, newTitle);

        String newInstanceId = flowFormService.launch(processKey, form == null ? null : form.getFormId(), vars, null,
                newTitle);

        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("instanceId", newInstanceId);
        data.put("sourceInstanceId", instanceId);
        data.put("processDefinitionKey", processKey);
        data.put("formId", form == null ? null : form.getFormId());
        data.put("title", newTitle);
        Object newProcessNo = null;
        try
        {
            newProcessNo = runtimeService.getVariable(newInstanceId, FlowSystemFields.FIELD_PROCESS_NO);
        }
        catch (Exception e)
        {
            log.warn("重新发起：读取新实例流程编号失败：{}", e.getMessage());
        }
        data.put("processNo", newProcessNo != null ? newProcessNo : vars.get(FlowSystemFields.FIELD_PROCESS_NO));
        return data;
    }

    /**
     * 读取历史变量（复活/重新发起共用）
     *
     * 排除引擎内部变量、会签集合变量（节点执行时会重新解析）与本方法自己写的追溯变量，
     * 反序列化失败的变量只记日志跳过，不让一条脏变量卡住整单。
     */
    private Map<String, Object> readHistoricVariables(String instanceId)
    {
        Map<String, Object> vars = new java.util.HashMap<>();
        List<org.flowable.variable.api.history.HistoricVariableInstance> list = historyService
                .createHistoricVariableInstanceQuery().processInstanceId(instanceId).list();
        for (org.flowable.variable.api.history.HistoricVariableInstance var : list)
        {
            String name = var.getVariableName();
            if (StringUtils.isEmpty(name) || name.startsWith("_") || "processInstanceId".equals(name)
                    || "executionId".equals(name) || "taskId".equals(name)
                    || name.startsWith(com.ruoyi.flowable.service.FlowParticipantResolver.COLLECTION_PREFIX)
                    || "reviveFromInstanceId".equals(name))
            {
                continue;
            }
            try
            {
                Object value = var.getValue();
                if (value != null)
                {
                    vars.put(name, value);
                }
            }
            catch (Exception e)
            {
                log.warn("历史变量 {} 反序列化失败，已跳过：{}", name, e.getMessage());
            }
        }
        return vars;
    }

    /**
     * 复活后把业务数据行重新指向新实例
     *
     * businessKey 是业务行主键，复活沿用同一行，只把 process_instance_id 换到新实例。
     * 不能只信 selectFlowFormByProcessKey：模型 metaInfo 绑定的 formId 可能与实例实际落库的表不一致
     * （历史数据/多表单绑定），因此按「旧实例ID + 主键」逐张候选表探测，命中哪张改哪张。
     * 找不到时只告警，不影响流程本身已经跑起来。
     */
    private void rebindBusinessInstanceId(String processKey, String businessKey, String sourceInstanceId,
            String instanceId)
    {
        if (StringUtils.isEmpty(businessKey))
        {
            return;
        }
        for (String table : candidateBusinessTables(processKey))
        {
            try
            {
                int rows = StringUtils.isNotEmpty(sourceInstanceId)
                        ? jdbcTemplate.update("UPDATE `" + table
                                + "` SET process_instance_id = ? WHERE id = ? AND process_instance_id = ?",
                                instanceId, businessKey, sourceInstanceId)
                        : jdbcTemplate.update("UPDATE `" + table + "` SET process_instance_id = ? WHERE id = ?",
                                instanceId, businessKey);
                if (rows > 0)
                {
                    log.info("流程复活：业务表 {} 行 {} 已指向新实例 {}（影响 {} 行）", table, businessKey, instanceId,
                            rows);
                    return;
                }
            }
            catch (Exception e)
            {
                log.debug("流程复活：跳过表 {}（{}）", table, e.getMessage());
            }
        }
        log.warn("流程复活：未定位到 businessKey={} 的业务数据行，实例ID未回填（新实例 {}）", businessKey, instanceId);
    }

    /**
     * 按 businessKey（可选旧实例ID）在候选业务表中定位真实落库的表
     *
     * @return 命中的表名，未命中返回 null
     */
    private String resolveBusinessTable(String processKey, String businessKey, String sourceInstanceId)
    {
        if (StringUtils.isEmpty(businessKey))
        {
            return null;
        }
        for (String table : candidateBusinessTables(processKey))
        {
            try
            {
                List<Long> hit = StringUtils.isNotEmpty(sourceInstanceId)
                        ? jdbcTemplate.queryForList("SELECT id FROM `" + table
                                + "` WHERE id = ? AND process_instance_id = ? LIMIT 1", Long.class, businessKey,
                                sourceInstanceId)
                        : jdbcTemplate.queryForList("SELECT id FROM `" + table + "` WHERE id = ? LIMIT 1", Long.class,
                                businessKey);
                if (!hit.isEmpty())
                {
                    return table;
                }
            }
            catch (Exception ignored)
            {
                // 表结构不一致（无 process_instance_id 列等）直接跳过
            }
        }
        // 兜底：旧实例ID已被覆盖过（重复复活），只按主键再试一轮
        if (StringUtils.isNotEmpty(sourceInstanceId))
        {
            return resolveBusinessTable(processKey, businessKey, null);
        }
        return null;
    }

    /** 候选业务表：先模型绑定表，再全部表单表（新表单优先） */
    private List<String> candidateBusinessTables(String processKey)
    {
        java.util.Set<String> tables = new java.util.LinkedHashSet<>();
        if (StringUtils.isNotEmpty(processKey))
        {
            try
            {
                com.ruoyi.flowable.domain.FlowForm bound = flowFormService.selectFlowFormByProcessKey(processKey);
                if (bound != null && StringUtils.isNotEmpty(bound.getTableName()))
                {
                    tables.add(bound.getTableName());
                }
            }
            catch (Exception ignored)
            {
            }
        }
        try
        {
            tables.addAll(jdbcTemplate.queryForList(
                    "SELECT table_name FROM flow_form WHERE table_name IS NOT NULL AND table_name <> '' ORDER BY form_id DESC",
                    String.class));
        }
        catch (Exception e)
        {
            log.warn("读取表单数据表清单失败：{}", e.getMessage());
        }
        return new ArrayList<>(tables);
    }

    /** 解析实例实际落库的表单（优先业务表反查，兼底走模型绑定） */
    private com.ruoyi.flowable.domain.FlowForm resolveBusinessForm(String processKey, String businessKey,
            String sourceInstanceId)
    {
        String table = resolveBusinessTable(processKey, businessKey, sourceInstanceId);
        if (StringUtils.isNotEmpty(table))
        {
            try
            {
                Long formId = jdbcTemplate.queryForObject(
                        "SELECT form_id FROM flow_form WHERE table_name = ? ORDER BY form_id DESC LIMIT 1", Long.class,
                        table);
                if (formId != null)
                {
                    com.ruoyi.flowable.domain.FlowForm f = flowFormService.selectFlowFormByFormId(formId);
                    if (f != null)
                    {
                        return f;
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("按业务表 {} 反查表单失败：{}", table, e.getMessage());
            }
        }
        return flowFormService.selectFlowFormByProcessKey(processKey);
    }
}
