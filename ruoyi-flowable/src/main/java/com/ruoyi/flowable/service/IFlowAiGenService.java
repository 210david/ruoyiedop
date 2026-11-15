package com.ruoyi.flowable.service;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.flowable.domain.FlowAiGenTask;

/**
 * AI流程生成 Service
 *
 * @author ruoyi
 */
public interface IFlowAiGenService
{
    /**
     * 查询任务列表
     */
    List<FlowAiGenTask> selectFlowAiGenTaskList(FlowAiGenTask query);

    /**
     * 查询任务详情
     */
    FlowAiGenTask selectFlowAiGenTaskById(Long taskId);

    /**
     * 上传流程方案并调用AI解析，返回任务ID
     *
     * @param remark 备注说明（用户补充的生成要求，可空）
     */
    Long uploadAndAnalyze(MultipartFile file, String taskName, String remark);

    /**
     * 基于已保存的方案文本重新解析
     *
     * @param remark 备注说明（传空时沿用任务已保存的备注）
     */
    FlowAiGenTask reanalyze(Long taskId, String remark);

    /**
     * 按确认后的结构化结果生成表单与流程模型草稿（不部署）
     */
    Map<String, Object> generate(Long taskId, String resultJson);

    /**
     * 批量删除任务
     */
    int deleteFlowAiGenTaskByIds(Long[] taskIds);
}
