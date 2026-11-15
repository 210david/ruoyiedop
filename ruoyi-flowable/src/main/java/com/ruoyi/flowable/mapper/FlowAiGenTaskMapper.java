package com.ruoyi.flowable.mapper;

import java.util.List;
import com.ruoyi.flowable.domain.FlowAiGenTask;

/**
 * AI流程生成任务 Mapper
 *
 * @author ruoyi
 */
public interface FlowAiGenTaskMapper
{
    /**
     * 查询任务列表
     */
    List<FlowAiGenTask> selectFlowAiGenTaskList(FlowAiGenTask query);

    /**
     * 根据ID查询任务
     */
    FlowAiGenTask selectFlowAiGenTaskById(Long taskId);

    /**
     * 新增任务
     */
    int insertFlowAiGenTask(FlowAiGenTask task);

    /**
     * 修改任务
     */
    int updateFlowAiGenTask(FlowAiGenTask task);

    /**
     * 批量软删除
     */
    int deleteFlowAiGenTaskByIds(Long[] taskIds);
}
