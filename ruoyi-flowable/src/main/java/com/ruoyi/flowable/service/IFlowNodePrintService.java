package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowNodePrint;

/**
 * 流程节点打印模板 Service 接口
 *
 * @author ruoyi
 */
public interface IFlowNodePrintService
{
    /** 某流程模型下所有节点的打印模板配置 */
    public List<FlowNodePrint> selectByModelId(String modelId);

    /** 按模型ID + 节点ID 取配置（设计器回显） */
    public FlowNodePrint selectByModelIdAndNodeId(String modelId, String nodeId);

    /** 按流程定义Key + 节点ID 取配置（办理页打印） */
    public FlowNodePrint selectByModelKeyAndNodeId(String modelKey, String nodeId);

    /** 保存（同一节点只保留一条，存在则覆盖）；打印模板内容为表单快照 */
    public int saveNodePrint(FlowNodePrint nodePrint);

    /** 删除节点打印模板（恢复为「跟随节点显示模板」） */
    public int deleteByModelIdAndNodeId(String modelId, String nodeId);
}
