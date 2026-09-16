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

    /** 查询流程实例详情（含流转历史） */
    public FlowInstance selectInstanceDetail(String instanceId);

    /** 撤销我发起的进行中流程 */
    public int cancelProcess(String instanceId, String reason);

    /** 查询我的待办任务 */
    public List<FlowTask> selectTodoList(FlowTask query);

    /** 统计我的待办任务数 */
    public long countTodo();

    /** 查询我的已办任务 */
    public List<FlowTask> selectDoneList(FlowTask query);

    /** 办理任务（同意/驳回） */
    public int completeTask(FlowCompleteDTO dto);

    /** 任务转办 */
    public int delegateTask(String taskId, Long targetUserId, String comment);

    /** 查询流程实例流转记录（时间线） */
    public List<FlowTask> selectHistoryTasks(String instanceId);

    /** 生成流程图（高亮当前节点），返回PNG字节流 */
    public byte[] generateProcessDiagram(String instanceId);
}
