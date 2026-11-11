package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowInstance;
import com.ruoyi.flowable.domain.FlowTask;
import com.ruoyi.flowable.domain.dto.FlowCompleteDTO;
import com.ruoyi.flowable.domain.dto.FlowStartDTO;

/**
 * 流程实例/任务 Service 接口
 *
 * @author ruoyi
 */
public interface IFlowProcessService
{
    /** 发起流程，返回流程实例ID */
    public String startProcess(FlowStartDTO dto);

    /** 查询我发起的流程 */
    public List<FlowInstance> selectMyProcessList(FlowInstance query);

    /** 查询我参与过的全部流程（发起+审批过） */
    public List<FlowInstance> selectInvolvedProcessList(FlowInstance query);

    /** 查询流程实例详情（含流转历史） */
    public FlowInstance selectInstanceDetail(String instanceId);

    // ==================== 流程监控（系统管理员干预） ====================

    /** 流程监控：全部流程实例（管理员视角，支持标题/发起人/状态/时间等条件） */
    public List<FlowInstance> selectMonitorProcessList(FlowInstance query);

    /** 流程监控：状态统计（all/running/completed/canceled） */
    public java.util.Map<String, Object> countMonitor();

    /**
     * 流程监控：干预面板数据 —— 当前停留节点、当前待办任务（含办理人）、全部可跳转的用户任务节点
     */
    public java.util.Map<String, Object> selectMonitorNodes(String instanceId);

    /**
     * 流程干预：把流程从当前节点强制跳转到指定用户任务节点。
     *
     * @param instanceId       流程实例ID
     * @param targetActivityId 目标节点ID（BPMN 用户任务 id）
     * @param assigneeId       可选，跳转后指定办理人（为空则由节点操作人配置自动派发）
     * @param comment          干预意见（写入流转意见）
     */
    public int interveneInstance(String instanceId, String targetActivityId, Long assigneeId, String comment);

    /**
     * 流程监控：强制收回（对标泛微「强制收回」）——把流程从当前节点回退到上一节点，
     * 由上一节点的原办理人重新办理。
     *
     * <p>对齐泛微口径：管理员后台动作，不要求填写意见，也不在流程记录里写批注留痕，
     * 仅记录系统操作日志。</p>
     *
     * <p>上一节点的判定顺序：① 图上当前节点的上游用户任务中，历史里最近真正走过的那个；
     * ② 历史上没有（刚发起/刚过网关）时取图上最邻近的上游用户任务。</p>
     *
     * @param instanceId 流程实例ID
     * @param reason     可选备注（只进系统日志，不写流程记录）
     * @return 收回结果：fromActivityName / prevActivityId / prevActivityName / prevAssignee / prevAssigneeName
     */
    public java.util.Map<String, Object> recallInstance(String instanceId, String reason);

    /**
     * 管理员结束流程
     *
     * @param instanceId 流程实例ID
     * @param action     end=强制结束 / cancel=撤销
     * @param reason     操作原因
     */
    public int adminEndProcess(String instanceId, String action, String reason);

    /** 管理员删除流程实例（运行中先终止，再连同历史数据一起删除） */
    public int deleteProcessInstance(String instanceId);

    /** 管理员改派当前节点办理人 */
    public int adminAssignTask(String taskId, Long targetUserId, String comment);

    /**
     * 办理人终止流程（强制结束当前流程实例）
     *
     * 用于办理页「终止」操作菜单：办理人认为流程无需继续时直接结束整个流程，
     * 与发起人「撤销」的区别是不限发起人身份，但必须是当前任务的办理人。
     *
     * @param taskId 当前任务ID
     * @param reason 终止原因（写入流转意见）
     */
    public int terminateTask(String taskId, String reason);

    /** 查询我的待办任务 */
    public List<FlowTask> selectTodoList(FlowTask query);

    /** 统计我的待办任务数 */
    public long countTodo();

    /** 待办数量拆分：all 全部待办 / returned 被退回的待办（待办页「退回」页签角标） */
    public java.util.Map<String, Object> countTodoMap();

    /**
     * 查询当前任务的可退回节点（办理页「驳回」弹窗选节点用）。
     *
     * 返回本实例已经走过的用户任务节点（按首次经过顺序），以及发起人（申请）节点；
     * 当前所在节点不返回。
     *
     * @param taskId 当前任务ID
     */
    public java.util.List<java.util.Map<String, Object>> selectReturnableNodes(String taskId);

    /** 查询我的已办（我办理过的任务 + 我发起的流程，按实例去重；statusScope=all/running/finished 控制页签） */
    public List<FlowTask> selectDoneList(FlowTask query);

    /** 统计我的已办数量：all 全部 / running 未结束 / finished 已结束 */
    public java.util.Map<String, Object> countDone();

    /** 办理任务（同意 / 驳回退回）。驳回时按 targetActivityId 把流程退回到指定节点 */
    public int completeTask(FlowCompleteDTO dto);

    /** 流程分类树（最新版本流程定义按分类分组） */
    public java.util.List<java.util.Map<String, Object>> selectProcessTree();

    /** 任务转办 */
    public int delegateTask(String taskId, Long targetUserId, String comment);

    /** 查询流程实例流转记录（时间线） */
    public List<FlowTask> selectHistoryTasks(String instanceId);

    /** 生成流程图（高亮当前节点），返回PNG字节流 */
    public byte[] generateProcessDiagram(String instanceId);

    /**
     * 实例流程图数据：BPMN XML + 已流转节点/连线 + 当前停留节点 + 按节点聚合的人员信息
     * 供前端 bpmn-js 实时渲染（矢量、可缩放、与设计器同配色），替代 PNG 位图
     */
    public java.util.Map<String, Object> selectInstanceDiagram(String instanceId);

    /**
     * 记录「已查看」：办理人/候选人打开办理页时上报任务ID，供流程图展示「已查看人员」
     *
     * @param taskId 任务ID
     */
    public void recordNodeView(String taskId);
}
