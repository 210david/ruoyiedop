package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowModel;

/**
 * 流程模型 Service 接口
 *
 * @author ruoyi
 */
public interface IFlowModelService
{
    /** 查询模型列表 */
    public List<FlowModel> selectModelList(FlowModel model);

    /** 查询模型详情（含BPMN XML） */
    public FlowModel selectModelById(String modelId);

    /** 新增模型 */
    public String insertModel(FlowModel model);

    /** 修改模型（保存设计器XML） */
    public int updateModel(FlowModel model);

    /** 删除模型（含其部署） */
    public int deleteModelByIds(String[] modelIds);

    /** 部署模型为流程定义 */
    public int deployModel(String modelId);

    /** 根据流程定义ID导出BPMN XML */
    public String getDefinitionBpmnXml(String processDefinitionId);

    /** 查询流程定义列表（已部署） */
    public List<FlowModel> selectDefinitionList(FlowModel model);

    /** 挂起/激活流程定义 */
    public int changeDefinitionState(String processDefinitionId, boolean suspend);
}
