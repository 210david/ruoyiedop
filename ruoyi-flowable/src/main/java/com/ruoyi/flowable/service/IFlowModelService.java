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

    /** 部署模型为流程定义；mode=new 新版本并存（默认），mode=replace 覆盖当前版本（部署后停用旧版本并设为活动版本）；remark 版本备注；setActive 是否设为活动版本 */
    public int deployModel(String modelId, String mode, String remark, Boolean setActive);

    /** 更新流程定义版本备注 */
    public int updateDefinitionRemark(String processDefinitionId, String remark);

    /** 设为当前活动版本（同Key其他版本取消活动标志） */
    public int setActiveDefinition(String processDefinitionId);

    /** 根据流程定义ID导出BPMN XML */
    public String getDefinitionBpmnXml(String processDefinitionId);

    /**
     * 按流程 Key 取当前活动版本（无活动版本时取最高版本）的 BPMN XML
     * 供前台发起页用 bpmn-js 渲染流程图，样式与后台设计器一致
     */
    public String getDefinitionBpmnXmlByKey(String processKey);

    /** 查询流程定义列表（已部署） */
    public List<FlowModel> selectDefinitionList(FlowModel model);

    /**
     * 生成流程定义流程图（发起前预览）
     * 按流程 Key 取当前活动版本（无活动版本时取最高版本）的 BPMN 图
     */
    public byte[] generateDefinitionDiagram(String processKey);

    /** 挂起/激活流程定义 */
    public int changeDefinitionState(String processDefinitionId, boolean suspend);

    /**
     * 部署前结构对比：当前保存的流程模型 vs 已部署版本。
     * 结构未变（仅表单字段/必填/只读/名称调整）→ 无需存为新版本；
     * 增删节点、变更流转线、更换绑定数据表 → 需部署新版本。
     */
    public java.util.Map<String, Object> checkDeployStructure(String modelId);
}
